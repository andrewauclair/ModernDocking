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
import io.github.andrewauclair.moderndocking.internal.CustomTabbedPane;
import io.github.andrewauclair.moderndocking.internal.DisplayPanel;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.DockingListeners;
import io.github.andrewauclair.moderndocking.internal.FloatingFrame;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.settings.Settings;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Floating listener used for floating individual panels
 */
public class DisplayPanelFloatListener extends FloatListener {
    private final DockingAPI docking;
    private final DisplayPanel panel;

    /**
     * Create a new float listener for the specific display panel
     *
     * @param docking Docking instance
     * @param panel The display panel to listen to
     */
    public DisplayPanelFloatListener(DockingAPI docking, DisplayPanel panel) {
        super(docking, panel);
        this.docking = docking;
        this.panel = panel;
    }

    /**
     * Create a new float listener for the specific display panel
     *
     * @param docking Docking instance
     * @param panel The display panel to listen to
     * @param dragComponent The component to add the drag listener to
     */
    public DisplayPanelFloatListener(DockingAPI docking, DisplayPanel panel, JComponent dragComponent) {
        super(docking, panel, dragComponent);
        this.docking = docking;
        this.panel = panel;
    }

    @Override
    protected boolean allowDrag(DragGestureEvent dragGestureEvent) {
        return true;
    }

    /**
     * Get the dockable that this float listener is listening to for drag events
     *
     * @return Dockable this panel is tied to
     */
    public Dockable getDockable() {
        return panel.getWrapper().getDockable();
    }

    @Override
    protected Window getOriginalWindow() {
        return panel.getWrapper().getWindow();
    }

    @Override
    protected void undock() {
        DockingInternal.get(docking).undock(panel.getWrapper().getDockable(), true);
    }

    @Override
    protected JFrame createFloatingFrame() {
        if (Settings.alwaysDisplayTabsMode()) {
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

            // don't allow dockables that can't be closed or are limited to their window to move to another window
            if (targetWindow != originalWindow && (floatingDockable.getDockable().isLimitedToWindow() || !floatingDockable.getDockable().isClosable())) {
                return false;
            }

            if (utilsFrame.isOverRootHandle()) {
                docking.dock(floatingDockable.getDockable(), targetWindow, utilsFrame.rootHandleRegion());
            }
            else if (utilsFrame.isOverDockableHandle()) {
                docking.dock(floatingDockable.getDockable(), dockableAtPos, utilsFrame.dockableHandle());
            }
            else if (utilsFrame.isOverPinHandle()) {
                docking.autoHideDockable(floatingDockable.getDockable(), utilsFrame.pinRegion(), targetWindow);
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

                SwingUtilities.invokeLater(() -> {
                    docking.bringToFront(floatingDockable.getDockable());

                    DockingListeners.fireNewFloatingFrameEvent(newFloatingFrame, newFloatingFrame.getRoot(), floatingDockable.getDockable());
                });
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

            SwingUtilities.invokeLater(() -> {
                docking.bringToFront(floatingDockable.getDockable());

                DockingListeners.fireNewFloatingFrameEvent(newFloatingFrame, newFloatingFrame.getRoot(), floatingDockable.getDockable());
            });
        }
        else {
            // failed to dock, restore the previous layout
            return false;
        }
        return true;
    }
}