package ModernDocking.floating;

import ModernDocking.api.DockingAPI;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.internal.DockedTabbedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragGestureEvent;

public class DockedTabFloatListener extends DisplayPanelFloatListener {
    private final DockedTabbedPanel tabs;
    private final DisplayPanel displayPanel;

    public DockedTabFloatListener(DockingAPI docking, DockedTabbedPanel tabs, DisplayPanel displayPanel, JComponent dragComponent) {
        super(docking, displayPanel, dragComponent);
        this.tabs = tabs;
        this.displayPanel = displayPanel;
    }

    @Override
    protected boolean allowDrag(DragGestureEvent dragGestureEvent) {
        // if we're dragging from a tab then we need to use the normal drag event
        Point dragOrigin = new Point(dragGestureEvent.getDragOrigin());
        SwingUtilities.convertPointToScreen(dragOrigin, dragGestureEvent.getComponent());

        int targetTabIndex = tabs.getTargetTabIndex(dragOrigin);

        return targetTabIndex == tabs.getIndexOfPanel(displayPanel);
    }
}