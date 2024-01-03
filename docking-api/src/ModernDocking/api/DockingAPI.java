/*
Copyright (c) 2023 Andrew Auclair

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
import ModernDocking.DockableStyle;
import ModernDocking.DockingRegion;
import ModernDocking.event.DockingListener;
import ModernDocking.event.MaximizeListener;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.exception.NotDockedException;
import ModernDocking.floating.FloatListener;
import ModernDocking.internal.*;
import ModernDocking.layouts.WindowLayout;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Single instance of the docking framework. Useful when a single JVM is to host multiple instances of an application
 * and docking should be handled separately for each of them
 */
public class DockingAPI {
    private final DockingInternal internals = new DockingInternal(this);

    // the applications main frame
    private Window mainWindow;

    // this may look unused, but we need to create an instance of it to make it work
    private final ActiveDockableHighlighter activeDockableHighlighter = new ActiveDockableHighlighter(this);

    private final AppStatePersister appStatePersister = new AppStatePersister(this);

    // map of all the root panels in the application
    private  final Map<Window, RootDockingPanelAPI> rootPanels = new HashMap<>();

    private final AppStateAPI appState = new AppStateAPI(this);
    private final DockingStateAPI dockingState = new DockingStateAPI(this);

    private final LayoutPersistenceAPI layoutPersistence = new LayoutPersistenceAPI(this);

    // listen for L&F changes so that we can update dockable panels properly when not displayed
    private final PropertyChangeListener propertyChangeListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            SwingUtilities.invokeLater(internals::updateLAF);
        }
    };

    private boolean deregistering = false;

    public AppStateAPI getAppState() {
        return appState;
    }

    public DockingStateAPI getDockingState() {
        return dockingState;
    }

    public LayoutPersistenceAPI getLayoutPersistence() {
        return layoutPersistence;
    }

    protected DockingAPI(Window mainWindow) {
        this.mainWindow = mainWindow;

        UIManager.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Uninitialize the docking framework so that it can be initialized again with a new window
     */
    public void uninitialize() {
        // deregister all dockables and panels
        deregisterAllDockables();
        deregisterAllDockingPanels();

        // remove reference to window so it can be cleaned up
        mainWindow = null;

        activeDockableHighlighter.removeListeners();

        UIManager.removePropertyChangeListener(propertyChangeListener);

        DockingInternal.remove(this);
    }

    /**
     * Get a map of RootDockingPanels to their Windows
     *
     * @return map of root panels
     */
    public Map<Window, RootDockingPanelAPI> getRootPanels() {
        return rootPanels;
    }

    /**
     * Get the main window instance
     *
     * @return main window
     */
    public Window getMainWindow() {
        return mainWindow;
    }

    /**
     * register a dockable with the framework
     *
     * @param dockable Dockable to register
     */
    public void registerDockable(Dockable dockable) {
        internals.registerDockable(dockable);
    }

    /**
     * Check if a dockable has already been registered
     *
     * @param persistentID The persistent ID to look for
     */
    public boolean isDockableRegistered(String persistentID) {
        return getDockables().stream().anyMatch(dockable -> dockable.getPersistentID().equals(persistentID));
    }

    /**
     * Dockables must be deregistered so it can be properly disposed
     *
     * @param dockable Dockable to deregister
     */
    public void deregisterDockable(Dockable dockable) {
        deregistering = true;

        try {
            // make sure we undock the dockable from the UI before deregistering
            undock(dockable);

            internals.deregisterDockable(dockable);
        }
        finally {
            deregistering = false;
        }
    }

    /**
     * Deregister all dockables that have been registered. This action will also undock all dockables.
     */
    public void deregisterAllDockables() {
        deregistering = true;

        try {
            Set<Window> windows = new HashSet<>(getRootPanels().keySet());

            for (Window window : windows) {
                DockingComponentUtils.undockComponents(this, window);

                // only dispose this window if we created it
                if (window instanceof FloatingFrame) {
                    window.dispose();
                }
            }

            for (Dockable dockable : internals.getDockables()) {
                deregisterDockable(dockable);
            }
        }
        finally {
            deregistering = false;
        }
    }

    public List<Dockable> getDockables() {
        return internals.getDockables();
    }

    /**
     * registration function for DockingPanel
     *
     * @param panel Panel to register
     * @param parent The parent frame of the panel
     */
    public void registerDockingPanel(RootDockingPanelAPI panel, JFrame parent) {
        if (rootPanels.containsKey(parent)) {
            throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
        }

        if (rootPanels.containsValue(panel)) {
            // we already checked above that we have this value
            //noinspection OptionalGetWithoutIsPresent
            Window window = rootPanels.entrySet().stream()
                    .filter(entry -> entry.getValue() == panel)
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .get();

            throw new DockableRegistrationFailureException("RootDockingPanel already registered for window: " + window);
        }

        rootPanels.put(parent, panel);
        FloatListener.registerDockingWindow(this, parent, panel);

        appStatePersister.addWindow(parent);
    }

    /**
     * Register a RootDockingPanel
     *
     * @param panel RootDockingPanel to register
     * @param parent The parent JDialog of the panel
     */
    public void registerDockingPanel(RootDockingPanelAPI panel, JDialog parent) {
        if (rootPanels.containsKey(parent)) {
            throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
        }

        if (rootPanels.containsValue(panel)) {
            // we already checked above that we have this value
            //noinspection OptionalGetWithoutIsPresent
            Window window = rootPanels.entrySet().stream()
                    .filter(entry -> entry.getValue() == panel)
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .get();

            throw new DockableRegistrationFailureException("RootDockingPanel already registered for window: " + window);
        }

        rootPanels.put(parent, panel);
        FloatListener.registerDockingWindow(this, parent, panel);

        appStatePersister.addWindow(parent);
    }

    /**
     * Deregister a docking root panel
     *
     * @param parent The parent of the panel that we're deregistering
     */
    public void deregisterDockingPanel(Window parent) {
        if (rootPanels.containsKey(parent)) {
            RootDockingPanelAPI root = rootPanels.get(parent);

            DockingComponentUtils.undockComponents(this, root);
        }

        rootPanels.remove(parent);
        FloatListener.deregisterDockingWindow(parent);

        appStatePersister.removeWindow(parent);
    }

    /**
     * Deregister all registered panels. Additionally, dispose any windows created by Modern Docking.
     */
    public void deregisterAllDockingPanels() {
        Set<Window> windows = new HashSet<>(getRootPanels().keySet());

        for (Window window : windows) {
            deregisterDockingPanel(window);

            // only dispose this window if we created it
            if (window instanceof FloatingFrame) {
                window.dispose();
            }
        }
    }

    /**
     * allows the user to configure pinning per window. by default pinning is only enabled on the frames the docking framework creates
     *
     * @param window The window to configure pinning on
     * @param layer The layout to use for pinning in the JLayeredPane
     * @param allow Whether pinning is allowed on this Window
     */
    public void configurePinning(Window window, int layer, boolean allow) {
        if (!rootPanels.containsKey(window)) {
            throw new DockableRegistrationFailureException("No root panel for window has been registered.");
        }

        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);
        root.setPinningSupported(allow);
        root.setPinningLayer(layer);
    }

    /**
     * Check if pinning is allowed for a dockable
     *
     * @param dockable Dockable to check
     * @return Whether the dockable can be pinned
     */
    public boolean pinningAllowed(Dockable dockable) {
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, DockingComponentUtils.findWindowForDockable(this, dockable));

        return dockable.isPinningAllowed() && root.isPinningSupported();
    }

    /**
     * docks a dockable to the center of the given window
     * <p>
     * NOTE: This will only work if the window root docking node is empty. Otherwise, this does nothing.
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     */
    public void dock(String persistentID, Window window) {
        dock(internals.getDockable(persistentID), window, DockingRegion.CENTER);
    }

    /**
     * docks a dockable to the center of the given window
     * <p>
     * NOTE: This will only work if the window root docking node is empty. Otherwise, this does nothing.
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     */
    public void dock(Dockable dockable, Window window) {
        dock(dockable, window, DockingRegion.CENTER);
    }

    /**
     * docks a dockable into the specified region of the root of the window with 25% divider proportion
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     */
    public void dock(String persistentID, Window window, DockingRegion region) {
        dock(internals.getDockable(persistentID), window, region, 0.25);
    }

    /**
     * docks a dockable into the specified region of the root of the window with 25% divider proportion
     * <p>
     * NOTE: Nothing will be done if docking to the CENTER of the window and the window root panel is not empty
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     */
    public void dock(Dockable dockable, Window window, DockingRegion region) {
        RootDockingPanelAPI root = rootPanels.get(window);

        if (root == null) {
            throw new DockableRegistrationFailureException("Window does not have a RootDockingPanel: " + window);
        }

        if (!root.isEmpty() && region == DockingRegion.CENTER) {
            // can't dock here, so stop
            return;
        }

        dock(dockable, window, region, 0.25);
    }

    /**
     * docks a dockable into the specified region of the window with the specified divider proportion
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public void dock(String persistentID, Window window, DockingRegion region, double dividerProportion) {
        dock(internals.getDockable(persistentID), window, region, dividerProportion);
    }

    /**
     * docks a dockable into the specified region of the window with the specified divider proportion
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public void dock(Dockable dockable, Window window, DockingRegion region, double dividerProportion) {
        RootDockingPanelAPI root = rootPanels.get(window);

        if (root == null) {
            throw new DockableRegistrationFailureException("Window does not have a RootDockingPanel: " + window);
        }

        // if the source is already docked we need to undock it before docking it again, otherwise we might steal it from its UI parent
        if (isDocked(dockable)) {
            DockableWrapper wrapper = internals.getWrapper(dockable);

            wrapper.getParent().undock(dockable);

            // don't fire an undocked event for this one
        }

        root.dock(dockable, region, dividerProportion);

        internals.getWrapper(dockable).setWindow(window);

        // fire a docked event when the component is actually added
        DockingListeners.fireDockedEvent(dockable);

        appState.persist();
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     */
    public void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region) {
        dock(internals.getDockable(sourcePersistentID), internals.getDockable(targetPersistentID), region, 0.5);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     */
    public void dock(String sourcePersistentID, Dockable target, DockingRegion region) {
        dock(internals.getDockable(sourcePersistentID), target, region, 0.5);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     */
    public void dock(Dockable source, String targetPersistentID, DockingRegion region) {
        dock(source, internals.getDockable(targetPersistentID), region, 0.5);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     */
    public void dock(Dockable source, Dockable target, DockingRegion region) {
        dock(source, target, region, 0.5);
    }

    /**
     * docks the target to the source in the specified region with the specified divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region, double dividerProportion) {
        dock(internals.getDockable(sourcePersistentID), internals.getDockable(targetPersistentID), region, dividerProportion);
    }

    /**
     * docks the target to the source in the specified region with the specified divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion) {
        if (!isDocked(target)) {
            throw new NotDockedException(target);
        }

        // if the source is already docked we need to undock it before docking it again, otherwise we might steal it from its UI parent
        if (isDocked(source)) {
            DockableWrapper wrapper = internals.getWrapper(source);

            wrapper.getParent().undock(source);
        }

        DockableWrapper wrapper = internals.getWrapper(target);

        wrapper.getParent().dock(source, region, dividerProportion);

        internals.getWrapper(source).setWindow(wrapper.getWindow());

        DockingListeners.fireDockedEvent(source);

        appState.persist();
    }

    /**
     * create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param persistentID The persistent ID of the dockable to float in a new window
     */
    public void newWindow(String persistentID) {
        newWindow(internals.getDockable(persistentID));
    }

    /**
     * create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param dockable The dockable to float in a new window
     */
    public void newWindow(Dockable dockable) {
        DisplayPanel displayPanel = internals.getWrapper(dockable).getDisplayPanel();

        if (isDocked(dockable)) {
            Point location = displayPanel.getLocationOnScreen();
            Dimension size = displayPanel.getSize();

            newWindow(dockable, location, size);
        }
        else {
            FloatingFrame frame = new FloatingFrame(this);

            dock(dockable, frame);

            frame.pack();
            frame.setLocationRelativeTo(getMainWindow());
        }
    }

    /**
     * Create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param persistentID The persistent ID of the dockable to float in a new window
     * @param location The screen location to display the new frame at
     * @param size The size of the new frame
     */
    public void newWindow(String persistentID, Point location, Dimension size) {
        newWindow(internals.getDockable(persistentID), location, size);
    }

    /**
     * Create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param dockable The dockable to float in a new window
     * @param location The screen location to display the new frame at
     * @param size The size of the new frame
     */
    public void newWindow(Dockable dockable, Point location, Dimension size) {
        FloatingFrame frame = new FloatingFrame(this, dockable, location, size, JFrame.NORMAL);

        undock(dockable);
        dock(dockable, frame);

        SwingUtilities.invokeLater(() -> bringToFront(dockable));
    }

    /**
     * bring the specified dockable to the front if it is in a tabbed panel
     *
     * @param dockable Dockable to bring to the front
     */
    public void bringToFront(Dockable dockable) {
        if (!isDocked(dockable)) {
            throw new NotDockedException(dockable);
        }

        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);

        if (window instanceof JFrame && ((JFrame) window).getState() == JFrame.ICONIFIED) {
            ((JFrame)window).setState(JFrame.NORMAL);
        }

        window.setAlwaysOnTop(true);
        window.setAlwaysOnTop(false);

        if (internals.getWrapper(dockable).getParent() instanceof DockedTabbedPanel) {
            DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) internals.getWrapper(dockable).getParent();
            tabbedPanel.bringToFront(dockable);
        }
    }

    /**
     * undock a dockable
     *
     * @param persistentID The persistentID of the dockable to undock
     */
    public void undock(String persistentID) {
        undock(internals.getDockable(persistentID));
    }

    /**
     * undock a dockable
     *
     * @param dockable The dockable to undock
     */
    public void undock(Dockable dockable) {
        if (!isDocked(dockable)) {
            // nothing to undock
            return;
        }

        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);

        Objects.requireNonNull(window);

        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);

        Objects.requireNonNull(root);

        DockableWrapper wrapper = internals.getWrapper(dockable);

        wrapper.setRoot(root);

        if (isUnpinned(dockable)) {
            root.undock(dockable);
            wrapper.setParent(null);
            wrapper.setUnpinned(false);
        }
        else {
            wrapper.getParent().undock(dockable);
        }
        wrapper.setWindow(null);

        DockingListeners.fireUndockedEvent(dockable);

        // make sure that can dispose this window, and we're not floating the last dockable in it
        if (canDisposeWindow(window) && root.isEmpty() && !FloatListener.isFloating()) {
            deregisterDockingPanel(window);
            window.dispose();
        }

        appState.persist();

        // force this dockable to dock again if we're not floating it
        if (!dockable.isClosable() && !FloatListener.isFloating() && !deregistering) {
            dock(dockable, mainWindow);
        }
    }

    /**
     * check if a dockable is currently docked
     *
     * @param persistentID The persistentID of the dockable to check
     * @return Whether the dockable is docked
     */
    public boolean isDocked(String persistentID) {
        return isDocked(internals.getDockable(persistentID));
    }

    /**
     * check if a dockable is currently docked
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is docked
     */
    public boolean isDocked(Dockable dockable) {
        return internals.getWrapper(dockable).getParent() != null;
    }

    /**
     * check if a dockable is currently in the unpinned state
     *
     * @param persistentID The persistentID of the dockable to check
     * @return Whether the dockable is unpinned
     */
    public boolean isUnpinned(String persistentID) {
        return isUnpinned(internals.getDockable(persistentID));
    }

    /**
     * check if a dockable is currently in the unpinned state
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is unpinned
     */
    public boolean isUnpinned(Dockable dockable) {
        return internals.getWrapper(dockable).isUnpinned();
    }

    /**
     * check if the window can be disposed. Windows can be disposed if they are not the main window and are not maximized
     *
     * @param window Window to check
     * @return Boolean indicating if the specified Window can be disposed
     */
    public boolean canDisposeWindow(Window window) {
        // don't dispose of any docking windows that are JDialogs
        if (window instanceof JDialog) {
            return false;
        }

        if (dockingState.maximizeRestoreLayout.containsKey(window)) {
            return false;
        }

        // only dispose this window if we created it
        return window instanceof FloatingFrame;
    }

    /**
     * checks if a dockable is currently maximized
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is maximized
     */
    public boolean isMaximized(Dockable dockable) {
        return internals.getWrapper(dockable).isMaximized();
    }

    /**
     * maximizes a dockable
     *
     * @param dockable Dockable to maximize
     */
    public void maximize(Dockable dockable) {
        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);

        // can only maximize one panel per root
        if (!dockingState.maximizeRestoreLayout.containsKey(window) && root != null) {
            internals.getWrapper(dockable).setMaximized(true);
            DockingListeners.fireMaximizeEvent(dockable, true);

            WindowLayout layout = dockingState.getWindowLayout(window);
            layout.setMaximizedDockable(dockable.getPersistentID());

            dockingState.maximizeRestoreLayout.put(window, layout);

            DockingComponentUtils.undockComponents(this, root);

            dock(dockable, window);
        }
    }

    /**
     * minimize a dockable if it is currently maximized
     *
     * @param dockable Dockable to minimize
     */
    public void minimize(Dockable dockable) {
        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);

        // can only minimize if already maximized
        if (dockingState.maximizeRestoreLayout.containsKey(window)) {
            internals.getWrapper(dockable).setMaximized(false);
            DockingListeners.fireMaximizeEvent(dockable, false);

            dockingState.restoreWindowLayout(window, dockingState.maximizeRestoreLayout.get(window));

            dockingState.maximizeRestoreLayout.remove(window);

            internals.fireDockedEventForFrame(window);
        }
    }

    /**
     * pin a dockable. only valid if the dockable is unpinned
     *
     * @param dockable Dockable to pin
     */
    public void pinDockable(Dockable dockable) {
        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);

        if (internals.getWrapper(dockable).isUnpinned()) {
            root.setDockablePinned(dockable);

            internals.getWrapper(dockable).setUnpinned(false);

            DockingListeners.firePinnedEvent(dockable);
        }
    }

    /**
     * unpin a dockable. only valid if the dockable is pinned
     * @param dockable Dockable to unpin
     */
    public void unpinDockable(Dockable dockable) {
        if (isUnpinned(dockable)) {
            return;
        }

        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);

        Component component = (Component) dockable;

        Point posInFrame = component.getLocation();
        SwingUtilities.convertPointToScreen(posInFrame, component.getParent());
        SwingUtilities.convertPointFromScreen(posInFrame, root);

        posInFrame.x += component.getWidth() / 2;
        posInFrame.y += component.getHeight() / 2;

        boolean allowedSouth = dockable.getPinningStyle() == DockableStyle.BOTH || dockable.getPinningStyle() == DockableStyle.HORIZONTAL;

        int westDist = posInFrame.x;
        int eastDist = window.getWidth() - posInFrame.x;
        int southDist = window.getHeight() - posInFrame.y;

        boolean east = eastDist <= westDist;
        boolean south = southDist < westDist && southDist < eastDist;

        ToolbarLocation location;

        if (south && allowedSouth) {
            location = ToolbarLocation.SOUTH;
        }
        else if (east) {
            location = ToolbarLocation.EAST;
        }
        else {
            location = ToolbarLocation.WEST;
        }

        unpinDockable(dockable, location);
    }

    /**
     * unpin a dockable. only valid if the dockable is pinned
     * @param dockable Dockable to unpin
     * @param location Toolbar location to unpin the dockable to
     */
    public void unpinDockable(Dockable dockable, ToolbarLocation location) {
        Window window = DockingComponentUtils.findWindowForDockable(this, dockable);
        RootDockingPanelAPI root = DockingComponentUtils.rootForWindow(this, window);

        unpinDockable(dockable, location, window, root);
    }

    /**
     * unpin a dockable. only valid if the dockable is pinned
     * @param dockable Dockable to unpin
     * @param location Toolbar location to unpin the dockable to
     */
    public void unpinDockable(Dockable dockable, ToolbarLocation location, Window window, RootDockingPanelAPI root) {
        if (isUnpinned(dockable)) {
            return;
        }

        Component component = (Component) dockable;

        Point posInFrame = component.getLocation();
        SwingUtilities.convertPointToScreen(posInFrame, component.getParent());
        SwingUtilities.convertPointFromScreen(posInFrame, root);

        posInFrame.x += component.getWidth() / 2;
        posInFrame.y += component.getHeight() / 2;

        if (!root.isPinningSupported()) {
            return;
        }
        undock(dockable);

        // reset the window, undocking the dockable sets it to null
        internals.getWrapper(dockable).setWindow(window);
        internals.getWrapper(dockable).setUnpinned(true);

        root.setDockableUnpinned(dockable, location);

        DockingListeners.fireUnpinnedEvent(dockable);
        DockingListeners.fireHiddenEvent(dockable);
    }

    /**
     * display a dockable
     *
     * @param persistentID The persistentID of the dockable to display
     */
    public void display(String persistentID) {
        display(internals.getDockable(persistentID));
    }

    /**
     * Display a dockable
     * <p>
     * if the dockable is already docked, then bringToFront is called.
     * if it is not docked, then dock is called, docking it with dockables of the same type
     *
     * @param dockable The dockable to display
     */
    public void display(Dockable dockable) {
        if (isDocked(dockable)) {
            bringToFront(dockable);
        }
        else {
            // go through all the dockables and find the first one that is the same type
            Optional<Dockable> firstOfType = DockingComponentUtils.findFirstDockableOfType(this, dockable.getType());

            if (firstOfType.isPresent()) {
                dock(dockable, firstOfType.get(), DockingRegion.CENTER);
            }
            else {
                // if we didn't find any dockables of the same type, we'll dock to north
                // TODO this is a bit boring. we should have a better way to do this
                dock(dockable, mainWindow, DockingRegion.NORTH);
            }
        }
    }

    /**
     * update the tab text on a dockable if it is in a tabbed panel
     *
     * @param persistentID The persistentID of the dockable to update
     */
    public void updateTabInfo(String persistentID) {
        updateTabInfo(internals.getDockable(persistentID));
    }

    /**
     * update the tab text on a dockable if it is in a tabbed panel
     *
     * @param dockable The dockable to update
     */
    public void updateTabInfo(Dockable dockable) {
        if (!isDocked(dockable)) {
            // if the dockable isn't docked then we don't have to do anything to update its tab text
            return;
        }

        DockableWrapper wrapper = internals.getWrapper(dockable);

        wrapper.getHeaderUI().update();

        DockingPanel parent = wrapper.getParent();

        if (parent instanceof DockedTabbedPanel) {
            ((DockedTabbedPanel) parent).updateTabInfo(dockable);
        }
    }

    /**
     * Add a new maximize listener. Will be called when a dockable is maximized
     *
     * @param listener Listener to add
     */
    public void addMaximizeListener(MaximizeListener listener) {
        DockingListeners.addMaximizeListener(listener);
    }

    /**
     * Remove a previously added maximize listener. No-op if the listener isn't in the list
     *
     * @param listener Listener to remove
     */
    public void removeMaximizeListener(MaximizeListener listener) {
        DockingListeners.removeMaximizeListener(listener);
    }

    public void addDockingListener(DockingListener listener) {
        DockingListeners.addDockingListener(listener);
    }

    public void removeDockingListener(DockingListener listener) {
        DockingListeners.removeDockingListener(listener);
    }
}
