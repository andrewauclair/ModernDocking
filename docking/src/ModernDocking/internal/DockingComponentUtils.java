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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.RootDockingPanel;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.persist.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static ModernDocking.internal.DockingInternal.getWrapper;

// set of internal utilities for dealing with the component hierarchy of dockables
public class DockingComponentUtils {
	// used to undock all dockables in a container
	// called when a frame is to be disposed
	public static void undockComponents(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				Docking.undock(((DisplayPanel) component).getWrapper().getDockable());
			}
			else if (component instanceof Container) {
				undockComponents((Container) component);
			}
		}
	}

	// search for a root panel on the screen at a specific position
	public static Window findRootAtScreenPos(Point screenPos) {
		for (Window window : Docking.getInstance().getRootPanels().keySet()) {
			Rectangle bounds = new Rectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());

			if (bounds.contains(screenPos) && window.isVisible()) {
				return window;
			}
		}
		return null;
	}

	// find the frame that a dockable is in
	// returns null if the dockable is not docked
	public static Window findWindowForDockable(Dockable dockable) {
		return getWrapper(dockable).getWindow();
	}

	// find the root for the given window
	// throws an exception if the window doesn't have a root panel
	public static RootDockingPanel rootForWindow(Window window) {
		if (Docking.getInstance().getRootPanels().containsKey(window)) {
			return Docking.getInstance().getRootPanels().get(window);
		}
		throw new DockableRegistrationFailureException("No root panel for window has been registered.");
	}

	// find the frame for a given root
	public static Window windowForRoot(RootDockingPanel root) {
		Optional<Window> first = Docking.getInstance().getRootPanels().keySet().stream()
				.filter(frame -> Docking.getInstance().getRootPanels().get(frame) == root)
				.findFirst();

		return first.orElse(null);
	}

	// find a dockable at a given screen position
	public static Dockable findDockableAtScreenPos(Point screenPos) {
		Window window = findRootAtScreenPos(screenPos);

		// no window found at the location, return null
		if (window == null) {
			return null;
		}

		return findDockableAtScreenPos(screenPos, window);
	}

	// find a dockable at a given screen position, limited to a single window
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

	// find a docking panel at a given screen position
	public static DockingPanel findDockingPanelAtScreenPos(Point screenPos) {
		Window window = findRootAtScreenPos(screenPos);

		// no window found at the location, return null
		if (window == null) {
			return null;
		}

		return findDockingPanelAtScreenPos(screenPos, window);
	}

	// find a docking panel at a given screen position. limited to a single window
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

	// remove panels from window if they return false for allowFloating() and there are no other dockables in the window
	public static void removeIllegalFloats(Window window) {
		RootDockingPanel root = rootForWindow(window);

		if (Docking.canDisposeWindow(window) && root != null) {
			if (shouldUndock(root)) {
				undockIllegalFloats(root);
			}
		}

		AppState.persist();
	}

	private static boolean shouldUndock(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof DisplayPanel) {
				DisplayPanel panel = (DisplayPanel) component;
				if (panel.getWrapper().getDockable().floatingAllowed()) {
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
				DockingListeners.fireAutoUndockedEvent(dockable);
			}
			else if (component instanceof Container) {
				undockIllegalFloats((Container) component);
			}
		}
	}

	public static Optional<Dockable> findFirstDockableOfType(int type) {
		RootDockingPanel mainRoot = rootForWindow(Docking.getInstance().getMainWindow());

		Optional<Dockable> mainPanelDockable = findDockableOfType(type, mainRoot.getPanel());

		if (mainPanelDockable.isPresent()) {
			return mainPanelDockable;
		}

		for (RootDockingPanel panel : Docking.getInstance().getRootPanels().values()) {
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

				if (dockable.type() == type) {
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
}
