/*
Copyright (c) 2022 Andrew Auclair

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
package ModernDocking.api;

import ModernDocking.Dockable;
import ModernDocking.exception.DockableNotFoundException;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.internal.*;
import ModernDocking.layouts.*;
import ModernDocking.persist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

public class DockingStateAPI {
    /**
     * cached layout for when a maximized dockable is minimized
     */
    public final Map<Window, WindowLayout> maximizeRestoreLayout = new HashMap<>();
    private final DockingAPI docking;

    protected DockingStateAPI(DockingAPI docking) {
        this.docking = docking;
    }

    public RootDockState getRootState(Window window) {
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RuntimeException("Root for window does not exist: " + window);
        }

        return new RootDockState(root);
    }

    public WindowLayout getWindowLayout(Window window) {
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RuntimeException("Root for frame does not exist: " + window);
        }

        WindowLayout maxLayout = maximizeRestoreLayout.get(window);

        if (maxLayout != null) {
            return maxLayout;
        }

        return DockingLayouts.layoutFromRoot(docking, root);
    }

    /**
     * Get the current application layout of the application
     *
     * @return Layout of the application
     */
    public ApplicationLayout getApplicationLayout() {
        ApplicationLayout layout = new ApplicationLayout();

        layout.setMainFrame(getWindowLayout(docking.getMainWindow()));

        for (Window frame : docking.getRootPanels().keySet()) {
            if (frame != docking.getMainWindow()) {
                layout.addFrame(getWindowLayout(frame));
            }
        }

        return layout;
    }

    /**
     * Restore the application layout, creating any necessary windows
     *
     * @param layout Application layout to restore
     */
    public void restoreApplicationLayout(ApplicationLayout layout) {
        // get rid of all existing windows and undock all dockables
        Set<Window> windows = new HashSet<>(docking.getRootPanels().keySet());
        for (Window window : windows) {
            DockingComponentUtils.undockComponents(docking, window);

            // only dispose this window if we created it
            if (window instanceof FloatingFrame) {
                window.dispose();
            }
        }

        docking.getAppState().setPaused(true);

        // setup main frame
        restoreWindowLayout(docking.getMainWindow(), layout.getMainFrameLayout());

        // setup rest of floating windows from layout
        for (WindowLayout frameLayout : layout.getFloatingFrameLayouts()) {
            FloatingFrame frame = new FloatingFrame(docking, frameLayout.getLocation(), frameLayout.getSize(), frameLayout.getState());

            restoreWindowLayout(frame, frameLayout);
        }

        docking.getAppState().setPaused(false);
        docking.getAppState().persist();

        DockingInternal.fireDockedEventForAll(docking);

        DockingLayouts.layoutRestored(layout);
    }

    /**
     * Restore the layout of a single window
     *
     * @param window Window to restore the layout onto
     * @param layout The layout to restore
     */
    public void restoreWindowLayout(Window window, WindowLayout layout) {
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RuntimeException("Root for window does not exist: " + window);
        }

        if (layout.hasSizeAndLocationInformation()) {
            window.setLocation(layout.getLocation());
            window.setSize(layout.getSize());

            if (window instanceof JFrame) {
                ((JFrame) window).setExtendedState(layout.getState());
            }
        }

        DockingComponentUtils.undockComponents(docking, root);

        root.setPanel(restoreState(docking, layout.getRootNode(), window));

        // undock and destroy any failed dockables
        undockFailedComponents(docking, root);

        restoreProperSplitLocations(root);

        for (String id : layout.getWestUnpinnedToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableUnpinned(dockable, DockableToolbar.Location.WEST);
            root.hideUnpinnedPanels();
            DockingInternal.get(docking).getWrapper(dockable).setUnpinned(true);
            getWrapper(dockable).setUnpinned(true);
        }

        for (String id : layout.getEastUnpinnedToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableUnpinned(dockable, DockableToolbar.Location.EAST);
            root.hideUnpinnedPanels();
            getWrapper(dockable).setUnpinned(true);
        }

        for (String id : layout.getSouthUnpinnedToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableUnpinned(dockable, DockableToolbar.Location.SOUTH);
            root.hideUnpinnedPanels();
            getWrapper(dockable).setUnpinned(true);
        }

        if (layout.getMaximizedDockable() != null) {
            docking.maximize(getDockable(docking, layout.getMaximizedDockable()));
        }
    }

    private void findSplitPanels(Container container, List<DockedSplitPanel> panels) {
        for (Component component : container.getComponents()) {
            if (component instanceof DockedSplitPanel) {
                panels.add((DockedSplitPanel) component);
            }

            if (component instanceof Container) {
                findSplitPanels((Container) component, panels);
            }
        }
    }

    public void restoreWindowLayout_PreserveSizeAndPos(Window window, WindowLayout layout) {
        Point location = window.getLocation();
        Dimension size = window.getSize();

        restoreWindowLayout(window, layout);

        window.setLocation(location);
        window.setSize(size);
    }

    public void restoreState(Window window, RootDockState state) {
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RuntimeException("Root for window does not exist: " + window);
        }

        DockingComponentUtils.undockComponents(docking, root);

        boolean paused = docking.getAppState().isPaused();
        docking.getAppState().setPaused(true);

        root.setPanel(restoreState(docking, state.getState(), window));

        restoreProperSplitLocations(root);

        docking.getAppState().setPaused(paused);

        if (!paused) {
            docking.getAppState().persist();
        }
    }

    private DockingPanel restoreState(DockingAPI docking, DockableState state, Window window) {
        if (state instanceof PanelState) {
            return restoreSimple(docking, (PanelState) state, window);
        } else if (state instanceof SplitState) {
            return restoreSplit(docking, (SplitState) state, window);
        } else if (state instanceof TabState) {
            return restoreTabbed(docking, (TabState) state, window);
        } else {
            throw new RuntimeException("Unknown state type");
        }
    }

    private DockedSplitPanel restoreSplit(DockingAPI docking, SplitState state, Window window) {
        DockedSplitPanel panel = new DockedSplitPanel(docking, window);

        panel.setLeft(restoreState(docking, state.getLeft(), window));
        panel.setRight(restoreState(docking, state.getRight(), window));
        panel.setOrientation(state.getOrientation());
        panel.setDividerLocation(state.getDividerProprtion());

        return panel;
    }

    private DockedTabbedPanel restoreTabbed(DockingAPI docking, TabState state, Window window) {
        DockedTabbedPanel panel = null;

        for (String persistentID : state.getPersistentIDs()) {
            Dockable dockable = getDockable(docking, persistentID);

            if (dockable == null) {
                throw new DockableNotFoundException(persistentID);
            }

            docking.undock(dockable);

            DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
            wrapper.setWindow(window);

            if (panel == null) {
                panel = new DockedTabbedPanel(docking, wrapper);
            } else {
                panel.addPanel(wrapper);
            }
        }

        if (panel == null) {
            throw new RuntimeException("DockedTabbedPanel has no tabs");
        }
        return panel;
    }

    private DockedSimplePanel restoreSimple(DockingAPI docking, PanelState state, Window window) {
        Dockable dockable = getDockable(docking, state.getPersistentID());

        if (dockable instanceof FailedDockable) {
            try {
                Class<?> aClass = Class.forName(state.getClassName());
                Constructor<?> constructor = aClass.getConstructor(String.class, String.class);

                docking.deregisterDockable(dockable);

                constructor.newInstance(state.getPersistentID(), state.getPersistentID());

                dockable = getDockable(docking, state.getPersistentID());
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                   InvocationTargetException ignore) {
            }
        }

        if (dockable == null) {
            throw new DockableNotFoundException(state.getPersistentID());
        }

        docking.undock(dockable);

        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
        wrapper.setWindow(window);

        return new DockedSimplePanel(docking, wrapper);
    }

    private DockingPanel restoreState(DockingAPI docking, DockingLayoutNode node, Window window) {
        if (node instanceof DockingSimplePanelNode) {
            return restoreSimple(docking, (DockingSimplePanelNode) node, window);
        } else if (node instanceof DockingSplitPanelNode) {
            return restoreSplit(docking, (DockingSplitPanelNode) node, window);
        } else if (node instanceof DockingTabPanelNode) {
            return restoreTabbed(docking, (DockingTabPanelNode) node, window);
        } else if (node == null) {
            // the main window root can contain a null panel if nothing is docked
            return null;
        } else {
            throw new RuntimeException("Unknown state type");
        }
    }

    private DockedSplitPanel restoreSplit(DockingAPI docking, DockingSplitPanelNode node, Window window) {
        DockedSplitPanel panel = new DockedSplitPanel(docking, window);

        panel.setLeft(restoreState(docking, node.getLeft(), window));
        panel.setRight(restoreState(docking, node.getRight(), window));
        panel.setOrientation(node.getOrientation());
        panel.setDividerLocation(node.getDividerProportion());

        return panel;
    }

    private DockedTabbedPanel restoreTabbed(DockingAPI docking, DockingTabPanelNode node, Window window) {
        DockedTabbedPanel panel = null;

        for (DockingSimplePanelNode simpleNode : node.getPersistentIDs()) {
            Dockable dockable = getDockable(docking, simpleNode.getPersistentID());

            if (dockable == null) {
                throw new DockableNotFoundException(simpleNode.getPersistentID());
            }

            DockableProperties.configureProperties(dockable, simpleNode.getProperties());

            docking.undock(dockable);

            DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
            wrapper.setWindow(window);

            if (panel == null) {
                panel = new DockedTabbedPanel(docking, wrapper);
            } else {
                panel.addPanel(wrapper);
            }
        }

        if (panel == null) {
            throw new RuntimeException("DockedTabbedPanel has no tabs");
        }

        if (!node.getSelectedTabID().isEmpty()) {
            panel.bringToFront(getDockable(docking, node.getSelectedTabID()));
        }

        return panel;
    }

    private DockedSimplePanel restoreSimple(DockingAPI docking, DockingSimplePanelNode node, Window window) {
        Dockable dockable = getDockable(docking, node.getPersistentID());

        if (dockable instanceof FailedDockable) {
            try {
                Class<?> aClass = Class.forName(node.getClassName());
                Constructor<?> constructor = aClass.getConstructor(String.class, String.class);

                docking.deregisterDockable(dockable);

                constructor.newInstance(node.getPersistentID(), node.getPersistentID());

                dockable = getDockable(docking, node.getPersistentID());
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ignore) {
            }
        }

        if (dockable == null) {
            throw new DockableNotFoundException(node.getPersistentID());
        }

        DockableProperties.configureProperties(dockable, node.getProperties());

        // undock the dockable in case it is currently docked somewhere else
        docking.undock(dockable);

        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
        wrapper.setWindow(window);

        return new DockedSimplePanel(docking, wrapper);
    }

    private Dockable getDockable(DockingAPI docking, String persistentID) {
        try {
            return DockingInternal.get(docking).getDockable(persistentID);
        } catch (DockableRegistrationFailureException ignore) {
        }
        return new FailedDockable(docking, persistentID);
    }

    private DockableWrapper getWrapper(Dockable dockable) {
        return DockingInternal.get(docking).getWrapper(dockable);
    }

    private void undockFailedComponents(DockingAPI docking, Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof FailedDockable) {
                FailedDockable dockable = (FailedDockable) component;
                docking.undock(getDockable(docking, dockable.getPersistentID()));
                dockable.destroy();
            } else if (component instanceof Container) {
                undockFailedComponents(docking, (Container) component);
            }
        }
    }

    private void restoreProperSplitLocations(RootDockingPanelAPI root) {
        SwingUtilities.invokeLater(() -> {
            // find all the splits and restore their divider locations from the bottom up
            List<DockedSplitPanel> splitPanels = new ArrayList<>();

            // find all the splits recursively. Pushing new splits onto the front of the deque. this forces the deepest
            // splits to be adjusted last, keeping their position proper.
            findSplitPanels(root, splitPanels);

            List<JSplitPane> splits = new ArrayList<>();
            List<Double> proportions = new ArrayList<>();

            // loop through and restore split proportions, bottom up
            for (DockedSplitPanel splitPanel : splitPanels) {
                splits.add(splitPanel.getSplitPane());
                proportions.add(splitPanel.getLastRequestedDividerProportion());
            }
            restoreSplits(splits, proportions);
        });
    }

    private void restoreSplits(List<JSplitPane> splits, List<Double> proportions) {
        if (splits.size() != proportions.size()) {
            return;
        }
        if (splits.isEmpty()) {
            return;
        }

        JSplitPane splitPane = splits.get(0);
        double proportion = proportions.get(0);

        splits.remove(0);
        proportions.remove(0);

        restoreSplit(splitPane, proportion, splits, proportions);
    }

	/**
	 * Restore all splits in the window, starting with the outer most splits and working our way in. Only moving to the next when the previous
	 * has been completely set.
	 *
	 * @param splitPane The current splitpane we're setting
	 * @param proportion The proportion of the splitpane
	 * @param splits The list of splitpanes left
	 * @param proportions The list of proportions for the remaining splitpanes
	 */
    private void restoreSplit(JSplitPane splitPane, double proportion, List<JSplitPane> splits, List<Double> proportions) {
        // calling setDividerLocation on a JSplitPane that isn't visible does nothing, so we need to check if it is showing first
        if (splitPane.isShowing()) {
            if (splitPane.getWidth() > 0 && splitPane.getHeight() > 0) {
                splitPane.setDividerLocation(proportion);

                if (!splits.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        JSplitPane nextSplit = splits.get(0);
                        double nextProportion = proportions.get(0);

                        splits.remove(0);
                        proportions.remove(0);

                        restoreSplit(nextSplit, nextProportion, splits, proportions);
                    });
                }
            } else {
                // split hasn't been completely calculated yet, wait until componentResize
                splitPane.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        // remove this listener, it's a one off
                        splitPane.removeComponentListener(this);
                        // call the function again, this time it should actually set the divider location
                        restoreSplit(splitPane, proportion, splits, proportions);
                    }
                });
            }
        } else {
            // split hasn't been shown yet, wait until it's showing
            splitPane.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    boolean isShowingChangeEvent = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;

                    if (isShowingChangeEvent && splitPane.isShowing()) {
                        // remove this listener, it's a one off
                        splitPane.removeHierarchyListener(this);
                        // call the function again, this time it might set the size or wait for componentResize
                        restoreSplit(splitPane, proportion, splits, proportions);
                    }
                }
            });
        }
    }
}
