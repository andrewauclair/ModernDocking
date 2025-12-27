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
package io.github.andrewauclair.moderndocking.api;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.DynamicDockableParameters;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.exception.DockableNotFoundException;
import io.github.andrewauclair.moderndocking.exception.RootDockingPanelNotFoundException;
import io.github.andrewauclair.moderndocking.internal.DockableProperties;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockedAnchorPanel;
import io.github.andrewauclair.moderndocking.internal.DockedSimplePanel;
import io.github.andrewauclair.moderndocking.internal.DockedSplitPanel;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.DockingListeners;
import io.github.andrewauclair.moderndocking.internal.DockingPanel;
import io.github.andrewauclair.moderndocking.internal.FailedDockable;
import io.github.andrewauclair.moderndocking.internal.FloatingFrame;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DefaultDynamicDockableCreationListener;
import io.github.andrewauclair.moderndocking.layouts.DockingAnchorPanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingLayoutNode;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.layouts.DockingSimplePanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingSplitPanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingTabPanelNode;
import io.github.andrewauclair.moderndocking.layouts.DynamicDockableCreationListener;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * Utility class to get layouts from existing windows and to restore layouts onto existing windows
 */
public class DockingStateAPI {
    private static final Logger logger = Logger.getLogger(DockingStateAPI.class.getPackageName());

    /**
     * cached layout for when a maximized dockable is minimized
     */
    public final Map<Window, WindowLayout> maximizeRestoreLayout = new HashMap<>();

    private final DockingAPI docking;

    private final DynamicDockableCreationListener defaultDynamicDockableCreation;
    private DynamicDockableCreationListener userDynamicDockableCreation = null;

    /**
     * Create a new instance for the given docking instance
     *
     * @param docking Docking instance this docking state belongs to
     */
    protected DockingStateAPI(DockingAPI docking) {
        this.docking = docking;
        defaultDynamicDockableCreation = new DefaultDynamicDockableCreationListener(docking);
    }

    /**
     * Get the current window layout of a window
     *
     * @param window The window to get a layout for
     *
     * @return The window layout
     */
    public WindowLayout getWindowLayout(Window window) {
        InternalRootDockingPanel root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RootDockingPanelNotFoundException(window);
        }

        WindowLayout maxLayout = maximizeRestoreLayout.get(window);

        if (maxLayout != null) {
            return maxLayout;
        }

        return DockingLayouts.layoutFromRoot(docking, root.getRootPanel());
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
            DockingComponentUtils.clearAnchors(window);

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

            SwingUtilities.invokeLater(() -> {
                DockingListeners.fireNewFloatingFrameEvent(frame, frame.getRoot());
            });
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
        InternalRootDockingPanel root = DockingComponentUtils.rootForWindow(docking, window);

        if (root == null) {
            throw new RootDockingPanelNotFoundException(window);
        }

        if (layout.hasSizeAndLocationInformation()) {
            if (layout.getState() != Frame.MAXIMIZED_BOTH) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] devices = env.getScreenDevices();

                boolean locationOnScreen = false;

                for (GraphicsDevice device : devices) {
                    if (device.getDefaultConfiguration().getBounds().contains(layout.getLocation())) {
                        locationOnScreen = true;
                    }
                }

                if (!locationOnScreen && devices.length > 0) {
                    layout.setLocation(devices[0].getDefaultConfiguration().getBounds().getLocation());
                }
            }

            window.setLocation(layout.getLocation());
            window.setSize(layout.getSize());

            if (window instanceof JFrame) {
                ((JFrame) window).setExtendedState(layout.getState());
            }
        }

        DockingComponentUtils.clearAnchors(root);

        DockingComponentUtils.undockComponents(docking, root);

        root.setPanel(restoreLayout(docking, layout.getRootNode(), window));

        // undock and destroy any failed dockables
        undockFailedComponents(docking, root);

        restoreProperSplitLocations(root.getRootPanel());

        for (String id : layout.getWestAutoHideToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableHidden(dockable, ToolbarLocation.WEST);
            root.hideHiddenPanels();
            getWrapper(dockable).setHidden(true);

            root.setSlidePosition(dockable, (int) (layout.slidePosition(id) * window.getWidth()));
        }

        for (String id : layout.getEastAutoHideToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableHidden(dockable, ToolbarLocation.EAST);
            root.hideHiddenPanels();
            getWrapper(dockable).setHidden(true);

            root.setSlidePosition(dockable, (int) (layout.slidePosition(id) * window.getHeight()));
        }

        for (String id : layout.getSouthAutoHideToolbarIDs()) {
            Dockable dockable = getDockable(docking, id);
            root.setDockableHidden(dockable, ToolbarLocation.SOUTH);
            root.hideHiddenPanels();
            getWrapper(dockable).setHidden(true);

            root.setSlidePosition(dockable, (int) (layout.slidePosition(id) * window.getHeight()));
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

    /**
     * Restore the layout of a single window, preserving the current size and position of the window
     *
     * @param window Window to restore the layout onto
     * @param layout The layout to restore
     */
    public void restoreWindowLayout_PreserveSizeAndPos(Window window, WindowLayout layout) {
        Point location = window.getLocation();
        Dimension size = window.getSize();

        restoreWindowLayout(window, layout);

        window.setLocation(location);
        window.setSize(size);
    }

    private DockingPanel restoreLayout(DockingAPI docking, DockingLayoutNode node, Window window) {
        if (node instanceof DockingSimplePanelNode) {
            return restoreSimple(docking, (DockingSimplePanelNode) node, window);
        }
        else if (node instanceof DockingSplitPanelNode) {
            return restoreSplit(docking, (DockingSplitPanelNode) node, window);
        }
        else if (node instanceof DockingTabPanelNode) {
            return restoreTabbed(docking, (DockingTabPanelNode) node, window);
        }
        else if (node instanceof DockingAnchorPanelNode) {
            return restoreAnchor(docking, (DockingAnchorPanelNode) node, window);
        }
        else if (node == null) {
            // the main window root can contain a null panel if nothing is docked
            return null;
        }
        else {
            throw new RuntimeException("Unknown state type");
        }
    }

    private DockedSplitPanel restoreSplit(DockingAPI docking, DockingSplitPanelNode node, Window window) {
        DockedSplitPanel panel = new DockedSplitPanel(docking, window, "");

        panel.setLeft(restoreLayout(docking, node.getLeft(), window));
        panel.setRight(restoreLayout(docking, node.getRight(), window));
        panel.setOrientation(node.getOrientation());
        panel.setDividerLocation(node.getDividerProportion());

        return panel;
    }

    private DockingPanel restoreTabbed(DockingAPI docking, DockingTabPanelNode node, Window window) {
        DockedTabbedPanel panel = null;

        for (DockingSimplePanelNode simpleNode : node.getPersistentIDs()) {
            Dockable dockable = getDockable(docking, simpleNode.getPersistentID());

            if (dockable instanceof FailedDockable) {
                dockable = createDynamicDockable(dockable, simpleNode.getPersistentID(), simpleNode.getClassName(), simpleNode.getTitleText(), simpleNode.getTabText(), simpleNode.getProperties());
            }

            if (dockable == null) {
                throw new DockableNotFoundException(simpleNode.getPersistentID());
            }

            DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

            DockableProperties.configureProperties(wrapper, simpleNode.getProperties());

            docking.undock(dockable);

            wrapper.setWindow(window);

            if (panel == null) {
                panel = new DockedTabbedPanel(docking, wrapper, node.getAnchor());
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

    private DockingPanel restoreAnchor(DockingAPI docking, DockingAnchorPanelNode node, Window window) {
        Dockable dockable = getDockable(docking, node.getPersistentID());

        if (dockable instanceof FailedDockable) {
            dockable = createDynamicDockable(dockable, node.getPersistentID(), node.getClassName(), "", "", Collections.emptyMap());
        }

        if (dockable == null) {
            throw new DockableNotFoundException(node.getPersistentID());
        }

        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

        // undock the dockable in case it is currently docked somewhere else
        docking.undock(dockable);

        wrapper.setWindow(window);

        return new DockedAnchorPanel(docking, wrapper);
    }

    private DockingPanel restoreSimple(DockingAPI docking, DockingSimplePanelNode node, Window window) {
        Dockable dockable = getDockable(docking, node.getPersistentID());

        if (dockable instanceof FailedDockable) {
            dockable = createDynamicDockable(dockable, node.getPersistentID(), node.getClassName(), node.getTitleText(), node.getTabText(), node.getProperties());
        }

        if (dockable == null) {
            throw new DockableNotFoundException(node.getPersistentID());
        }

        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

        DockableProperties.configureProperties(wrapper, node.getProperties());

        // undock the dockable in case it is currently docked somewhere else
        docking.undock(dockable);

        wrapper.setWindow(window);

        if (wrapper.isAnchor()) {
            return new DockedAnchorPanel(docking, wrapper);
        }

        if (Settings.alwaysDisplayTabsMode() || dockable.getTabPreference() == DockableTabPreference.TOP) {
            return new DockedTabbedPanel(docking, wrapper, node.getAnchor());
        }
        return new DockedSimplePanel(docking, wrapper, node.getAnchor());
    }

    private Dockable createDynamicDockable(Dockable dockable, String persistentID, String className, String titleText, String tabText, Map<String, Property> properties) {
        // the failed dockable is registered with the persistentID we want to use
        docking.deregisterDockable(dockable);

        dockable = null;

        if (userDynamicDockableCreation != null) {
            dockable = userDynamicDockableCreation.createDockable(persistentID, className, titleText, tabText, properties);
        }

        if (dockable == null) {
            dockable = defaultDynamicDockableCreation.createDockable(persistentID, className, titleText, tabText, properties);
        }

        return dockable;
//        boolean foundNewConstructor = false;
//
//        try {
//            Class<?> aClass = Class.forName(className);
//            Constructor<?> constructor = aClass.getConstructor(DynamicDockableParameters.class);
//
//            // the failed dockable is registered with the persistentID we want to use
//            docking.deregisterDockable(dockable);
//
//            constructor.newInstance(new DynamicDockableParameters(persistentID, tabText, titleText));
//
//            foundNewConstructor = true;
//        }
//        catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
//               InvocationTargetException e) {
//            logger.log(Level.INFO, "Failed to create instance of dynamic dockable with DynamicDockableParameters constructor. Falling back on (String, String)");
//            logger.log(Level.INFO, e.getMessage(), e);
//        }
//
//        if (!foundNewConstructor) {
//            try {
//                Class<?> aClass = Class.forName(className);
//                Constructor<?> constructor = aClass.getConstructor(String.class, String.class);
//
//                // the failed dockable is registered with the persistentID we want to use
//                docking.deregisterDockable(dockable);
//
//                // create the instance, this should register the dockable and let us look it up
//                constructor.newInstance(persistentID, persistentID);
//            }
//            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
//                     InvocationTargetException e) {
//                logger.log(Level.INFO, e.getMessage(), e);
//                return null;
//            }
//        }
//
//        dockable = getDockable(docking, persistentID);
//
//        if (dockable instanceof FailedDockable) {
//            return null;
//        }
//        return dockable;
    }

    private Dockable getDockable(DockingAPI docking, String persistentID) {
        try {
            return DockingInternal.get(docking).getDockable(persistentID);
        }
        catch (DockableNotFoundException ignore) {
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

    public void setUserDynamicDockableCreationListener(DynamicDockableCreationListener userDynamicDockableCreation) {
        this.userDynamicDockableCreation = userDynamicDockableCreation;
    }
}
