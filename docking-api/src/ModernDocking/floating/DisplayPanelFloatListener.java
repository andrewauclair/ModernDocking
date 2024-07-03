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
import ModernDocking.internal.*;
import ModernDocking.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.util.Collections;

public class DisplayPanelFloatListener extends FloatListener {
    private final DockingAPI docking;
    private final DisplayPanel panel;

    public DisplayPanelFloatListener(DockingAPI docking, DisplayPanel panel) {
        super(docking, panel);
        this.docking = docking;
        this.panel = panel;
    }

    public DisplayPanelFloatListener(DockingAPI docking, DisplayPanel panel, JComponent dragComponent) {
        super(docking, panel, dragComponent);
        this.docking = docking;
        this.panel = panel;
    }

    @Override
    protected boolean allowDrag(DragGestureEvent dragGestureEvent) {
        return true;
    }

    public Dockable getDockable() {
        return panel.getWrapper().getDockable();
    }

    @Override
    protected Window getOriginalWindow() {
        return panel.getWrapper().getWindow();
    }

    @Override
    protected void undock() {
        docking.undock(panel.getWrapper().getDockable());
    }

    @Override
    protected JFrame createFloatingFrame() {
        if (Settings.alwaysDisplayTabsMode(panel.getWrapper().getDockable())) {
            return new TempFloatingFrame(Collections.singletonList(panel.getWrapper()), 0, panel, panel.getSize());
        }
        return new TempFloatingFrame(panel.getWrapper(), panel, panel.getSize());
    }

    @Override
    protected boolean dropPanel(FloatUtilsFrame utilsFrame, JFrame floatingFrame, Point mousePosOnScreen) {
        DockableWrapper floatingDockable = panel.getWrapper();

        if (utilsFrame != null) {
            Window targetWindow = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);
            InternalRootDockingPanel root = DockingComponentUtils.rootForWindow(docking, targetWindow);

            Dockable dockableAtPos = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, targetWindow);

            if (utilsFrame.isOverRootHandle()) {
                docking.dock(floatingDockable.getDockable(), targetWindow, utilsFrame.rootHandleRegion());
            }
            else if (utilsFrame.isOverDockableHandle()) {
                docking.dock(floatingDockable.getDockable(), dockableAtPos, utilsFrame.dockableHandle());
            }
            else if (utilsFrame.isOverPinHandle()) {
                docking.unpinDockable(floatingDockable.getDockable(), utilsFrame.pinRegion(), targetWindow, root.getRootPanel());
            }
            else if (utilsFrame.isOverTab()) {
                CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, targetWindow);

                DockedTabbedPanel dockingTabPanel = (DockedTabbedPanel) DockingComponentUtils.findDockingPanelAtScreenPos(mousePosOnScreen, targetWindow);

                if (tabbedPane != null && dockingTabPanel != null) {
                    int tabIndex = tabbedPane.getTargetTabIndex(mousePosOnScreen, true);

                    if (tabIndex == -1) {
                        dockingTabPanel.dock(floatingDockable.getDockable(), DockingRegion.CENTER, 1.0);
                    }
                    else {
                        dockingTabPanel.dockAtIndex(floatingDockable.getDockable(), tabIndex);
                    }
                }
                else {
                    // failed to dock, restore the previous layout
                    return false;
                }
            }
            else if (dockableAtPos != null) {
                // docking to a dockable region
                DockingRegion region = utilsFrame.getDockableRegion(dockableAtPos, panel.getWrapper().getDockable(), mousePosOnScreen);

                docking.dock(floatingDockable.getDockable(), dockableAtPos, region);
            }
            else if (floatingDockable.getDockable().isFloatingAllowed()) {
                // floating
                FloatingFrame newFloatingFrame = new FloatingFrame(docking, floatingDockable.getDockable(), mousePosOnScreen, floatingDockable.getDisplayPanel().getSize(), 0);
                docking.dock(floatingDockable.getDockable(), newFloatingFrame);
            }
            else {
                // failed to dock, restore the previous layout
                return false;
            }
        }
        else if (floatingDockable.getDockable().isFloatingAllowed()) {
            // floating
            FloatingFrame newFloatingFrame = new FloatingFrame(docking, floatingDockable.getDockable(), mousePosOnScreen, floatingDockable.getDisplayPanel().getSize(), 0);
            docking.dock(floatingDockable.getDockable(), newFloatingFrame);
        }
        else {
            // failed to dock, restore the previous layout
            return false;
        }
        return true;
    }
}