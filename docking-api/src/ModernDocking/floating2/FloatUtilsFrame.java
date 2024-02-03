package ModernDocking.floating2;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;

public class FloatUtilsFrame extends JFrame implements DragSourceMotionListener {
    private DragSource dragSource;

    public FloatUtilsFrame(DockingAPI docking, Window referenceDockingWindow, RootDockingPanelAPI root) {

    }

    public void activate(DragSource dragSource) {
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);
    }

    public void deactivate() {
        dragSource.removeDragSourceMotionListener(this);
        dragSource = null;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent dsde) {

    }
}
