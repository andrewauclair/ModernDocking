package ModernDocking.floating2;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.floating.TempFloatingFrame;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.internal.DockedTabbedPanel;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.layouts.WindowLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

public abstract class FloatListener2 extends DragSourceAdapter implements DragSourceMotionListener, DragSourceListener {
    private final DockingAPI docking;
    private final JPanel panel;
    private final JComponent dragComponent;

    // our drag source to support dragging the dockables
    private final DragSource dragSource = new DragSource();

    private Point dragComponentDragOffset = new Point();

    private Window originalWindow;
    private WindowLayout originalWindowLayout;
    private JFrame floatingFrame;

    private Window currentWindow;
    private FloatUtilsFrame currentUtilFrame;

    public FloatListener2(DockingAPI docking, DisplayPanel panel) {
        this(docking, panel, (JComponent) panel.getWrapper().getHeaderUI());
    }

    public FloatListener2(DockingAPI docking, DockedTabbedPanel tabs, JComponent dragComponent) {
        this(docking, (JPanel) tabs, dragComponent);
    }

    private FloatListener2(DockingAPI docking, JPanel panel, JComponent dragComponent) {
        this.docking = docking;
        this.panel = panel;
        this.dragComponent = dragComponent;

        if (dragComponent != null) {
            dragSource.addDragSourceMotionListener(this);
            dragSource.createDefaultDragGestureRecognizer(dragComponent, DnDConstants.ACTION_MOVE, this::startDrag);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    private void startDrag(DragGestureEvent dragGestureEvent) {
        // if there is already a floating panel, don't float this one
        if (Floating.isFloating()) {
            return;
        }

        try {
            dragSource.startDrag(dragGestureEvent, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), new StringSelection(""), this);
        }
        catch (InvalidDnDOperationException ignored) {
            // someone beat us to it
            return;
        }
        dragStarted(dragGestureEvent.getDragOrigin());
    }

    private void dragStarted(Point dragOrigin) {
        Floating.setFloating(true);

        dragComponentDragOffset = dragOrigin;

        // force the drag offset to be inset from the edge slightly
        dragComponentDragOffset.y = Math.max(5, dragComponentDragOffset.y);
        dragComponentDragOffset.x = Math.max(5, dragComponentDragOffset.x);

        originalWindow = getOriginalWindow();

        originalWindowLayout = docking.getDockingState().getWindowLayout(originalWindow);

        floatingFrame = createFloatingFrame();

        undock();

        DockingComponentUtils.removeIllegalFloats(docking, originalWindow);

        RootDockingPanelAPI currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

        // TODO need to dispose original window if it is empty after the drags. but only hide it for now

        // TODO if the original window is not hidden, set it as our top and target and get the active util frame
    }

    public void removeListeners() {
        dragSource.removeDragSourceMotionListener(this);
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent event) {
        if (!Floating.isFloating()) {
            return;
        }
        updateFramePosition(event.getLocation());
    }

    private void updateFramePosition(Point mousePosOnScreen) {
        // update the frames position to our mouse position
        Point framePos = new Point(mousePosOnScreen.x - dragComponentDragOffset.x, mousePosOnScreen.y - dragComponentDragOffset.y);
        floatingFrame.setLocation(framePos);

        checkForFrameSwitch(mousePosOnScreen);
    }

    private void checkForFrameSwitch(Point mousePosOnScreen) {
        // find the frame at our current position
        Window frame = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);

        if (frame != currentWindow) {
            if (currentUtilFrame != null) {
                currentUtilFrame.deactivate();
            }

            currentWindow = frame;

            currentUtilFrame = Floating.frameForWindow(currentWindow);

            if (currentUtilFrame != null) {
                currentUtilFrame.activate(this, floatingFrame, dragSource, mousePosOnScreen);

                currentUtilFrame.toFront();
            }
        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent event) {
        if (!Floating.isFloating()) {
            return;
        }
        dropFloatingPanel();

        Floating.setFloating(false);
    }

    private void dropFloatingPanel() {
        if (!Floating.isFloating()) {
            return;
        }

        if (currentUtilFrame != null) {
            currentUtilFrame.deactivate();
        }

        floatingFrame.dispose();
    }

    protected abstract Window getOriginalWindow();

    protected abstract void undock();

    protected abstract JFrame createFloatingFrame();
}
