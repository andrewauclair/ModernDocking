/*
Copyright (c) 2026 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.internal.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.CustomTabbedPane;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A {@link FloatUtils} implementation that renders docking handles and the drop overlay on a
 * transparent {@link JPanel} added to the host window's {@link JLayeredPane}.
 * <p>
 * This approach is used on platforms that do not support per-pixel window translucency,
 * where {@link FloatUtilsFrame} cannot make its background transparent.
 * Because the panel lives inside the host window there is no separate overlay
 * window and no need for z-order management.
 */
class FloatUtilsLayer implements FloatUtils, DragSourceMotionListener, ComponentListener {

    private final Window referenceDockingWindow;
    private final InternalRootDockingPanel root;
    private final JLayeredPane layeredPane;
    private final RootDockingHandles rootHandles;
    private final FloatingOverlay overlay;

    private FloatListener floatListener;
    private JFrame floatingFrame;
    private DragSource dragSource;
    private Dockable currentDockable;
    private DockableHandles dockableHandles;

    /**
     * Proxy panel that hosts the floating dockable's content while the drag is inside the host
     * window. Sits below {@link #renderPanel} in the layered pane so the docking handles and
     * drop overlay render on top. Its {@code contains()} always returns {@code false} so that
     * {@code SwingUtilities.getDeepestComponentAt} passes through to the underlying dockables.
     */
    private final JPanel floatingProxy = new JPanel(new BorderLayout()) {
        @Override
        public boolean contains(int x, int y) {
            return false;
        }
    };

    /**
     * Transparent panel covering the layered pane. Handles are added as children so that
     * their bounds are expressed in this panel's coordinate space, making hit-testing and
     * coordinate conversion consistent.
     */
    private final JPanel renderPanel = new JPanel() {
        @Override
        public boolean contains(int x, int y) {
            // Pass through for SwingUtilities.getDeepestComponentAt so dockables
            // underneath the overlay remain discoverable during hit-testing.
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            rootHandles.paint(g2);

            if (dockableHandles != null) {
                dockableHandles.paint(g2);
            }

            overlay.paint(g);
            g2.dispose();
        }
    };

    /**
     * Create a new layer-based overlay tied to the given host window.
     *
     * @param docking                The docking instance
     * @param referenceDockingWindow The host window; must be a {@link JFrame}
     * @param root                   The internal root panel within the host window
     */
    FloatUtilsLayer(DockingAPI docking, JFrame referenceDockingWindow, InternalRootDockingPanel root) {
        this.referenceDockingWindow = referenceDockingWindow;
        this.root = root;
        this.layeredPane = referenceDockingWindow.getLayeredPane();

        floatingProxy.setVisible(false);
        layeredPane.add(floatingProxy, JLayeredPane.POPUP_LAYER);

        renderPanel.setOpaque(false);
        renderPanel.setLayout(null);

        this.rootHandles = new RootDockingHandles(renderPanel, root);
        this.overlay = new FloatingOverlay(docking, renderPanel);

        referenceDockingWindow.addComponentListener(this);
        SwingUtilities.invokeLater(this::updatePanelBounds);

        layeredPane.add(renderPanel, JLayeredPane.DRAG_LAYER);
        renderPanel.setVisible(false);
    }

    @Override
    public void activate(FloatListener floatListener, JFrame floatingFrame, DragSource dragSource, Point mousePosOnScreen) {
        this.floatListener = floatListener;
        this.floatingFrame = floatingFrame;
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);

        // Make the panel visible before any coordinate conversion: renderPanel is a JPanel,
        // so getLocationOnScreen() (used internally by SwingUtilities.convertPointFromScreen)
        // requires isShowing() == true. Unlike FloatUtilsFrame (a JFrame whose screen
        // location is valid even before setVisible), renderPanel must be in a visible
        // hierarchy first.
        renderPanel.setVisible(true);
        updatePanelBounds();

        // Reparent the floating dockable's content into the layered pane proxy so that the
        // actual dockable renders inside the host window rather than as a separate OS window.
        // A Swing component can only have one parent, so we move it out of the JFrame.
        Container contentPane = floatingFrame.getContentPane();
        if (contentPane.getComponentCount() > 0) {
            floatingProxy.add(contentPane.getComponent(0), BorderLayout.CENTER);
        }
        floatingFrame.setVisible(false);
        updateFloatingProxyBounds();
        floatingProxy.setVisible(true);

        if (floatListener instanceof DisplayPanelFloatListener) {
            Dockable floatingDockable = ((DisplayPanelFloatListener) floatListener).getDockable();
            rootHandles.setFloatingDockable(floatingDockable);
        }

        mouseMoved(mousePosOnScreen);
        renderPanel.repaint();
    }

    @Override
    public void deactivate() {
        renderPanel.setVisible(false);

        if (dragSource != null) {
            dragSource.removeDragSourceMotionListener(this);
        }
        floatListener = null;

        // Move the floating dockable content back into its JFrame before making it visible,
        // so the user sees the dockable ghost while dragging outside the host window.
        if (floatingFrame != null) {
            if (floatingProxy.getComponentCount() > 0) {
                floatingFrame.getContentPane().add(floatingProxy.getComponent(0), BorderLayout.CENTER);
            }
            floatingFrame.setVisible(true);
        }
        floatingProxy.setVisible(false);
        floatingProxy.removeAll();

        floatingFrame = null;
        dragSource = null;
        currentDockable = null;
        dockableHandles = null;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent event) {
        SwingUtilities.invokeLater(() -> mouseMoved(event.getLocation()));
    }

    private void mouseMoved(Point mousePosOnScreen) {
        if (dragSource == null) {
            return;
        }

        updateFloatingProxyBounds();

        rootHandles.mouseMoved(mousePosOnScreen);

        if (dockableHandles != null) {
            dockableHandles.mouseMoved(mousePosOnScreen);
        }

        boolean prevVisible = overlay.isVisible();

        // Hide the overlay; it will be set visible again if we find a target
        overlay.setVisible(false);

        if (!referenceDockingWindow.getBounds().contains(mousePosOnScreen)) {
            return;
        }

        Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, referenceDockingWindow);

        Dockable floatingDockable = null;
        if (floatListener instanceof DisplayPanelFloatListener) {
            floatingDockable = ((DisplayPanelFloatListener) floatListener).getDockable();
        }

        if (dockable != currentDockable) {
            if (dockable == null) {
                dockableHandles = null;
            }
            else if (floatListener instanceof DisplayPanelFloatListener) {
                dockableHandles = new DockableHandles(renderPanel, dockable, floatingDockable);
            }
            else {
                dockableHandles = new DockableHandles(renderPanel, dockable);
            }
        }
        currentDockable = dockable;

        if (rootHandles.isOverHandle()) {
            overlay.updateForRoot(root, rootHandles.getRegion());
        }
        else if (dockableHandles != null) {
            overlay.updateForDockable(currentDockable, floatingDockable, mousePosOnScreen, dockableHandles.getRegion());
        }
        else if (currentDockable == null && floatListener instanceof DisplayPanelFloatListener) {
            CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, referenceDockingWindow);
            if (tabbedPane != null) {
                overlay.updateForTab(tabbedPane, mousePosOnScreen);
            }
        }

        renderPanel.repaint();
        if (overlay.requiresRedraw() || prevVisible != overlay.isVisible()) {
            renderPanel.repaint();
            overlay.clearRedraw();
        }
    }

    @Override
    public boolean isOverRootHandle() {
        return rootHandles.isOverHandle();
    }

    @Override
    public DockingRegion rootHandleRegion() {
        return rootHandles.getRegion();
    }

    @Override
    public boolean isOverPinHandle() {
        return rootHandles.isOverPinHandle();
    }

    @Override
    public ToolbarLocation pinRegion() {
        return rootHandles.getPinRegion();
    }

    @Override
    public boolean isOverDockableHandle() {
        return dockableHandles != null && dockableHandles.getRegion() != null;
    }

    @Override
    public boolean isOverTab() {
        return overlay.isOverTab();
    }

    @Override
    public DockingRegion dockableHandle() {
        return dockableHandles == null ? null : dockableHandles.getRegion();
    }

    @Override
    public DockingRegion getDockableRegion(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen) {
        return overlay.getRegion(targetDockable, floatingDockable, mousePosOnScreen);
    }

    @Override
    public void dispose() {
        referenceDockingWindow.removeComponentListener(this);
        layeredPane.remove(renderPanel);
        layeredPane.remove(floatingProxy);
    }

    // -- ComponentListener --

    @Override
    public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(this::updatePanelBounds);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // Panel position is relative to the layered pane; it doesn't need updating on window move
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // Panel position is relative to the layered pane; it doesn't need updating on window shown
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Panel position is relative to the layered pane; it doesn't need updating on window hidden
    }

    private void updatePanelBounds() {
        renderPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        // updateHandlePositions calls SwingUtilities.convertPointFromScreen against renderPanel,
        // which requires renderPanel.isShowing() == true. Skip it here when the panel is hidden
        // (e.g. the invokeLater from the constructor); activate() calls updatePanelBounds again
        // after making the panel visible.
        if (renderPanel.isShowing()) {
            rootHandles.updateHandlePositions();
        }
        renderPanel.revalidate();
        renderPanel.repaint();
    }

    private void updateFloatingProxyBounds() {
        if (floatingFrame == null) {
            return;
        }
        Point pos = new Point(floatingFrame.getLocation());
        SwingUtilities.convertPointFromScreen(pos, layeredPane);
        floatingProxy.setBounds(pos.x, pos.y, floatingFrame.getWidth(), floatingFrame.getHeight());
    }
}
