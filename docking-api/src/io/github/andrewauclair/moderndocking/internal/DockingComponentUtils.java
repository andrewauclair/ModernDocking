/*
Copyright (c) 2022-2024 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.exception.RootDockingPanelNotFoundException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Optional;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * set of internal utilities for dealing with the component hierarchy of dockables
 */
public class DockingComponentUtils {
	/**
	 * Not used. All methods in this class are static
	 */
	private DockingComponentUtils() {
	}

	//
	//
	/**
	 * used to clear all anchors before we undock components. This is done to prevent the anchor from being readded
	 *
	 * @param container Container to undock all components from
	 */
	public static void clearAnchors(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DockedSimplePanel) {
				((DockedSimplePanel) component).setAnchor("");
			}
			else if (component instanceof DockedSplitPanel) {
				((DockedSplitPanel) component).setAnchor("");
			}
			else if (component instanceof DockedTabbedPanel) {
				((DockedTabbedPanel) component).setAnchor("");
			}
			else if (component instanceof Container) {
				clearAnchors((Container) component);
			}
		}
	}
	/**
	 * used to undock all dockables in a container. called when a frame is to be disposed
	 *
	 * @param docking The docking instance
	 * @param container Container to undock all components from
	 */
	public static void undockComponents(DockingAPI docking, Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				docking.undock(((DisplayPanel) component).getWrapper().getDockable());
			}
			else if (component instanceof Container) {
				undockComponents(docking, (Container) component);
			}
		}
	}

	/**
	 * search for a root panel on the screen at a specific position
	 *
	 * @param docking The docking instance
	 * @param screenPos The screen position to search at
	 * @return The window at screenPos. null if not found.
	 */
	public static Window findRootAtScreenPos(DockingAPI docking, Point screenPos) {
		for (Window window : docking.getRootPanels().keySet()) {
			Rectangle bounds = new Rectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());

			if (bounds.contains(screenPos) && window.isVisible()) {
				return window;
			}
		}
		return null;
	}

	/**
	 * Find the window that a dockable is docked to
	 *
	 * @param docking The docking instance
	 * @param dockable The dockable to find a window for.
	 * @return The window containing the dockable. null if not found.
	 */
	public static Window findWindowForDockable(DockingAPI docking, Dockable dockable) {
		return DockingInternal.get(docking).getWrapper(dockable).getWindow();
	}

	/**
	 * find the root for the given window, throws an exception if the window doesn't have a root panel
	 *
	 * @param docking The docking instance
	 * @param window The window to find a root for
	 * @return The root of the given window
	 */
	public static InternalRootDockingPanel rootForWindow(DockingAPI docking, Window window) {
		if (docking.getRootPanels().containsKey(window)) {
			return DockingInternal.get(docking).getRootPanels().get(window);
		}
		throw new RootDockingPanelNotFoundException(window);
	}

	/**
	 * Find the window for a given root
	 *
	 * @param docking The docking instance
	 * @param root The root to find a window for
	 * @return The window for the root or null
	 */
	public static Window windowForRoot(DockingAPI docking, RootDockingPanelAPI root) {
		Optional<Window> first = docking.getRootPanels().keySet().stream()
				.filter(frame -> docking.getRootPanels().get(frame) == root)
				.findFirst();

		return first.orElse(null);
	}

	/**
	 * find a dockable at a given screen position
	 *
	 * @param docking The docking instance
	 * @param screenPos Screen position to check for a dockable at
	 * @return Dockable under the screen position, or null if none is found
	 */
	public static Dockable findDockableAtScreenPos(DockingAPI docking, Point screenPos) {
		Window window = findRootAtScreenPos(docking, screenPos);

		// no window found at the location, return null
		if (window == null) {
			return null;
		}

		return findDockableAtScreenPos(screenPos, window);
	}

	/**
	 * find a dockable at a given screen position, limited to a single window
	 *
	 * @param screenPos Screen position to check for a dockable at
	 * @param window The window to check
	 * @return Dockable under the screen position, or null if none is found
	 */
	public static Dockable findDockableAtScreenPos(Point screenPos, Window window) {
		// window is null so there's no dockable to find
		if (window == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, window);

		Component component = SwingUtilities.getDeepestComponentAt(window, framePoint.x, framePoint.y);

		// no component found at the position, return null
		if (component == null) {
			return null;
		}

		while (!(component instanceof DisplayPanel) && component.getParent() != null) {
			component = component.getParent();
		}

		// didn't find a Dockable, return null
		if (!(component instanceof DisplayPanel)) {
			return null;
		}
		return ((DisplayPanel) component).getWrapper().getDockable();
	}

	/**
	 * Find an instance of our CustomTabbedPane at the position in the window
	 *
	 * @param screenPos The screen position on the window to check
	 * @param window The window to check
	 *
	 * @return Instance of CustomTabbedPane that was found or null
	 */
	public static CustomTabbedPane findTabbedPaneAtPos(Point screenPos, Window window) {
		// window is null so there's no dockable to find
		if (window == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, window);

		Component component = SwingUtilities.getDeepestComponentAt(window, framePoint.x, framePoint.y);

		// no component found at the position, return null
		if (component == null) {
			return null;
		}

		while (!(component instanceof CustomTabbedPane) && component.getParent() != null) {
			component = component.getParent();
		}

		// didn't find a Dockable, return null
		if (!(component instanceof CustomTabbedPane)) {
			return null;
		}
		return (CustomTabbedPane) component;
	}

	/**
	 * find a docking panel at a given screen position
	 *
	 * @param docking The docking instance
	 * @param screenPos Screen position to check for a dockable at
	 * @return DockingPanel under the screen position, or null if none is found
	 */
	public static DockingPanel findDockingPanelAtScreenPos(DockingAPI docking, Point screenPos) {
		Window window = findRootAtScreenPos(docking, screenPos);

		// no window found at the location, return null
		if (window == null) {
			return null;
		}

		return findDockingPanelAtScreenPos(screenPos, window);
	}

	/**
	 * find a docking panel at a given screen position. limited to a single window
	 *
	 * @param screenPos Screen position to check for a dockable at
	 * @param window The window to check
	 * @return DockingPanel under the screen position, or null if none is found
	 */
	public static DockingPanel findDockingPanelAtScreenPos(Point screenPos, Window window) {
		// no window found at the location, return null
		if (window == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, window);

		Component component = SwingUtilities.getDeepestComponentAt(window, framePoint.x, framePoint.y);

		// no component found at the position, return null
		if (component == null) {
			return null;
		}

		while (!(component instanceof DockingPanel) && component.getParent() != null) {
			component = component.getParent();
		}

		// didn't find a Dockable, return null
		if (!(component instanceof DockingPanel)) {
			return null;
		}
		return (DockingPanel) component;
	}

	/**
	 * remove panels from window if they return false for allowFloating() and there are no other dockables in the window
	 *
	 * @param docking The docking instance
	 * @param window The window to remove illegal floating dockables from
	 */
	public static void removeIllegalFloats(DockingAPI docking, Window window) {
		// don't touch any dockables on a JDialog, they are their own little environment
		if (window instanceof JDialog) {
			return;
		}

		InternalRootDockingPanel root = rootForWindow(docking, window);

		if (docking.canDisposeWindow(window) && root != null) {
			if (shouldUndock(root)) {
				undockIllegalFloats(root);
			}
		}

		docking.getAppState().persist();
	}

	private static boolean shouldUndock(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				DisplayPanel panel = (DisplayPanel) component;

				// there is at least one dockable that is allowed to float alone, we shouldn't undock
				if (panel.getWrapper().getDockable().isFloatingAllowed()) {
					return false;
				}
			}
			else if (component instanceof Container) {
				if (!shouldUndock((Container) component)) {
					return false;
				}
			}
		}
		return true;
	}

	private static void undockIllegalFloats(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				DisplayPanel panel = (DisplayPanel) component;

				DockableWrapper wrapper = panel.getWrapper();
				Dockable dockable = wrapper.getDockable();
				wrapper.getParent().undock(dockable);

				DockingListeners.fireUndockedEvent(dockable);
			}
			else if (component instanceof Container) {
				undockIllegalFloats((Container) component);
			}
		}
	}

	/**
	 * Finds the first dockable of a specified type. The main frame is searched first and then
	 * any other frames that exist
	 *
	 * @param docking The docking instance
	 * @param type The type to search for
	 *
	 * @return The first Dockable of the given type, if any exist
	 */
	public static Optional<Dockable> findFirstDockableOfType(DockingAPI docking, int type) {
		InternalRootDockingPanel mainRoot = rootForWindow(docking, docking.getMainWindow());

		Optional<Dockable> mainPanelDockable = findDockableOfType(type, mainRoot.getPanel());

		if (mainPanelDockable.isPresent()) {
			return mainPanelDockable;
		}

		for (RootDockingPanelAPI panel : docking.getRootPanels().values()) {
			Optional<Dockable> dockable = findDockableOfType(type, panel);

			if (dockable.isPresent()) {
				return dockable;
			}
		}

		return Optional.empty();
	}

	private static Optional<Dockable> findDockableOfType(int type, Container container) {
		if (container == null) {
			return Optional.empty();
		}

		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				DisplayPanel panel = (DisplayPanel) component;

				DockableWrapper wrapper = panel.getWrapper();
				Dockable dockable = wrapper.getDockable();

				if (dockable.getType() == type) {
					return Optional.of(dockable);
				}
			}
			else if (component instanceof Container) {
				Optional<Dockable> dockableOfType = findDockableOfType(type, (Container) component);

				if (dockableOfType.isPresent()) {
					return dockableOfType;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Check if an anchor is empty
	 *
	 * @param docking The docking instance
	 * @param anchor The anchor to check
	 *
	 * @return The window for the root or null
	 */
	public static boolean isAnchorEmpty(DockingAPI docking, Dockable anchor) {
		try {
			InternalRootDockingPanel root = DockingComponentUtils.rootForWindow(docking, DockingComponentUtils.findWindowForDockable(docking, anchor));

			for (DockingPanel child : root.getChildren()) {
				if (isAnchorNotEmpty(child)) {
					return false;
				}
			}
		}
		catch (RootDockingPanelNotFoundException ignored) {
		}
		return true;
	}

	private static boolean isAnchorNotEmpty(DockingPanel child) {
		if (child.getAnchor() != "") {
			return true;
		}
		for (DockingPanel childChild : child.getChildren()) {
			if (isAnchorNotEmpty(childChild)) {
				return true;
			}
		}
		return false;
	}
}
