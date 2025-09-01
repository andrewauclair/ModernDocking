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
package io.github.andrewauclair.moderndocking.internal.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.DockingListeners;
import io.github.andrewauclair.moderndocking.internal.FloatingFrame;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Listener for drag events on tab groups
 */
public class DockedTabbedPanelFloatListener extends FloatListener {
    private final DockingAPI docking;
    /**
     * The tabbed panel we're listening to
     */
    protected final DockedTabbedPanel tabs;

    /**
     * Create a new instance, tied to the tabbed panel
     *
     * @param docking The docking instance this listener belongs to
     * @param tabs The tabbed panel to listen for drags on
     * @param dragComponent The drag component to add the drag listener to
     */
    public DockedTabbedPanelFloatListener(DockingAPI docking, DockedTabbedPanel tabs, JComponent dragComponent) {
        super(docking, tabs, dragComponent);

        this.docking = docking;
        this.tabs = tabs;
    }

    @Override
    protected boolean allowDrag(DragGestureEvent dragGestureEvent) {
        // if we're dragging from a tab then we need to use the normal drag event
        Point dragOrigin = new Point(dragGestureEvent.getDragOrigin());
        SwingUtilities.convertPointToScreen(dragOrigin, dragGestureEvent.getComponent());

        int targetTabIndex = tabs.getTargetTabIndex(dragOrigin);

        return targetTabIndex == -1;
    }

    @Override
    protected Window getOriginalWindow() {
        return SwingUtilities.windowForComponent(tabs);
    }

    @Override
    protected void undock() {
        List<DockableWrapper> wrappers = new ArrayList<>(tabs.getDockables());

        for (DockableWrapper wrapper : wrappers) {
            docking.undock(wrapper.getDockable());
        }
    }

    @Override
    protected JFrame createFloatingFrame() {
        Floating.setFloatingTabbedPane(true);

        List<DockableWrapper> wrappers = new ArrayList<>(tabs.getDockables());

        return new TempFloatingFrame(wrappers, tabs.getSelectedTabIndex(), tabs, tabs.getSize());
    }

    @Override
    protected boolean dropPanel(FloatUtilsFrame utilsFrame, JFrame floatingFrame, Point mousePosOnScreen) {
        if (!(floatingFrame instanceof TempFloatingFrame)) {
            return false;
        }

        Floating.setFloatingTabbedPane(false);

        TempFloatingFrame tempFloatingFrame = (TempFloatingFrame) floatingFrame;

        List<DockableWrapper> dockables = new ArrayList<>(tempFloatingFrame.getDockables());
        Dockable selectedDockable = dockables.get(tempFloatingFrame.getSelectedIndex()).getDockable();

        if (utilsFrame == null) {
            for (DockableWrapper dockable : dockables) {
                // don't allow dockables that can't be closed or are limited to their window to move to another window
                if (dockable.getDockable().isLimitedToWindow() || !dockable.getDockable().isClosable()) {
                    return false;
                }
            }

            boolean first = true;
            Dockable firstDockable = null;
            FloatingFrame newFrame = null;

            for (DockableWrapper dockable : dockables) {
                if (first) {
                    first = false;
                    newFrame = new FloatingFrame(docking, dockable.getDockable(), tempFloatingFrame);
                    firstDockable = dockable.getDockable();
                }
                else {
                    docking.dock(dockable.getDockable(), firstDockable, DockingRegion.CENTER);
                }
            }

            if (selectedDockable != null) {
                docking.bringToFront(selectedDockable);
            }

            if (newFrame != null) {
                final FloatingFrame frame = newFrame;

                SwingUtilities.invokeLater(() -> {
                    DockingListeners.fireNewFloatingFrameEvent(frame, frame.getRoot());
                });
            }

            return true;
        }


        boolean first = true;
        Dockable firstDockable = null;

        Window targetWindow = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);
        Dockable dockableAtPos = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, targetWindow);
        DockingRegion region = dockableAtPos == null ? DockingRegion.CENTER : utilsFrame.getDockableRegion(dockableAtPos, null, mousePosOnScreen);

        if (utilsFrame.isOverDockableHandle()) {
            region = utilsFrame.dockableHandle();
        }

        for (DockableWrapper dockable : dockables) {
            // don't allow dockables that can't be closed or are limited to their window to move to another window
            if (targetWindow != originalWindow && (dockable.getDockable().isLimitedToWindow() || !dockable.getDockable().isClosable())) {
                return false;
            }

            if (first) {
                if (utilsFrame.isOverRootHandle()) {
                    docking.dock(dockable.getDockable(), targetWindow, utilsFrame.rootHandleRegion());
                }
                else if (dockableAtPos != null) {
                    docking.dock(dockable.getDockable(), dockableAtPos, region);
                }
                firstDockable = dockable.getDockable();
            }
            else {
                docking.dock(dockable.getDockable(), firstDockable, DockingRegion.CENTER);
            }
            first = false;
        }

        if (selectedDockable != null) {
            docking.bringToFront(selectedDockable);
        }

        return true;
    }
}