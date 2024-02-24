package ModernDocking.floating2;

import ModernDocking.Dockable;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.CustomTabbedPane;
import ModernDocking.internal.DockingComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class FloatUtilsFrame extends JFrame implements DragSourceMotionListener, ComponentListener {
    private final DockingAPI docking;
    private final Window referenceDockingWindow;
    private final RootDockingPanelAPI root;
    private final RootDockingHandles rootHandles;
    private final FloatingOverlay overlay;

    private FloatListener2 floatListener;
    private JFrame floatingFrame;
    private DragSource dragSource;
    private Dockable currentDockable;
    private DockableHandles dockableHandles;

    public FloatUtilsFrame(DockingAPI docking, Window referenceDockingWindow, RootDockingPanelAPI root) {
        this.docking = docking;
        this.referenceDockingWindow = referenceDockingWindow;
        this.root = root;
        this.rootHandles = new RootDockingHandles(this, root);
        this.overlay = new FloatingOverlay(docking, this);

        this.referenceDockingWindow.addComponentListener(this);

        SwingUtilities.invokeLater(() -> {
            setLocation(referenceDockingWindow.getLocation());
            setSize(referenceDockingWindow.getSize());
        });

        setLayout(null); // don't use a layout manager for this custom painted frame
        setUndecorated(true); // don't want to see a frame border
        setType(Type.UTILITY); // hide this frame from the task bar

        setBackground(new Color(0, 0, 0, 0)); // don't want a background for this frame
        getRootPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the root pane either. Workaround for a FlatLaf macOS issue.
        getContentPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the content frame either.

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

    public void activate(FloatListener2 floatListener, JFrame floatingFrame, DragSource dragSource, Point mousePosOnScreen) {
        this.floatListener = floatListener;
        this.floatingFrame = floatingFrame;
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);

        setVisible(true);

        mouseMoved(mousePosOnScreen);
    }

    public void deactivate() {
        setVisible(false);

        dragSource.removeDragSourceMotionListener(this);
        floatListener = null;
        floatingFrame = null;
        dragSource = null;
        currentDockable = null;
        dockableHandles = null;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent event) {
        mouseMoved(event.getLocation());
    }

    private void mouseMoved(Point mousePosOnScreen) {
        if (dragSource == null) {
            return;
        }

        // TODO check if we're over a dockable. if we are, and we weren't before, swap to the dockable overlay
        // TODO check if we're over a tab. if we are, and we weren't before, swap to the tab overlay
        // TODO draw handles. if the state changes then we change which handles are visible

        rootHandles.mouseMoved(mousePosOnScreen);

        if (dockableHandles != null) {
            dockableHandles.mouseMoved(mousePosOnScreen);
        }

        if (!referenceDockingWindow.getBounds().contains(mousePosOnScreen)) {
            overlay.setVisible(false);
            return;
        }

        Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, referenceDockingWindow);

        if (dockable != currentDockable) {
            rootHandles.setTargetDockable(dockable);

            if (dockable == null) {
                dockableHandles = null;
            }
            else {
                dockableHandles = new DockableHandles(this, dockable);
            }
        }
        currentDockable = dockable;

        if (rootHandles.isOverHandle()) {
            overlay.updateForRoot(root, rootHandles.getRegion());
        }
        else if (dockableHandles != null) {
            overlay.updateForDockable(currentDockable, mousePosOnScreen, dockableHandles.getRegion());
        }

        if (currentDockable == null && floatListener instanceof DisplayPanelFloatListener) {
            CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, referenceDockingWindow);

            floatingFrame.setVisible(tabbedPane == null);

            // TODO if we're over a tab, hide the floating frame
            // TODO if we're no longer over a tab, show the floating frame again
            // TODO possibly reorder windows. we have it in the old code
            if (tabbedPane != null) {
                overlay.updateForTab(tabbedPane, mousePosOnScreen);
            }
        }
        else if (!floatingFrame.isVisible()) {
            floatingFrame.setVisible(true);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        rootHandles.paint(g);

        if (dockableHandles != null) {
            dockableHandles.paint(g);
        }

        overlay.paint(g);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setSize(referenceDockingWindow.getSize());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        setLocation(referenceDockingWindow.getLocation());
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}
