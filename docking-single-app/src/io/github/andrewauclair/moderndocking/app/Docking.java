/*
Copyright (c) 2022-2023 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.app;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import io.github.andrewauclair.moderndocking.event.MaximizeListener;
import io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.DockingListeners;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for apps that only need a single instance of the docking framework. Working with the static functions
 * is easier than passing an instance of the Docking class all over the app codebase.
 */
public class Docking {
    private static DockingAPI instance;

    /**
     * This class should not be instantiated
     */
    private Docking() {
    }

    /**
     * Create the one and only instance of the Docking class for the application
     * @param mainWindow The main window of the application
     */
    public static void initialize(Window mainWindow) {
        if (instance == null) {
            instance = new DockingAPI(mainWindow){};
        }
    }

    /**
     * Uninitialize the docking framework so that it can be initialized again with a new window
     */
    public static void uninitialize() {
        instance.uninitialize();
        instance = null;
    }

    /**
     * Get a map of RootDockingPanels to their Windows
     *
     * @return map of root panels
     */
    public static Map<Window, RootDockingPanelAPI> getRootPanels() {
        return instance.getRootPanels();
    }

    /**
     * Get the main window instance
     *
     * @return main window
     */
    public static Window getMainWindow() {
        return instance.getMainWindow();
    }

    /**
     * register a dockable with the framework
     *
     * @param dockable Dockable to register
     */
    public static void registerDockable(Dockable dockable) {
        instance.registerDockable(dockable);
    }

    /**
     * Check if a dockable has already been registered
     *
     * @param persistentID The persistent ID to look for
     *
     * @return Is the dockable registered?
     */
    public static boolean isDockableRegistered(String persistentID) {
        return instance.isDockableRegistered(persistentID);
    }

    /**
     * Dockables must be deregistered so it can be properly disposed
     *
     * @param dockable Dockable to deregister
     */
    public static void deregisterDockable(Dockable dockable) {
        instance.deregisterDockable(dockable);
    }

    /**
     * Deregister all dockables that have been registered. This action will also undock all dockables.
     */
    public static void deregisterAllDockables() {
        instance.deregisterAllDockables();
    }

    public static List<Dockable> getDockables() {
        return instance.getDockables();
    }

    /**
     * registration function for DockingPanel
     *
     * @param panel Panel to register
     * @param parent The parent frame of the panel
     */
    public static void registerDockingPanel(RootDockingPanelAPI panel, JFrame parent) {
        instance.registerDockingPanel(panel, parent);
    }

    /**
     * Register a RootDockingPanel
     *
     * @param panel RootDockingPanel to register
     * @param parent The parent JDialog of the panel
     */
    public static void registerDockingPanel(RootDockingPanelAPI panel, JDialog parent) {
        instance.registerDockingPanel(panel, parent);
    }

    /**
     * Deregister a docking root panel
     *
     * @param parent The parent of the panel that we're deregistering
     */
    public static void deregisterDockingPanel(Window parent) {
        instance.deregisterDockingPanel(parent);
    }

    /**
     * Deregister all registered panels. Additionally, dispose any windows created by Modern Docking.
     */
    public static void deregisterAllDockingPanels() {
        instance.deregisterAllDockingPanels();
    }

    public static void registerDockingAnchor(Dockable anchor) {
        instance.registerDockingAnchor(anchor);
    }

    public static void deregisterDockingAnchor(Dockable anchor) {
        instance.deregisterDockingAnchor(anchor);
    }

    /**
     * allows the user to configure auto hide per window. by default auto hide is only enabled on the frames the docking framework creates
     *
     * @param window The window to configure auto hide on
     * @param layer The layout to use for auto hide in the JLayeredPane
     * @param allow Whether auto hide is allowed on this Window
    */
    public static void configureAutoHide(Window window, int layer, boolean allow) {
        instance.configureAutoHide(window, layer, allow);
    }

    /**
     * Check if auto hide is allowed for a dockable
     *
     * @param dockable Dockable to check
     * @return Whether the dockable can be hidden
     */
    public static boolean autoHideAllowed(Dockable dockable) {
        return instance.autoHideAllowed(dockable);
    }

    /**
     * Check if auto hide is allowed for a dockable
     *
     * @param dockable Dockable to check
     * @return Whether the dockable can be hidden
     */
    public static boolean isAutoHideAllowed(Dockable dockable) {
        return instance.isAutoHideAllowed(dockable);
    }

    /**
     * docks a dockable to the center of the given window
     * <p>
     * NOTE: This will only work if the window root docking node is empty. Otherwise, this does nothing.
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     */
    public static void dock(String persistentID, Window window) {
        instance.dock(persistentID, window);
    }

    /**
     * docks a dockable to the center of the given window
     * <p>
     * NOTE: This will only work if the window root docking node is empty. Otherwise, this does nothing.
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     */
    public static void dock(Dockable dockable, Window window) {
        instance.dock(dockable, window);
    }

    /**
     * docks a dockable into the specified region of the root of the window with 25% divider proportion
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     */
    public static void dock(String persistentID, Window window, DockingRegion region) {
        instance.dock(persistentID, window, region);
    }

    /**
     * docks a dockable into the specified region of the root of the window with 25% divider proportion
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     */
    public static void dock(Dockable dockable, Window window, DockingRegion region) {
        instance.dock(dockable, window, region);
    }

    /**
     * docks a dockable into the specified region of the window with the specified divider proportion
     *
     * @param persistentID The persistentID of the dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public static void dock(String persistentID, Window window, DockingRegion region, double dividerProportion) {
        instance.dock(persistentID, window, region, dividerProportion);
    }

    /**
     * docks a dockable into the specified region of the window with the specified divider proportion
     *
     * @param dockable The dockable to dock
     * @param window The window to dock into
     * @param region The region to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public static void dock(Dockable dockable, Window window, DockingRegion region, double dividerProportion) {
        instance.dock(dockable, window, region, dividerProportion);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     */
    public static void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region) {
        instance.dock(sourcePersistentID, targetPersistentID, region);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     */
    public static void dock(String sourcePersistentID, Dockable target, DockingRegion region) {
        instance.dock(sourcePersistentID, target, region);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     */
    public static void dock(Dockable source, String targetPersistentID, DockingRegion region) {
        instance.dock(source, targetPersistentID, region);
    }

    /**
     * docks the target to the source in the specified region with 50% divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     */
    public static void dock(Dockable source, Dockable target, DockingRegion region) {
        instance.dock(source, target, region);
    }

    /**
     * docks the target to the source in the specified region with the specified divider proportion
     *
     * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
     * @param targetPersistentID The persistentID of the target dockable
     * @param region The region on the source dockable to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public static void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region, double dividerProportion) {
        instance.dock(sourcePersistentID, targetPersistentID, region, dividerProportion);
    }

    /**
     * docks the target to the source in the specified region with the specified divider proportion
     *
     * @param source The source dockable to dock the target dockable to
     * @param target The target dockable
     * @param region The region on the source dockable to dock into
     * @param dividerProportion The proportion to use if docking in a split pane
     */
    public static void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion) {
        instance.dock(source, target, region, dividerProportion);
    }

    /**
     * create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param persistentID The persistent ID of the dockable to float in a new window
     */
    public static void newWindow(String persistentID) {
        instance.newWindow(persistentID);
    }

    /**
     * create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param dockable The dockable to float in a new window
     */
    public static void newWindow(Dockable dockable) {
        instance.newWindow(dockable);
    }

    /**
     * Create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param persistentID The persistent ID of the dockable to float in a new window
     * @param location The screen location to display the new frame at
     * @param size The size of the new frame
     */
    public static void newWindow(String persistentID, Point location, Dimension size) {
        instance.newWindow(persistentID, location, size);
    }

    /**
     * Create a new FloatingFrame window for the given dockable, undock it from its current frame (if there is one) and dock it into the new frame
     *
     * @param dockable The dockable to float in a new window
     * @param location The screen location to display the new frame at
     * @param size The size of the new frame
     */
    public static void newWindow(Dockable dockable, Point location, Dimension size) {
        instance.newWindow(dockable, location, size);
    }

    /**
     * bring the specified dockable to the front if it is in a tabbed panel
     *
     * @param persistentID The persistent ID of the dockable
     */
    public static void bringToFront(String persistentID) {
        bringToFront(DockingInternal.get(instance).getDockable(persistentID));
    }

    /**
     * bring the specified dockable to the front if it is in a tabbed panel
     *
     * @param dockable Dockable to bring to the front
     */
    public static void bringToFront(Dockable dockable) {
        instance.bringToFront(dockable);
    }

    /**
     * undock a dockable
     *
     * @param persistentID The persistentID of the dockable to undock
     */
    public static void undock(String persistentID) {
        instance.undock(persistentID);
    }

    /**
     * undock a dockable
     *
     * @param dockable The dockable to undock
     */
    public static void undock(Dockable dockable) {
        instance.undock(dockable);
    }

    /**
     * check if a dockable is currently docked
     *
     * @param persistentID The persistentID of the dockable to check
     * @return Whether the dockable is docked
     */
    public static boolean isDocked(String persistentID) {
        return instance.isDocked(persistentID);
    }

    /**
     * check if a dockable is currently docked
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is docked
     */
    public static boolean isDocked(Dockable dockable) {
        return instance.isDocked(dockable);
    }

    /**
     * check if a dockable is currently hidden
     *
     * @param persistentID The persistentID of the dockable to check
     * @return Whether the dockable is hidden
     */
    public static boolean isHidden(String persistentID) {
        return instance.isHidden(persistentID);
    }

    /**
     * check if a dockable is currently hidden
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is hidden
     */
    public static boolean isHidden(Dockable dockable) {
        return instance.isHidden(dockable);
    }

    /**
     * check if the window can be disposed. Windows can be disposed if they are not the main window and are not maximized
     *
     * @param window Window to check
     * @return Boolean indicating if the specified Window can be disposed
     */
    public static boolean canDisposeWindow(Window window) {
        return instance.canDisposeWindow(window);
    }

    /**
     * checks if a dockable is currently maximized
     *
     * @param dockable The dockable to check
     * @return Whether the dockable is maximized
     */
    public static boolean isMaximized(Dockable dockable) {
        return instance.isMaximized(dockable);
    }

    /**
     * maximizes a dockable
     *
     * @param dockable Dockable to maximize
     */
    public static void maximize(Dockable dockable) {
        instance.maximize(dockable);
    }

    /**
     * minimize a dockable if it is currently maximized
     *
     * @param dockable Dockable to minimize
     */
    public static void minimize(Dockable dockable) {
        instance.minimize(dockable);
    }

    public void autoShowDockable(Dockable dockable) {
        instance.autoShowDockable(dockable);
    }

    public void autoShowDockable(String persistentID) {
        instance.autoShowDockable(persistentID);
    }

    public void autoHideDockable(Dockable dockable) {
        instance.autoHideDockable(dockable);
    }

    public void autoHideDockable(String persistentID) {
        instance.autoHideDockable(persistentID);
    }

    public void autoHideDockable(Dockable dockable, ToolbarLocation location) {
        instance.autoHideDockable(dockable, location);
    }

    public void autoHideDockable(String persistentID, ToolbarLocation location) {
        instance.autoHideDockable(persistentID, location);
    }

    public void autoHideDockable(Dockable dockable, ToolbarLocation location, Window window) {
        instance.autoHideDockable(dockable, location, window);
    }

    public void autoHideDockable(String persistentID, ToolbarLocation location, Window window) {
        instance.autoHideDockable(persistentID, location, window);
    }

    /**
     * display a dockable
     *
     * @param persistentID The persistentID of the dockable to display
     */
    public static void display(String persistentID) {
        instance.display(persistentID);
    }

    /**
     * Display a dockable
     * <p>
     * if the dockable is already docked, then bringToFront is called.
     * if it is not docked, then dock is called, docking it with dockables of the same type
     *
     * @param dockable The dockable to display
     */
    public static void display(Dockable dockable) {
        instance.display(dockable);
    }

    /**
     * update the tab text on a dockable if it is in a tabbed panel
     *
     * @param persistentID The persistentID of the dockable to update
     */
    public static void updateTabInfo(String persistentID) {
        instance.updateTabInfo(persistentID);
    }

    /**
     * update the tab text on a dockable if it is in a tabbed panel
     *
     * @param dockable The dockable to update
     */
    public static void updateTabInfo(Dockable dockable) {
        instance.updateTabInfo(dockable);
    }

    /**
     * Add a new maximize listener. Will be called when a dockable is maximized
     *
     * @param listener Listener to add
     */
    public static void addMaximizeListener(MaximizeListener listener) {
        instance.addMaximizeListener(listener);
    }

    /**
     * Remove a previously added maximize listener. No-op if the listener isn't in the list
     *
     * @param listener Listener to remove
     */
    public static void removeMaximizeListener(MaximizeListener listener) {
        instance.removeMaximizeListener(listener);
    }

    public static void addDockingListener(DockingListener listener) {
        instance.addDockingListener(listener);
    }

    public static void removeDockingListener(DockingListener listener) {
        instance.removeDockingListener(listener);
    }

    public static void addNewFloatingFrameListener(NewFloatingFrameListener listener) {
        DockingListeners.addNewFloatingFrameListener(listener);
    }

    public static void removeNewFloatingFrameListener(NewFloatingFrameListener listener) {
        DockingListeners.removeNewFloatingFrameListener(listener);
    }

    public static DockingAPI getSingleInstance() {
        if (instance == null) {
            throw new RuntimeException("No docking instance available.");
        }
        return instance;
    }
}
