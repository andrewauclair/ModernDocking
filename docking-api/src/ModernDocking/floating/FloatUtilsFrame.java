/*
Copyright (c) 2024 Andrew Auclair

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
package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.CustomTabbedPane;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.internal.InternalRootDockingPanel;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;

public class FloatUtilsFrame extends JFrame implements DragSourceMotionListener, ComponentListener {
    private final Window referenceDockingWindow;
    private final InternalRootDockingPanel root;
    private final RootDockingHandles rootHandles;
    private final FloatingOverlay overlay;

    private FloatListener floatListener;
    private JFrame floatingFrame;
    private DragSource dragSource;
    private Dockable currentDockable;
    private DockableHandles dockableHandles;

    BufferStrategy bs; //create an strategy for multi-buffering.
    JPanel renderPanel = new JPanel() {
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

    public FloatUtilsFrame(DockingAPI docking, Window referenceDockingWindow, InternalRootDockingPanel root) {
        this.referenceDockingWindow = referenceDockingWindow;
        this.root = root;
        this.rootHandles = new RootDockingHandles(this, root);
        this.overlay = new FloatingOverlay(docking, this);

        this.referenceDockingWindow.addComponentListener(this);
        SwingUtilities.invokeLater(this::setSizeAndLocation);

        orderFrames();

        setLayout(null); // don't use a layout manager for this custom painted frame
        setUndecorated(true); // don't want to see a frame border
        setType(Type.UTILITY); // hide this frame from the task bar

        setBackground(new Color(0, 0, 0, 0)); // don't want a background for this frame
        getRootPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the root pane either. Workaround for a FlatLaf macOS issue.
        getContentPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the content frame either.

        add(renderPanel);
        renderPanel.setOpaque(false);

        try {
            if (getContentPane() instanceof JComponent) {
                ((JComponent) getContentPane()).setOpaque(false);
            }
        }
        catch (IllegalComponentStateException e) {
            // TODO we need to handle platforms that don't support translucent display
            // this exception indicates that the platform doesn't support changing the opacity
        }
    }

    public void activate(FloatListener floatListener, JFrame floatingFrame, DragSource dragSource, Point mousePosOnScreen) {
        this.floatListener = floatListener;
        this.floatingFrame = floatingFrame;
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);

        mouseMoved(mousePosOnScreen);

        if (floatListener instanceof DisplayPanelFloatListener) {
            Dockable floatingDockable = ((DisplayPanelFloatListener) floatListener).getDockable();
            rootHandles.setFloatingDockable(floatingDockable);
        }

        setVisible(true);

        createBufferStrategy(2);
        bs = this.getBufferStrategy();
        orderFrames();
    }

    public void deactivate() {
        setVisible(false);

        if (dragSource != null) {
            dragSource.removeDragSourceMotionListener(this);
        }
        floatListener = null;
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

        rootHandles.mouseMoved(mousePosOnScreen);

        if (dockableHandles != null) {
            dockableHandles.mouseMoved(mousePosOnScreen);
        }

        boolean prevVisible = overlay.isVisible();

        // hide the overlay. it will be marked visible again if we update it
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
                dockableHandles = new DockableHandles(this, dockable, floatingDockable);
            }
            else {
                dockableHandles = new DockableHandles(this, dockable);
            }
        }
        currentDockable = dockable;

        if (rootHandles.isOverHandle()) {
            if (!floatingFrame.isVisible()) {
                changeVisibility(floatingFrame, true);
            }
            overlay.updateForRoot(root, rootHandles.getRegion());
        }
        else if (dockableHandles != null) {
            if (!floatingFrame.isVisible()) {
                changeVisibility(floatingFrame, true);
            }
            overlay.updateForDockable(currentDockable, floatingDockable, mousePosOnScreen, dockableHandles.getRegion());
        }
        else if (currentDockable == null && floatListener instanceof DisplayPanelFloatListener) {
            CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, referenceDockingWindow);

            changeVisibility(floatingFrame, tabbedPane == null);

            if (tabbedPane != null) {
                overlay.updateForTab(tabbedPane, mousePosOnScreen);
            }
        }
        else if (!floatingFrame.isVisible()) {
            changeVisibility(floatingFrame, true);
        }
        repaint();
        if (overlay.requiresRedraw() || prevVisible != overlay.isVisible()) {
            repaint();
            overlay.clearRedraw();
        }
    }

    private void changeVisibility(JFrame frame, boolean visible) {
        if (frame.isVisible() != visible) {
            frame.setVisible(visible);

            orderFrames();
        }
    }

    private void orderFrames() {
        SwingUtilities.invokeLater(referenceDockingWindow::toFront);
        if (floatingFrame != null) {
            SwingUtilities.invokeLater(floatingFrame::toFront);
        }
        SwingUtilities.invokeLater(this::toFront);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(this::setSizeAndLocation);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        SwingUtilities.invokeLater(this::setSizeAndLocation);
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public boolean isOverRootHandle() {
        return rootHandles.isOverHandle();
    }

    public DockingRegion rootHandleRegion() {
        return rootHandles.getRegion();
    }

    public boolean isOverPinHandle() {
        return rootHandles.isOverPinHandle();
    }

    public ToolbarLocation pinRegion() {
        return rootHandles.getPinRegion();
    }

    public boolean isOverDockableHandle() {
        if (dockableHandles == null) {
            return false;
        }
        return dockableHandles.getRegion() != null;
    }

    public boolean isOverTab() {
        return overlay.isOverTab();
    }

    public DockingRegion dockableHandle() {
        if (dockableHandles == null) {
            return null;
        }
        return dockableHandles.getRegion();
    }

    public DockingRegion getDockableRegion(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen) {
        return overlay.getRegion(targetDockable, floatingDockable, mousePosOnScreen);
    }

    private void setSizeAndLocation() {
        int padding = (int) (DockingHandle.HANDLE_ICON_SIZE * 1.75);

        Point location = new Point(referenceDockingWindow.getLocationOnScreen());
        Dimension size = new Dimension(referenceDockingWindow.getSize());

        location.x -= padding;
        location.y -= padding;

        size.width += padding * 2;
        size.height += padding * 2;

        // set location and size based on the reference docking frame
        setLocation(location);
        setSize(size);

        renderPanel.setSize(size);

        rootHandles.updateHandlePositions();

        revalidate();
        repaint();
    }
}