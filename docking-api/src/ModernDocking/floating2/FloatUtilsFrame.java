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

public class FloatUtilsFrame extends JFrame implements DragSourceMotionListener {
    private final DockingAPI docking;
    private final Window referenceDockingWindow;
    private final RootDockingPanelAPI root;

    private FloatListener2 floatListener;
    private JFrame floatingFrame;
    private DragSource dragSource;
    private Dockable currentDockable;

    public FloatUtilsFrame(DockingAPI docking, Window referenceDockingWindow, RootDockingPanelAPI root) {
        this.docking = docking;

        this.referenceDockingWindow = referenceDockingWindow;
        this.root = root;
    }

    public void activate(FloatListener2 floatListener, JFrame floatingFrame, DragSource dragSource) {
        this.floatListener = floatListener;
        this.floatingFrame = floatingFrame;
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);
    }

    public void deactivate() {
        dragSource.removeDragSourceMotionListener(this);
        floatListener = null;
        floatingFrame = null;
        dragSource = null;
        currentDockable = null;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent event) {
        if (dragSource == null) {
            return;
        }

        // TODO check if we're over a dockable. if we are, and we weren't before, swap to the dockable overlay
        // TODO check if we're over a tab. if we are, and we weren't before, swap to the tab overlay
        // TODO draw handles. if the state changes then we change which handles are visible

        Point mousePosOnScreen = event.getLocation();

        if (!referenceDockingWindow.getBounds().contains(mousePosOnScreen)) {
            return;
        }

        Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, referenceDockingWindow);

        if (dockable != currentDockable) {

        }
        currentDockable = dockable;

        if (currentDockable == null && floatListener instanceof DisplayPanelFloatListener) {
            CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, referenceDockingWindow);

            // TODO if we're over a tab, hide the floating frame
            // TODO if we're no longer over a tab, show the floating frame again
            // TODO possibly reorder windows. we have it in the old code
        }
    }
}
