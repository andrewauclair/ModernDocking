/*
Copyright (c) 2022-2023 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.settings.Settings;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

/**
 * Flat N-ary split container that replaces nested JSplitPane instances.
 *
 * <p>Each instance owns N child panels (N >= 2) and N-1 divider bars laid out
 * in a single top-down pass.  Every divider position is stored as a fraction of
 * <em>this panel's own rect</em>, so dragging one divider changes exactly one
 * stored value and leaves all sibling dividers at their absolute pixel positions.
 *
 * <p>The old JSplitPane nesting problem is eliminated because there is no
 * recursive proportion chain: every proportion is relative to the same rect.
 */
public class DockedSplitPanel extends DockingPanel {

    static final int DIVIDER_THICKNESS = 5;
    private static final int MIN_CHILD_PX = 20;

    /** Ordered child panels. */
    private final List<DockingPanel> children = new ArrayList<>();

    /**
     * dividerPositions[i] is the position of the divider between children[i] and
     * children[i+1], as a fraction [0,1] of this panel's axis dimension.
     * Length is always children.size() - 1 (once fully constructed).
     */
    private final List<Double> dividerPositions = new ArrayList<>();

    /** One DividerBar per divider, parallel to dividerPositions. */
    private final List<DividerBar> dividerBars = new ArrayList<>();

    /** JSplitPane.HORIZONTAL_SPLIT or VERTICAL_SPLIT. */
    private int orientation = JSplitPane.HORIZONTAL_SPLIT;

    private DockingPanel parent;
    private final DockingAPI docking;
    private final Window window;
    private String anchor;

    /**
     * Create a new DockedSplitPanel.  Children and orientation must be
     * configured before the panel is displayed.
     */
    public DockedSplitPanel(DockingAPI docking, Window window, String anchor) {
        this.docking = docking;
        this.window = window;
        this.anchor = anchor;
        setLayout(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutChildren();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        layoutChildren();
    }

    // ----------------------------------------------------------------
    // Child management (called during construction and dock operations)
    // ----------------------------------------------------------------

    /** Set the left/top child (index 0). */
    public void setLeft(DockingPanel panel) {
        setChildAt(0, panel);
    }

    /** Set the right/bottom child (index 1). */
    public void setRight(DockingPanel panel) {
        setChildAt(1, panel);
    }

    private void setChildAt(int index, DockingPanel panel) {
        if (index < children.size()) {
            remove(children.get(index));
            children.set(index, panel);
        } else {
            children.add(panel);
        }
        panel.setParent(this);
        add(panel);
        rebuildDividerBars();
    }

    /**
     * Insert {@code panel} beside {@code existingChild}.
     *
     * @param existingChild the child to split beside
     * @param panel         the new child to insert
     * @param after         true = new child goes after (EAST/SOUTH), false = before (WEST/NORTH)
     * @param dividerPos    fraction of this panel's axis where the new divider sits
     */
    public void insertChildBeside(DockingPanel existingChild, DockingPanel panel,
                                  boolean after, double dividerPos) {
        int idx = children.indexOf(existingChild);
        if (idx < 0) return;

        if (after) {
            children.add(idx + 1, panel);
        } else {
            children.add(idx, panel);
        }
        // In both cases the new divider sits at the original index of existingChild.
        dividerPositions.add(idx, dividerPos);

        panel.setParent(this);
        add(panel);
        rebuildDividerBars();
        layoutChildren();
    }

    /**
     * Append a child without creating dividers — used only during layout
     * restoration where divider positions are applied afterwards via
     * {@link #setDividerPositions(double[])}.
     */
    public void addChildForRestore(DockingPanel child) {
        child.setParent(this);
        children.add(child);
        add(child);
    }

    public DockingPanel getLeft() {
        return children.isEmpty() ? null : children.get(0);
    }

    public DockingPanel getRight() {
        return children.size() < 2 ? null : children.get(1);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        for (DividerBar bar : dividerBars) {
            bar.updateCursor();
        }
    }

    /** Return the index of {@code child} within this panel, or -1. */
    public int indexOfChild(DockingPanel child) {
        return children.indexOf(child);
    }

    // ----------------------------------------------------------------
    // Divider position API
    // ----------------------------------------------------------------

    /**
     * Set the single divider proportion for a freshly constructed 2-child split.
     * Also used by legacy restore code that still calls this method.
     */
    public void setDividerLocation(double proportion) {
        if (dividerPositions.isEmpty()) {
            dividerPositions.add(proportion);
        } else {
            dividerPositions.set(0, proportion);
        }
        rebuildDividerBarsIfNeeded();
        layoutChildren();
    }

    /**
     * Snapshot of all divider positions as a double array — used when
     * persisting layouts.
     */
    public double[] getDividerPositions() {
        double[] out = new double[dividerPositions.size()];
        for (int i = 0; i < out.length; i++) out[i] = dividerPositions.get(i);
        return out;
    }

    /**
     * Replace all divider positions at once — used when restoring N-ary
     * layouts from disk.
     */
    public void setDividerPositions(double[] positions) {
        dividerPositions.clear();
        for (double p : positions) dividerPositions.add(p);
        rebuildDividerBars();
        layoutChildren();
    }

    // ----------------------------------------------------------------
    // Divider bar management
    // ----------------------------------------------------------------

    private void rebuildDividerBars() {
        for (DividerBar bar : dividerBars) remove(bar);
        dividerBars.clear();
        int count = Math.max(0, children.size() - 1);
        for (int i = 0; i < count; i++) {
            DividerBar bar = new DividerBar(i);
            dividerBars.add(bar);
            add(bar);
        }
    }

    private void rebuildDividerBarsIfNeeded() {
        if (dividerBars.size() != Math.max(0, children.size() - 1)) {
            rebuildDividerBars();
        }
    }

    // ----------------------------------------------------------------
    // Layout pass
    // ----------------------------------------------------------------

    void layoutChildren() {
        if (children.isEmpty()) return;
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        int total = horiz ? getWidth() : getHeight();
        int prev = 0;

        for (int i = 0; i < children.size(); i++) {
            DockingPanel child = children.get(i);
            if (child == null) continue;

            if (i < dividerPositions.size()) {
                int divStart = clamp((int) Math.round(dividerPositions.get(i) * total), prev, total);
                int childSize = divStart - prev;
                if (horiz) {
                    child.setBounds(prev, 0, Math.max(0, childSize), getHeight());
                    if (i < dividerBars.size()) {
                        dividerBars.get(i).setBounds(divStart, 0, DIVIDER_THICKNESS, getHeight());
                    }
                } else {
                    child.setBounds(0, prev, getWidth(), Math.max(0, childSize));
                    if (i < dividerBars.size()) {
                        dividerBars.get(i).setBounds(0, divStart, getWidth(), DIVIDER_THICKNESS);
                    }
                }
                prev = divStart + DIVIDER_THICKNESS;
            } else {
                int childSize = Math.max(0, total - prev);
                if (horiz) {
                    child.setBounds(prev, 0, childSize, getHeight());
                } else {
                    child.setBounds(0, prev, getWidth(), childSize);
                }
            }
        }
        revalidate();
        repaint();
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ----------------------------------------------------------------
    // DockingPanel interface
    // ----------------------------------------------------------------

    @Override
    public String getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    @Override
    public void setParent(DockingPanel parent) {
        this.parent = parent;
    }

    @Override
    public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
        wrapper.setWindow(window);

        if (region == DockingRegion.CENTER) {
            // Docking to the center of a split — redirect to an edge
            region = (orientation == JSplitPane.HORIZONTAL_SPLIT)
                    ? DockingRegion.WEST : DockingRegion.NORTH;
        }

        int newOrientation = (region == DockingRegion.EAST || region == DockingRegion.WEST)
                ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

        DockingPanel newPanel = createLeafPanel(docking, wrapper, anchor);

        if (newOrientation == orientation) {
            // Same axis: insert at the start or end of this split.
            if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
                double lastDivEnd = dividerPositions.isEmpty() ? 0.0
                        : dividerPositions.get(dividerPositions.size() - 1);
                double newDivPos = lastDivEnd + (1.0 - lastDivEnd) * (1.0 - dividerProportion);
                insertChildBeside(children.get(children.size() - 1), newPanel, true, newDivPos);
            } else {
                double firstDivEnd = dividerPositions.isEmpty() ? 1.0 : dividerPositions.get(0);
                double newDivPos = firstDivEnd * dividerProportion;
                insertChildBeside(children.get(0), newPanel, false, newDivPos);
            }
        } else {
            // Different axis: wrap this split in a new outer split.
            DockedSplitPanel outerSplit = new DockedSplitPanel(docking, window, anchor);
            parent.replaceChild(this, outerSplit);

            if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
                outerSplit.setLeft(this);
                outerSplit.setRight(newPanel);
                dividerProportion = 1.0 - dividerProportion;
            } else {
                outerSplit.setLeft(newPanel);
                outerSplit.setRight(this);
            }
            outerSplit.setOrientation(newOrientation);
            outerSplit.setDividerLocation(dividerProportion);
        }
    }

    @Override
    public void undock(Dockable dockable) {
        // Undocking is handled by the leaf panels; this split panel is only a container.
    }

    @Override
    public void replaceChild(DockingPanel child, DockingPanel newChild) {
        int idx = children.indexOf(child);
        if (idx >= 0) {
            remove(child);
            children.set(idx, newChild);
            newChild.setParent(this);
            add(newChild);
            layoutChildren();
        }
    }

    @Override
    public void removeChild(DockingPanel child) {
        if (parent == null) return;

        int idx = children.indexOf(child);
        if (idx < 0) return;

        remove(child);

        // Remove the divider that was adjacent to this child.
        // Prefer the divider to the right (index idx); fall back to the left (idx-1).
        int divIdx = (idx < dividerPositions.size()) ? idx : idx - 1;
        if (divIdx >= 0 && divIdx < dividerBars.size()) {
            remove(dividerBars.get(divIdx));
        }
        children.remove(idx);
        if (divIdx >= 0 && divIdx < dividerPositions.size()) {
            dividerPositions.remove(divIdx);
        }

        if (children.size() == 1) {
            // Only one child left — collapse this split by promoting the survivor.
            parent.replaceChild(this, children.get(0));
        } else {
            rebuildDividerBars();
            layoutChildren();
        }
    }

    @Override
    public List<DockingPanel> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // ----------------------------------------------------------------
    // DividerBar
    // ----------------------------------------------------------------

    final class DividerBar extends javax.swing.JComponent {

        private final int index;
        private int dragStartScreen;
        private double dragStartPosition;

        DividerBar(int index) {
            this.index = index;
            Color dividerColor = UIManager.getColor("SplitPane.dividerColor");
            setBackground(dividerColor != null ? dividerColor : new Color(0xaaaaaa));
            setOpaque(true);
            updateCursor();

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Point p = e.getLocationOnScreen();
                    dragStartScreen = isHoriz() ? p.x : p.y;
                    dragStartPosition = dividerPositions.get(index);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    onDrag(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    docking.getAppState().persist();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        // Double-click: reset to equal split
                        dividerPositions.set(index, equalPosition());
                        layoutChildren();
                        docking.getAppState().persist();
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        void updateCursor() {
            setCursor(Cursor.getPredefinedCursor(
                    isHoriz() ? Cursor.E_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR));
        }

        private boolean isHoriz() {
            return orientation == JSplitPane.HORIZONTAL_SPLIT;
        }

        private void onDrag(MouseEvent e) {
            Point p = e.getLocationOnScreen();
            int current = isHoriz() ? p.x : p.y;
            int delta = current - dragStartScreen;
            int total = isHoriz() ? DockedSplitPanel.this.getWidth() : DockedSplitPanel.this.getHeight();
            if (total <= 0) return;

            double newPos = dragStartPosition + (double) delta / total;

            // Clamp: at least MIN_CHILD_PX away from the neighbouring dividers or edges.
            double lo = (index > 0)
                    ? dividerPositions.get(index - 1) + (double) (MIN_CHILD_PX + DIVIDER_THICKNESS) / total
                    : (double) MIN_CHILD_PX / total;
            double hi = (index < dividerPositions.size() - 1)
                    ? dividerPositions.get(index + 1) - (double) (MIN_CHILD_PX + DIVIDER_THICKNESS) / total
                    : 1.0 - (double) MIN_CHILD_PX / total;

            dividerPositions.set(index, Math.max(lo, Math.min(hi, newPos)));
            layoutChildren();
        }

        /** Target position when resetting to equal distribution. */
        private double equalPosition() {
            int n = children.size();
            // Position that puts this divider at its "equal share" slot.
            return (double) (index + 1) / n;
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private static DockingPanel createLeafPanel(DockingAPI docking, DockableWrapper wrapper, String anchor) {
        if (wrapper.isAnchor()) return new DockedAnchorPanel(docking, wrapper);
        if (Settings.alwaysDisplayTabsMode()) return new DockedTabbedPanel(docking, wrapper, anchor);
        return new DockedSimplePanel(docking, wrapper, anchor);
    }
}
