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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
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

    /**
     * Ordered child panels.
     */
    private final List<DockingPanel> children = new ArrayList<>();

    /**
     * dividerPositions[i] is the position of the divider between children[i] and
     * children[i+1], as a fraction [0,1] of this panel's axis dimension.
     * Used for persistence (save/restore). Length equals children.size() - 1.
     */
    private final List<Double> dividerPositions = new ArrayList<>();

    /**
     * dividerPixels[i] is the absolute pixel position of divider[i] along the
     * split axis, measured from the leading edge of this panel.
     * A value of -1 means "not yet initialised; compute from dividerPositions on
     * the next layout pass".  Once set, this value is kept fixed across parent
     * resizes so that only the last child absorbs the extra space.
     */
    private final List<Integer> dividerPixels = new ArrayList<>();

    /**
     * One DividerBar per divider, parallel to dividerPositions.
     */
    private final List<DividerBar> dividerBars = new ArrayList<>();

    /**
     * JSplitPane.HORIZONTAL_SPLIT or VERTICAL_SPLIT.
     */
    private int orientation = JSplitPane.HORIZONTAL_SPLIT;

    private DockingPanel parent;
    private final DockingAPI docking;
    private final Window window;
    private String anchor;

    /**
     * Guards against re-entrant layoutChildren calls triggered by validate().
     */
    private boolean inLayout = false;

    /**
     * Set to true while a divider drag is in progress.  During drag, layout uses
     * absolute pixel positions so that only the two adjacent children resize and
     * all other panels stay stationary.  Outside of a drag (e.g. window resize),
     * layout uses stored fractions so that all children resize proportionally.
     */
    private static boolean isDragging = false;

    /**
     * Create a new DockedSplitPanel.  Children and orientation must be
     * configured before the panel is displayed.
     */
    public DockedSplitPanel(DockingAPI docking, Window window, String anchor) {
        this.docking = docking;
        this.window = window;
        this.anchor = anchor;
        setLayout(null);
    }

    @Override
    public void doLayout() {
        layoutChildren();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        layoutChildren();
    }

    @Override
    public Dimension getMinimumSize() {
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        // Split axis: sum of children + dividers between them.
        // Perpendicular axis: max of children (they all share the same cross-dimension).
        int axisMin = DIVIDER_THICKNESS * Math.max(0, children.size() - 1);
        int perpMin = 0;

        for (DockingPanel child : children) {
            if (child == null) {
                continue;
            }
            Dimension min = child.getMinimumSize();
            axisMin += horiz ? min.width : min.height;
            perpMin = Math.max(perpMin, horiz ? min.height : min.width);
        }
        return horiz ? new Dimension(axisMin, perpMin) : new Dimension(perpMin, axisMin);
    }

    /**
     * Minimum pixels needed for children[0..divIndex] plus the dividers between them.
     */
    private int minPxBefore(int divIndex) {
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        int px = 0;

        for (int i = 0; i <= divIndex; i++) {
            Dimension min = children.get(i).getMinimumSize();
            px += horiz ? min.width : min.height;

            if (i < divIndex) {
                px += DIVIDER_THICKNESS;
            }
        }
        return px;
    }

    /**
     * Minimum pixels needed for children[divIndex+1..n-1] plus the dividers between them.
     */
    private int minPxAfter(int divIndex) {
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        int px = 0;

        for (int i = divIndex + 1; i < children.size(); i++) {
            Dimension min = children.get(i).getMinimumSize();
            px += horiz ? min.width : min.height;

            if (i < children.size() - 1) {
                px += DIVIDER_THICKNESS;
            }
        }
        return px;
    }

    // ----------------------------------------------------------------
    // Child management (called during construction and dock operations)
    // ----------------------------------------------------------------

    /**
     * Set the left/top child (index 0).
     */
    public void setLeft(DockingPanel panel) {
        setChildAt(0, panel);
    }

    /**
     * Set the right/bottom child (index 1).
     */
    public void setRight(DockingPanel panel) {
        setChildAt(1, panel);
    }

    private void setChildAt(int index, DockingPanel panel) {
        if (index < children.size()) {
            remove(children.get(index));
            children.set(index, panel);
        }
        else {
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
        }
        else {
            children.add(idx, panel);
        }
        // In both cases the new divider sits at the original index of existingChild.
        dividerPositions.add(idx, dividerPos);
        dividerPixels.add(idx, -1);

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

    /**
     * Return the index of {@code child} within this panel, or -1.
     */
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
            dividerPixels.add(-1);
        }
        else {
            dividerPositions.set(0, proportion);
            dividerPixels.set(0, -1);
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

        for (int i = 0; i < out.length; i++) {
            out[i] = dividerPositions.get(i);
        }
        return out;
    }

    /**
     * Replace all divider positions at once — used when restoring N-ary
     * layouts from disk.
     */
    public void setDividerPositions(double[] positions) {
        dividerPositions.clear();
        dividerPixels.clear();
        for (double p : positions) {
            dividerPositions.add(p);
            dividerPixels.add(-1);
        }
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
        if (inLayout) {
            return;
        }
        inLayout = true;
        try {
            layoutChildrenImpl();
        }
        finally {
            inLayout = false;
        }
    }

    private void layoutChildrenImpl() {
        if (children.isEmpty()) {
            return;
        }
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        int total = horiz ? getWidth() : getHeight();

        if (total <= 0) {
            return;
        }
        int prev = 0;

        for (int i = 0; i < children.size(); i++) {
            DockingPanel child = children.get(i);

            if (child == null) {
                continue;
            }

            if (i < dividerPositions.size()) {
                // During a drag: use stored pixels so that only the two children adjacent
                // to the dragged divider resize; all others stay stationary.
                // Outside a drag (window resize, restore, etc.): use fractions so that
                // all children resize proportionally.
                int px;

                if (isDragging) {
                    px = dividerPixels.get(i);

                    if (px < 0) {
                        px = (int) Math.round(dividerPositions.get(i) * total);
                    }
                }
                else {
                    px = (int) Math.round(dividerPositions.get(i) * total);
                }
                // Lower bound: leave at least children[i]'s minimum space before the divider.
                // Upper bound: leave enough space for all children after the divider.
                Dimension minI = children.get(i).getMinimumSize();
                int lo = prev + (horiz ? minI.width : minI.height);
                int divStart = clamp(px, lo, total - DIVIDER_THICKNESS - minPxAfter(i));
                // Update pixel cache so drag has an accurate starting point.
                // Fractions are only updated by onDrag / syncFractionsFromPixels.
                dividerPixels.set(i, divStart);

                int childSize = divStart - prev;
                if (horiz) {
                    child.setBounds(prev, 0, Math.max(0, childSize), getHeight());

                    if (i < dividerBars.size()) {
                        dividerBars.get(i).setBounds(divStart, 0, DIVIDER_THICKNESS, getHeight());
                    }
                }
                else {
                    child.setBounds(0, prev, getWidth(), Math.max(0, childSize));

                    if (i < dividerBars.size()) {
                        dividerBars.get(i).setBounds(0, divStart, getWidth(), DIVIDER_THICKNESS);
                    }
                }
                prev = divStart + DIVIDER_THICKNESS;
            }
            else {
                int childSize = Math.max(0, total - prev);

                if (horiz) {
                    child.setBounds(prev, 0, childSize, getHeight());
                }
                else {
                    child.setBounds(0, prev, getWidth(), childSize);
                }
            }
        }
        if (!docking.getAppState().isPaused()) {
            // validate() is synchronous: it calls doLayout() on this (guarded by inLayout
            // to prevent re-entry) then recursively validates all children, running their
            // layout managers immediately rather than deferring to the EDT queue.
            validate();
            repaint();
        }
    }

    /**
     * Walk this split and all descendant splits, updating every stored fraction
     * to match the current pixel position relative to the current panel size.
     * Called after a drag ends so that subsequent window resizes proportion from
     * the new post-drag layout rather than from stale pre-drag fractions.
     */
    private void syncFractionsFromPixels() {
        boolean horiz = (orientation == JSplitPane.HORIZONTAL_SPLIT);
        int total = horiz ? getWidth() : getHeight();

        if (total > 0) {
            for (int i = 0; i < dividerPixels.size() && i < dividerPositions.size(); i++) {
                int px = dividerPixels.get(i);

                if (px >= 0) {
                    dividerPositions.set(i, (double) px / total);
                }
            }
        }

        for (DockingPanel child : children) {
            if (child instanceof DockedSplitPanel) {
                ((DockedSplitPanel) child).syncFractionsFromPixels();
            }
        }
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

        // CENTER has no meaning for a split — redirect to the nearer edge
        if (region == DockingRegion.CENTER) {
            region = (orientation == JSplitPane.HORIZONTAL_SPLIT) ? DockingRegion.WEST : DockingRegion.NORTH;
        }

        boolean after = (region == DockingRegion.EAST || region == DockingRegion.SOUTH);
        int newOrientation = orientationForRegion(region);
        DockingPanel newPanel = createLeafPanel(docking, wrapper, anchor);

        if (newOrientation == orientation) {
            // Same axis: append to the near end of this split.
            double newDivPos = appendDividerPosition(after, dividerProportion);
            insertChildBeside(children.get(after ? children.size() - 1 : 0), newPanel, after, newDivPos);
        }
        else {
            // Different axis: wrap this split in a new outer split.
            dockPanelBeside(this, parent, newPanel, region, dividerProportion, docking, window, anchor);
        }
    }

    /**
     * Computes the position for a new divider appended to the near end of this split.
     * When docking after (EAST/SOUTH) the new divider sits inside the space after the
     * last existing divider.  When docking before (NORTH/WEST) it sits inside the space
     * before the first existing divider.
     */
    private double appendDividerPosition(boolean after, double proportion) {
        if (after) {
            double lastEnd = dividerPositions.isEmpty() ? 0.0 : dividerPositions.get(dividerPositions.size() - 1);
            return lastEnd + (1.0 - lastEnd) * (1.0 - proportion);
        }
        double firstEnd = dividerPositions.isEmpty() ? 1.0 : dividerPositions.get(0);
        return firstEnd * proportion;
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
        if (parent == null) {
            return;
        }

        int idx = children.indexOf(child);

        if (idx < 0) {
            return;
        }

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
            dividerPixels.remove(divIdx);
        }

        if (children.size() == 1) {
            // Only one child left — collapse this split by promoting the survivor.
            parent.replaceChild(this, children.get(0));
        }
        else {
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
        private int dragStartPixel;

        DividerBar(int index) {
            this.index = index;
            Color dividerColor = UIManager.getColor("SplitPane.dividerColor");
            setBackground(dividerColor != null ? dividerColor : new Color(0xaaaaaa));
            setOpaque(true);
            updateCursor();

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    isDragging = true;
                    Point p = e.getLocationOnScreen();
                    dragStartScreen = isHoriz() ? p.x : p.y;
                    int px = dividerPixels.get(index);

                    if (px < 0) {
                        int total = isHoriz() ? DockedSplitPanel.this.getWidth() : DockedSplitPanel.this.getHeight();
                        px = (int) Math.round(dividerPositions.get(index) * total);
                        dividerPixels.set(index, px);
                    }
                    dragStartPixel = px;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    onDrag(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDragging = false;
                    // Sync all fractions from current pixel positions so that the next
                    // window resize proportions from the new post-drag layout.
                    syncFractionsFromPixels();
                    docking.getAppState().persist();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        // Double-click: reset to equal split
                        int total = isHoriz() ? DockedSplitPanel.this.getWidth() : DockedSplitPanel.this.getHeight();
                        int n = children.size();
                        int equalPx = (int) Math.round((double) (index + 1) / n * total);
                        dividerPixels.set(index, equalPx);
                        dividerPositions.set(index, (double) equalPx / total);
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
            if (total <= 0) {
                return;
            }

            int newPx = dragStartPixel + delta;

            // Clamp to the range that keeps both adjacent children above their minimum
            // size.  Neighbouring dividers' pixel positions (unchanged by this drag)
            // define the outer boundaries; only child[index] and child[index+1] resize.
            int prevEdge = (index > 0)
                    ? (isHoriz() ? dividerBars.get(index - 1).getX() : dividerBars.get(index - 1).getY()) + DIVIDER_THICKNESS
                    : 0;
            Dimension minI = children.get(index).getMinimumSize();
            int lo = prevEdge + (isHoriz() ? minI.width : minI.height);

            int nextEdge = (index < dividerBars.size() - 1)
                    ? (isHoriz() ? dividerBars.get(index + 1).getX() : dividerBars.get(index + 1).getY())
                    : total;
            Dimension minNext = children.get(index + 1).getMinimumSize();
            int hi = nextEdge - DIVIDER_THICKNESS - (isHoriz() ? minNext.width : minNext.height);

            int clampedPx = Math.max(lo, Math.min(hi, newPx));
            dividerPixels.set(index, clampedPx);
            dividerPositions.set(index, (double) clampedPx / total);
            layoutChildren();
        }
    }

    // ----------------------------------------------------------------
    // Helpers shared with DockedSimplePanel / DockedTabbedPanel
    // ----------------------------------------------------------------

    /**
     * Creates the appropriate leaf panel for {@code wrapper}: anchor, tabbed, or simple.
     */
    static DockingPanel createLeafPanel(DockingAPI docking, DockableWrapper wrapper, String anchor) {
        if (wrapper.isAnchor()) {
            return new DockedAnchorPanel(docking, wrapper);
        }
        if (Settings.alwaysDisplayTabsMode()) {
            return new DockedTabbedPanel(docking, wrapper, anchor);
        }
        return new DockedSimplePanel(docking, wrapper, anchor);
    }

    /**
     * Returns the JSplitPane orientation constant that corresponds to {@code region}.
     */
    static int orientationForRegion(DockingRegion region) {
        if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
            return JSplitPane.HORIZONTAL_SPLIT;
        }
        return JSplitPane.VERTICAL_SPLIT;
    }

    /**
     * Docks {@code newPanel} beside {@code thisPanel} in the direction indicated by
     * {@code region}.  If the parent split already runs along the same axis the new
     * panel is inserted inline (no extra nesting).  Otherwise a new 2-child split is
     * created to wrap {@code thisPanel}.
     */
    static void dockPanelBeside(DockingPanel thisPanel, DockingPanel parentPanel,
                                 DockingPanel newPanel, DockingRegion region,
                                 double proportion, DockingAPI docking,
                                 Window window, String anchor) {
        boolean after = (region == DockingRegion.EAST || region == DockingRegion.SOUTH);
        int newOrientation = orientationForRegion(region);

        if (parentPanel instanceof DockedSplitPanel) {
            DockedSplitPanel parentSplit = (DockedSplitPanel) parentPanel;

            if (parentSplit.getOrientation() == newOrientation) {
                // Insert inline — avoids nesting two splits of the same axis.
                double newDivPos = inlineDividerPosition(parentSplit, thisPanel, after, proportion);
                parentSplit.insertChildBeside(thisPanel, newPanel, after, newDivPos);
                return;
            }
        }

        // Wrap thisPanel in a new 2-child split.
        DockedSplitPanel split = new DockedSplitPanel(docking, window, anchor);
        split.setOrientation(newOrientation);
        parentPanel.replaceChild(thisPanel, split);

        if (after) {
            split.setLeft(thisPanel);
            split.setRight(newPanel);
            split.setDividerLocation(1.0 - proportion);
        }
        else {
            split.setLeft(newPanel);
            split.setRight(thisPanel);
            split.setDividerLocation(proportion);
        }
    }

    /**
     * Computes the fraction position for a new divider when inserting inline into
     * {@code parentSplit} beside {@code child}.
     */
    private static double inlineDividerPosition(DockedSplitPanel parentSplit, DockingPanel child,
                                                 boolean after, double proportion) {
        int myIndex = parentSplit.indexOfChild(child);
        double[] positions = parentSplit.getDividerPositions();
        double childStart = (myIndex > 0) ? positions[myIndex - 1] : 0.0;
        double childEnd = (myIndex < positions.length) ? positions[myIndex] : 1.0;
        double childSpace = childEnd - childStart;

        if (after) {
            return childStart + childSpace * (1.0 - proportion);
        }
        return childStart + childSpace * proportion;
    }
}
