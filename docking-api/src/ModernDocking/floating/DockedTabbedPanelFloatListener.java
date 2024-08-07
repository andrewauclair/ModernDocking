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
import ModernDocking.internal.DockableWrapper;
import ModernDocking.internal.DockedTabbedPanel;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.internal.FloatingFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DockedTabbedPanelFloatListener extends FloatListener {
    private final DockingAPI docking;
    protected final DockedTabbedPanel tabs;

    private DisplayPanelFloatListener listener = null;

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
            boolean first = true;
            Dockable firstDockable = null;

            for (DockableWrapper dockable : dockables) {
                if (first) {
                    first = false;
                    new FloatingFrame(docking, dockable.getDockable(), tempFloatingFrame);
                    firstDockable = dockable.getDockable();
                }
                else {
                    docking.dock(dockable.getDockable(), firstDockable, DockingRegion.CENTER);
                }
            }

            if (selectedDockable != null) {
                docking.bringToFront(selectedDockable);
            }

            return true;
        }


        boolean first = true;
        Dockable firstDockable = null;

        Window targetFrame = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);
        Dockable dockableAtPos = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, targetFrame);
        DockingRegion region = dockableAtPos == null ? DockingRegion.CENTER : utilsFrame.getDockableRegion(dockableAtPos, null, mousePosOnScreen);

        if (utilsFrame.isOverDockableHandle()) {
            region = utilsFrame.dockableHandle();
        }

        for (DockableWrapper dockable : dockables) {
            if (first) {
                if (utilsFrame.isOverRootHandle()) {
                    docking.dock(dockable.getDockable(), targetFrame, utilsFrame.rootHandleRegion());
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