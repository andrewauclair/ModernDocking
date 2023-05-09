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
import ModernDocking.layouts.*;
import ModernDocking.persist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static ModernDocking.internal.DockingInternal.getDockable;
import static ModernDocking.internal.DockingInternal.getWrapper;

// TODO find a good solution for where to dock new dockables. For example, I might select a view menu item which docks a certain dockable, that dockable should go in a logical location which is entirely app dependent (might depend on what other dockables are docked)

// TODO support pinning dockables, this should also support icons so that text or an icon can be shown on the toolbar button

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	// cached size of the decorated frame border size
	public static Insets frameBorderSizes = new Insets(0, 0, 0, 0);

	// map of all the root panels in the application
	private final Map<Window, RootDockingPanel> rootPanels = new HashMap<>();

	// the applications main frame
	private final Window mainWindow;

	private static Docking instance;

	// this may look unused, but we need to create an instance of it to make it work
	private final ActiveDockableHighlighter activeDockableHighlighter = new ActiveDockableHighlighter();

	private final AppStatePersister appStatePersister = new AppStatePersister();

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

	public static Docking getInstance() {
		return instance;
	}

	public Map<Window, RootDockingPanel> getRootPanels() {
		return rootPanels;
	}

	public Window getMainWindow() {
		return mainWindow;
	}

	// register a dockable with the framework
	public static void registerDockable(Dockable dockable) {
		DockingInternal.registerDockable(dockable);
	}

	// Dockables must be deregistered so it can be properly disposed
	public static void deregisterDockable(Dockable dockable) {
		DockingInternal.deregisterDockable(dockable);
	}

	// registration function for DockingPanel
	public static void registerDockingPanel(RootDockingPanel panel, JFrame parent) {
		// calculate the frame border size, used when dropping a dockable and changing from an undecorated frame (TempFloatingFrame) to a FloatingFrame
		if (frameBorderSizes.top == 0) {
			parent.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					Point location = parent.getLocation();
					Point contentsLocation = parent.getContentPane().getLocation();

					// convert content point to screen, location is already in screen coordinates because it's the location of a frame
					SwingUtilities.convertPointToScreen(contentsLocation, parent.getContentPane().getParent());

					Dimension size = parent.getSize();
					Dimension contentsSize = parent.getContentPane().getSize();

					// frame border size is the difference between the content's location and size on screen and the frame's location and size on screen
					int top = contentsLocation.y - location.y;
					int left = contentsLocation.x - location.x;
					frameBorderSizes = new Insets(top, left, size.height - contentsSize.height - top, size.width - contentsSize.width - left);

					// finally, remove this listener now that we've calculated the size
					parent.removeComponentListener(this);
				}
			});
		}

		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}

		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingWindow(parent, panel);

		instance.appStatePersister.addFrame(parent);
	}

	public static void registerDockingPanel(RootDockingPanel panel, JDialog parent) {
		// calculate the frame border size, used when dropping a dockable and changing from an undecorated frame (TempFloatingFrame) to a FloatingFrame
		if (frameBorderSizes.top == 0) {
			parent.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					Point location = parent.getLocation();
					Point contentsLocation = parent.getContentPane().getLocation();

					// convert content point to screen, location is already in screen coordinates because it's the location of a frame
					SwingUtilities.convertPointToScreen(contentsLocation, parent.getContentPane().getParent());

					Dimension size = parent.getSize();
					Dimension contentsSize = parent.getContentPane().getSize();

					// frame border size is the difference between the content's location and size on screen and the frame's location and size on screen
					int top = contentsLocation.y - location.y;
					int left = contentsLocation.x - location.x;
					frameBorderSizes = new Insets(top, left, size.height - contentsSize.height - top, size.width - contentsSize.width - left);

					// finally, remove this listener now that we've calculated the size
					parent.removeComponentListener(this);
				}
			});
		}

		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}

		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingWindow(parent, panel);

		instance.appStatePersister.addFrame(parent);
	}

	// allows the user to configure pinning per window. by default pinning is only enabled on the frames the docking framework creates
	public static void configurePinning(Window window, int layer, boolean allow) {
		if (!instance.rootPanels.containsKey(window)) {
			throw new DockableRegistrationFailureException("No root panel for window has been registered.");
		}

		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);
		root.setPinningSupported(allow);
		root.setPinningLayer(layer);
	}

	public static boolean pinningAllowed(Dockable dockable) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(DockingComponentUtils.findWindowForDockable(dockable));

		return dockable.allowPinning() && root.isPinningSupported();
	}

	public static void deregisterDockingPanel(Window parent) {
		if (instance.rootPanels.containsKey(parent)) {
			RootDockingPanel root = instance.rootPanels.get(parent);

			DockingComponentUtils.undockComponents(root);
		}

		instance.rootPanels.remove(parent);

		instance.appStatePersister.removeFrame(parent);
	}

	// docks a dockable to the center of the given window
	// NOTE: This will only work if the window root docking node is empty. Otherwise this does nothing.
	public static void dock(String persistentID, Window window) {
		dock(getDockable(persistentID), window, DockingRegion.CENTER);
	}

	// docks a dockable to the center of the given window
	// NOTE: This will only work if the window root docking node is empty. Otherwise this does nothing.
	public static void dock(Dockable dockable, Window window) {
		dock(dockable, window, DockingRegion.CENTER);
	}

	// docks a dockable into the specified region of the window with 50% divider proportion
	public static void dock(String persistentID, Window window, DockingRegion region) {
		dock(getDockable(persistentID), window, region, 0.5);
	}

	// docks a dockable into the specified region of the window with 50% divider proportion
	public static void dock(Dockable dockable, Window window, DockingRegion region) {
		dock(dockable, window, region, 0.5);
	}

	// docks a dockable into the specified region of the window with the specified divider proportion
	public static void dock(String persistentID, Window window, DockingRegion region, double dividerProportion) {
		dock(getDockable(persistentID), window, region, dividerProportion);
	}

	// docks a dockable into the specified region of the window with the specified divider proportion
	public static void dock(Dockable dockable, Window window, DockingRegion region, double dividerProportion) {
		RootDockingPanel root = instance.rootPanels.get(window);

		if (root == null) {
			throw new DockableRegistrationFailureException("Window does not have a RootDockingPanel: " + window);
		}

		// if the dockable has decided to do something else, skip out of this function
		if (dockable.onDocking())
		{
			return;
		}

		root.dock(dockable, region, dividerProportion);

		getWrapper(dockable).setWindow(window);

		// fire a docked event when the component is actually added
		DockingListeners.fireDockedEvent(dockable);

		AppState.persist();

		dockable.onDocked();
	}

	// docks the target to the source in the specified region with 50% divider proportion
	public static void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region) {
		dock(getDockable(sourcePersistentID), getDockable(targetPersistentID), region, 0.5);
	}

	// docks the target to the source in the specified region with 50% divider proportion
	public static void dock(String sourcePersistentID, Dockable target, DockingRegion region) {
		dock(getDockable(sourcePersistentID), target, region, 0.5);
	}

	// docks the target to the source in the specified region with 50% divider proportion
	public static void dock(Dockable source, String targetPersistentID, DockingRegion region) {
		dock(source, getDockable(targetPersistentID), region, 0.5);
	}

	// docks the target to the source in the specified region with 50% divider proportion
	public static void dock(Dockable source, Dockable target, DockingRegion region) {
		dock(source, target, region, 0.5);
	}

	// docks the target to the source in the specified region with the specified divider proportion
	public static void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion) {
		if (!isDocked(target)) {
			throw new NotDockedException(target);
		}
		DockableWrapper wrapper = getWrapper(target);

		wrapper.getParent().dock(source, region, dividerProportion);

		getWrapper(source).setWindow(wrapper.getWindow());

		DockingListeners.fireDockedEvent(source);

		AppState.persist();
	}

	// create a new FloatingFrame window for the given dockable, undock it from its current frame and dock it into the new frame
	public static void newWindow(Dockable dockable) {
		Point location = getWrapper(dockable).getDisplayPanel().getLocationOnScreen();
		Dimension size = getWrapper(dockable).getDisplayPanel().getSize();

		location.x -= frameBorderSizes.left;
		location.y -= frameBorderSizes.top;

		size.width += frameBorderSizes.left + frameBorderSizes.right;
		size.height += frameBorderSizes.top + frameBorderSizes.bottom;

		FloatingFrame frame = new FloatingFrame(location, size, JFrame.NORMAL);

		undock(dockable);
		dock(dockable, frame);
	}

	// bring the specified dockable to the front if it is in a tabbed panel
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

	// undock a dockable
	public static void undock(String persistentID) {
		undock(getDockable(persistentID));
	}

	// undock a dockable
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

		dockable.onUndocked();
	}

	// check if a dockable is currently docked
	public static boolean isDocked(String persistentID) {
		return isDocked(getDockable(persistentID));
	}

	// check if a dockable is currently docked
	public static boolean isDocked(Dockable dockable) {
		return getWrapper(dockable).getParent() != null;
	}

	// check if a dockable is currently in the unpinned state
	public static boolean isUnpinned(String persistentID) {
		return isUnpinned(getDockable(persistentID));
	}

	// check if a dockable is currently in the unpinned state
	public static boolean isUnpinned(Dockable dockable) {
		return getWrapper(dockable).isUnpinned();
	}

	// check if the window can be disposed. Frames can be disposed if they are not the main window and are not maximized
	public static boolean canDisposeWindow(Window window) {
		return window != instance.mainWindow && !DockingState.maximizeRestoreLayout.containsKey(window);
	}

	// checks if a dockable is currently maximized
	public static boolean isMaximized(Dockable dockable) {
		return getWrapper(dockable).isMaximized();
	}

	// maximizes a dockable
	public static void maximize(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		// can only maximize one panel per root
		if (!DockingState.maximizeRestoreLayout.containsKey(window) && root != null) {
			getWrapper(dockable).setMaximized(true);
			DockingListeners.fireMaximizeEvent(dockable, true);

			DockingLayout layout = DockingState.getCurrentLayout(window);
			layout.setMaximizedDockable(dockable.persistentID());

			DockingState.maximizeRestoreLayout.put(window, layout);

			DockingComponentUtils.undockComponents(root);

			dock(dockable, window);
		}
	}

	// minimize a dockable if it is currently maximized
	public static void minimize(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);

		// can only minimize if already maximized
		if (DockingState.maximizeRestoreLayout.containsKey(window)) {
			getWrapper(dockable).setMaximized(false);
			DockingListeners.fireMaximizeEvent(dockable, false);

			DockingState.setLayout(window, DockingState.maximizeRestoreLayout.get(window));

			DockingState.maximizeRestoreLayout.remove(window);

			DockingInternal.fireDockedEventForFrame(window);
		}
	}

	// pin a dockable. only valid if the dockable is unpinned
	public static void pinDockable(Dockable dockable) {
		Window window = DockingComponentUtils.findWindowForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (getWrapper(dockable).isUnpinned()) {
			root.setDockablePinned(dockable);

			getWrapper(dockable).setUnpinned(false);
			DockingListeners.fireDockedEvent(dockable);
		}
	}

	// unpin a dockable. only valid if the dockable is pinned
	// TODO looks like this could get called on an already unpinned dockable
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

		boolean allowedSouth = dockable.style() == DockableStyle.BOTH || dockable.style() == DockableStyle.HORIZONTAL;

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
	}

	// display a dockable by persistentID
	public static void display(String persistentID) {
		display(getDockable(persistentID));
	}

	// if the dockable is already docked, then bringToFront is called.
	// if it is not docked, then dock is called, docking it with dockables of the same type
	public static void display(Dockable dockable) {
		if (isDocked(dockable)) {
			bringToFront(dockable);
		}
		else if (dockable.strategy() != null) {
			dockable.strategy().dock();
		}
		else {
			// go through all the dockables and find the first one that is the same type
			Optional<Dockable> firstOfType = DockingComponentUtils.findFirstDockableOfType(dockable.type());

			if (firstOfType.isPresent()) {
				dock(dockable, firstOfType.get(), DockingRegion.CENTER);
			}
			else {
				// if we didn't find any dockables of the same type, we'll dock to north
				dock(dockable, instance.mainWindow, DockingRegion.NORTH);
			}
		}
	}

	// update the tab text on a dockable if it is in a tabbed panel
	public static void updateTabText(String persistentID) {
		updateTabText(getDockable(persistentID));
	}

	// update the tab text on a dockable if it is in a tabbed panel
	public static void updateTabText(Dockable dockable) {
		if (!isDocked(dockable)) {
			// if the dockable isn't docked then we don't have to do anything to update its tab text
			return;
		}

		FullAppLayout layout = DockingState.getFullLayout();

		Docking.undock(dockable);

		DockingState.restoreFullLayout(layout);
	}
}
