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
package ModernDocking;

import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.exception.NotDockedException;
import ModernDocking.floating.FloatListener;
import ModernDocking.internal.*;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.WindowLayout;
import ModernDocking.persist.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ModernDocking.internal.DockingInternal.getDockable;
import static ModernDocking.internal.DockingInternal.getWrapper;

/**
 * Main class for the docking framework
 * register and dock/undock dockables here
 */
public class Docking {
	// map of all the root panels in the application
	private final Map<Window, RootDockingPanel> rootPanels = new HashMap<>();

	// the applications main frame
	private final Window mainWindow;

	private static Docking instance;

	// this may look unused, but we need to create an instance of it to make it work
	private final ActiveDockableHighlighter activeDockableHighlighter = new ActiveDockableHighlighter();

	private final AppStatePersister appStatePersister = new AppStatePersister();

	private static boolean isInOnDockingCallback = false;

	/**
	 * Create the one and only instance of the Docking class for the application
	 * @param mainWindow The main window of the application
	 */
	public static void initialize(Window mainWindow) {
		new Docking(mainWindow);
	}

	private Docking(Window mainWindow) {
		this.mainWindow = mainWindow;
		instance = this;

		FloatListener.reset();
	}

	/**
	 * Retrieve an instance of the Docking singleton
	 *
	 * @return Instance of Docking
	 */
	public static Docking getInstance() {
		return instance;
	}

	/**
	 * Get a map of RootDockingPanels to their Windows
	 *
	 * @return map of root panels
	 */
	public Map<Window, RootDockingPanel> getRootPanels() {
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
	public static void registerDockable(Dockable dockable) {
		DockingInternal.registerDockable(dockable);
	}

	/**
	 * Dockables must be deregistered so it can be properly disposed
	 *
	 * @param dockable Dockable to deregister
	 */
	public static void deregisterDockable(Dockable dockable) {
		DockingInternal.deregisterDockable(dockable);
	}

	/**
	 * registration function for DockingPanel
	 *
	 * @param panel Panel to register
	 * @param parent The parent frame of the panel
	 */
	public static void registerDockingPanel(RootDockingPanel panel, JFrame parent) {
		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}

		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingWindow(parent, panel);

		instance.appStatePersister.addFrame(parent);
	}

	/**
	 * Register a RootDockingPanel
	 *
	 * @param panel RootDockingPanel to register
	 * @param parent The parent JDialog of the panel
	 */
	public static void registerDockingPanel(RootDockingPanel panel, JDialog parent) {
		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}

		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingWindow(parent, panel);

		instance.appStatePersister.addFrame(parent);
	}

	/**
	 * allows the user to configure pinning per window. by default pinning is only enabled on the frames the docking framework creates
	 *
	 * @param window The window to configure pinning on
	 * @param layer The layout to use for pinning in the JLayeredPane
	 * @param allow Whether pinning is allowed on this Window
	 */
	public static void configurePinning(Window window, int layer, boolean allow) {
		if (!instance.rootPanels.containsKey(window)) {
			throw new DockableRegistrationFailureException("No root panel for window has been registered.");
		}

		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);
		root.setPinningSupported(allow);
		root.setPinningLayer(layer);
	}

	/**
	 * Check if pinning is allowed for a dockable
	 *
	 * @param dockable Dockable to check
	 * @return Whether the dockable can be pinned
	 */
	public static boolean pinningAllowed(Dockable dockable) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(DockingComponentUtils.findWindowForDockable(dockable));

		return dockable.allowPinning() && root.isPinningSupported();
	}

	/**
	 * Deregister a docking root panel
	 *
	 * @param parent The parent of the panel that we're deregistering
	 */
	public static void deregisterDockingPanel(Window parent) {
		if (instance.rootPanels.containsKey(parent)) {
			RootDockingPanel root = instance.rootPanels.get(parent);

			DockingComponentUtils.undockComponents(root);
		}

		instance.rootPanels.remove(parent);

		instance.appStatePersister.removeFrame(parent);
	}

	/**
	 * docks a dockable to the center of the given window
	 *
	 * NOTE: This will only work if the window root docking node is empty. Otherwise this does nothing.
	 *
	 * @param persistentID The persistentID of the dockable to dock
	 * @param window The window to dock into
	 */
	public static void dock(String persistentID, Window window) {
		dock(getDockable(persistentID), window, DockingRegion.CENTER);
	}

	/**
	 * docks a dockable to the center of the given window
	 *
	 * NOTE: This will only work if the window root docking node is empty. Otherwise this does nothing.
	 *
	 * @param dockable The dockable to dock
	 * @param window The window to dock into
	 */
	public static void dock(Dockable dockable, Window window) {
		dock(dockable, window, DockingRegion.CENTER);
	}

	/**
	 * docks a dockable into the specified region of the root of the window with 25% divider proportion
	 *
	 * @param persistentID The persistentID of the dockable to dock
	 * @param window The window to dock into
	 * @param region The region to dock into
	 */
	public static void dock(String persistentID, Window window, DockingRegion region) {
		dock(getDockable(persistentID), window, region, 0.25);
	}

	/**
	 * docks a dockable into the specified region of the root of the window with 25% divider proportion
	 *
	 * @param dockable The dockable to dock
	 * @param window The window to dock into
	 * @param region The region to dock into
	 */
	public static void dock(Dockable dockable, Window window, DockingRegion region) {
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
	public static void dock(String persistentID, Window window, DockingRegion region, double dividerProportion) {
		dock(getDockable(persistentID), window, region, dividerProportion);
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
		RootDockingPanel root = instance.rootPanels.get(window);

		if (root == null) {
			throw new DockableRegistrationFailureException("Window does not have a RootDockingPanel: " + window);
		}

		// if the dockable has decided to do something else, skip out of this function
		if (!isInOnDockingCallback)  {
			isInOnDockingCallback = true;

			boolean dockingHandled = dockable.onDocking();

			isInOnDockingCallback = false;

			if (dockingHandled) {
				return;
			}
		}

		root.dock(dockable, region, dividerProportion);

		getWrapper(dockable).setWindow(window);

		// fire a docked event when the component is actually added
		DockingListeners.fireDockedEvent(dockable);

		AppState.persist();

		dockable.onDocked();
	}

	/**
	 * docks the target to the source in the specified region with 50% divider proportion
	 *
	 * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
	 * @param targetPersistentID The persistentID of the target dockable
	 * @param region The region on the source dockable to dock into
	 */
	public static void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region) {
		dock(getDockable(sourcePersistentID), getDockable(targetPersistentID), region, 0.5);
	}

	/**
	 * docks the target to the source in the specified region with 50% divider proportion
	 *
	 * @param sourcePersistentID The persistentID of the source dockable to dock the target dockable to
	 * @param target The target dockable
	 * @param region The region on the source dockable to dock into
	 */
	public static void dock(String sourcePersistentID, Dockable target, DockingRegion region) {
		dock(getDockable(sourcePersistentID), target, region, 0.5);
	}

	/**
	 * docks the target to the source in the specified region with 50% divider proportion
	 *
	 * @param source The source dockable to dock the target dockable to
	 * @param targetPersistentID The persistentID of the target dockable
	 * @param region The region on the source dockable to dock into
	 */
	public static void dock(Dockable source, String targetPersistentID, DockingRegion region) {
		dock(source, getDockable(targetPersistentID), region, 0.5);
	}

	/**
	 * docks the target to the source in the specified region with 50% divider proportion
	 *
	 * @param source The source dockable to dock the target dockable to
	 * @param target The target dockable
	 * @param region The region on the source dockable to dock into
	 */
	public static void dock(Dockable source, Dockable target, DockingRegion region) {
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
	public static void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region, double dividerProportion) {
		dock(getDockable(sourcePersistentID), getDockable(targetPersistentID), region, dividerProportion);
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
		if (!isDocked(target)) {
			throw new NotDockedException(target);
		}

		// if the source dockable has decided to do something else, skip out of this function
		if (!isInOnDockingCallback)  {
			isInOnDockingCallback = true;

			boolean dockingHandled = source.onDocking();

			isInOnDockingCallback = false;

			if (dockingHandled) {
				return;
			}
		}

		DockableWrapper wrapper = getWrapper(target);

		wrapper.getParent().dock(source, region, dividerProportion);

		getWrapper(source).setWindow(wrapper.getWindow());

		DockingListeners.fireDockedEvent(source);

		AppState.persist();
	}

	/**
	 * create a new FloatingFrame window for the given dockable, undock it from its current frame and dock it into the new frame
	 *
	 * @param dockable The dockable to float in a new window
	 */
	public static void newWindow(Dockable dockable) {
		DisplayPanel displayPanel = getWrapper(dockable).getDisplayPanel();
		Point location = displayPanel.getLocationOnScreen();
		Dimension size = displayPanel.getSize();

		FloatingFrame frame = new FloatingFrame(dockable, location, size, JFrame.NORMAL);

		undock(dockable);
		dock(dockable, frame);
	}

	/**
	 * bring the specified dockable to the front if it is in a tabbed panel
	 *
	 * @param dockable Dockable to bring to the front
	 */
	public static void bringToFront(Dockable dockable) {
		if (!isDocked(dockable)) {
			throw new NotDockedException(dockable);
		}

		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		window.setAlwaysOnTop(true);
		window.setAlwaysOnTop(false);

		if (getWrapper(dockable).getParent() instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) getWrapper(dockable).getParent();
			tabbedPanel.bringToFront(dockable);
		}
	}

	/**
	 * undock a dockable
	 *
	 * @param persistentID The persistentID of the dockable to undock
	 */
	public static void undock(String persistentID) {
		undock(getDockable(persistentID));
	}

	/**
	 * undock a dockable
	 *
	 * @param dockable The dockable to undock
	 */
	public static void undock(Dockable dockable) {
		if (!isDocked(dockable)) {
			// nothing to undock
			return;
		}

		Window window = DockingComponentUtils.findWindowForDockable(dockable);

		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		DockableWrapper wrapper = getWrapper(dockable);

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

		if (window != null && root != null && canDisposeWindow(window) && root.isEmpty()) {
			deregisterDockingPanel(window);
			window.dispose();
		}

		AppState.persist();

		if (!dockable.canBeClosed()) {
			dock(dockable, instance.mainWindow);
		}
		else {
			dockable.onUndocked();
		}
	}

	/**
	 * check if a dockable is currently docked
	 *
	 * @param persistentID The persistentID of the dockable to check
	 * @return Whether the dockable is docked
	 */
	public static boolean isDocked(String persistentID) {
		return isDocked(getDockable(persistentID));
	}

	/**
	 * check if a dockable is currently docked
	 *
	 * @param dockable The dockable to check
	 * @return Whether the dockable is docked
	 */
	public static boolean isDocked(Dockable dockable) {
		return getWrapper(dockable).getParent() != null;
	}

	/**
	 * check if a dockable is currently in the unpinned state
	 *
	 * @param persistentID The persistentID of the dockable to check
	 * @return Whether the dockable is unpinned
	 */
	public static boolean isUnpinned(String persistentID) {
		return isUnpinned(getDockable(persistentID));
	}

	/**
	 * check if a dockable is currently in the unpinned state
	 *
	 * @param dockable The dockable to check
	 * @return Whether the dockable is unpinned
	 */
	public static boolean isUnpinned(Dockable dockable) {
		return getWrapper(dockable).isUnpinned();
	}

	/**
	 * check if the window can be disposed. Windows can be disposed if they are not the main window and are not maximized
	 *
	 * @param window Window to check
	 * @return Boolean indicating if the specified Window can be disposed
	 */
	public static boolean canDisposeWindow(Window window) {
		// don't dispose of any docking windows that are JDialogs
		if (window instanceof JDialog) {
			return false;
		}
		return window != instance.mainWindow && !DockingState.maximizeRestoreLayout.containsKey(window);
	}

	/**
	 * checks if a dockable is currently maximized
	 *
	 * @param dockable The dockable to check
	 * @return Whether the dockable is maximized
	 */
	public static boolean isMaximized(Dockable dockable) {
		return getWrapper(dockable).isMaximized();
	}

	/**
	 * maximizes a dockable
	 *
	 * @param dockable Dockable to maximize
	 */
	public static void maximize(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		// can only maximize one panel per root
		if (!DockingState.maximizeRestoreLayout.containsKey(window) && root != null) {
			getWrapper(dockable).setMaximized(true);
			DockingListeners.fireMaximizeEvent(dockable, true);

			WindowLayout layout = DockingState.getWindowLayout(window);
			layout.setMaximizedDockable(dockable.getPersistentID());

			DockingState.maximizeRestoreLayout.put(window, layout);

			DockingComponentUtils.undockComponents(root);

			dock(dockable, window);
		}
	}

	/**
	 * minimize a dockable if it is currently maximized
	 *
	 * @param dockable Dockable to minimize
	 */
	public static void minimize(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);

		// can only minimize if already maximized
		if (DockingState.maximizeRestoreLayout.containsKey(window)) {
			getWrapper(dockable).setMaximized(false);
			DockingListeners.fireMaximizeEvent(dockable, false);

			DockingState.restoreWindowLayout(window, DockingState.maximizeRestoreLayout.get(window));

			DockingState.maximizeRestoreLayout.remove(window);

			DockingInternal.fireDockedEventForFrame(window);
		}
	}

	/**
	 * pin a dockable. only valid if the dockable is unpinned
	 *
	 * @param dockable Dockable to pin
	 */
	public static void pinDockable(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (getWrapper(dockable).isUnpinned()) {
			root.setDockablePinned(dockable);

			getWrapper(dockable).setUnpinned(false);
			DockingListeners.fireDockedEvent(dockable);
		}
	}

	// TODO looks like this could get called on an already unpinned dockable
	/**
	 * unpin a dockable. only valid if the dockable is pinned
	 * @param dockable Dockable to unpin
	 */
	public static void unpinDockable(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		Component component = (Component) dockable;

		Point posInFrame = component.getLocation();
		SwingUtilities.convertPointToScreen(posInFrame, component.getParent());
		SwingUtilities.convertPointFromScreen(posInFrame, root);

		posInFrame.x += component.getWidth() / 2;
		posInFrame.y += component.getHeight() / 2;

		undock(dockable);

		// reset the window, undocking the dockable sets it to null
		getWrapper(dockable).setWindow(window);
		getWrapper(dockable).setUnpinned(true);

		boolean allowedSouth = dockable.getStyle() == DockableStyle.BOTH || dockable.getStyle() == DockableStyle.HORIZONTAL;

		int westDist = posInFrame.x;
		int eastDist = window.getWidth() - posInFrame.x;
		int southDist = window.getHeight() - posInFrame.y;

		boolean east = eastDist <= westDist;
		boolean south = southDist < westDist && southDist < eastDist;

		if (south && allowedSouth) {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.SOUTH);
		}
		else if (east) {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.EAST);
		}
		else {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.WEST);
		}
		DockingListeners.fireUnpinnedEvent(dockable);
		dockable.hidden();
		DockingListeners.fireHiddenEvent(dockable);
	}

	/**
	 * display a dockable
	 *
	 * @param persistentID The persistentID of the dockable to display
	 */
	public static void display(String persistentID) {
		display(getDockable(persistentID));
	}

	/**
	 * Display a dockable
	 *
	 * if the dockable is already docked, then bringToFront is called.
	 * if it is not docked, then dock is called, docking it with dockables of the same type
	 *
	 * @param dockable The dockable to display
	 */
	public static void display(Dockable dockable) {
		if (isDocked(dockable)) {
			bringToFront(dockable);
		}
		else {
			// go through all the dockables and find the first one that is the same type
			Optional<Dockable> firstOfType = DockingComponentUtils.findFirstDockableOfType(dockable.getType());

			if (firstOfType.isPresent()) {
				dock(dockable, firstOfType.get(), DockingRegion.CENTER);
			}
			else {
				// if we didn't find any dockables of the same type, we'll dock to north
				dock(dockable, instance.mainWindow, DockingRegion.NORTH);
			}
		}
	}

	/**
	 * update the tab text on a dockable if it is in a tabbed panel
	 *
	 * @param persistentID The persistentID of the dockable to update
	 */
	public static void updateTabText(String persistentID) {
		updateTabText(getDockable(persistentID));
	}

	/**
	 * update the tab text on a dockable if it is in a tabbed panel
	 *
	 * @param dockable The dockable to update
	 */
	public static void updateTabText(Dockable dockable) {
		if (!isDocked(dockable)) {
			// if the dockable isn't docked then we don't have to do anything to update its tab text
			return;
		}

		ApplicationLayout layout = DockingState.getApplicationLayout();

		Docking.undock(dockable);

		DockingState.restoreApplicationLayout(layout);
	}
}
