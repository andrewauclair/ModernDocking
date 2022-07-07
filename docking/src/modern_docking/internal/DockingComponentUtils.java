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
package modern_docking.internal;

import modern_docking.Dockable;
import modern_docking.Docking;
import modern_docking.RootDockingPanel;
import modern_docking.exception.DockableRegistrationFailureException;
import modern_docking.persist.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static modern_docking.internal.DockingInternal.getWrapper;

public class DockingComponentUtils {
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

	public static JFrame findRootAtScreenPos(Point screenPos) {
		for (JFrame frame : Docking.getInstance().getRootPanels().keySet()) {
			Rectangle bounds = new Rectangle(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());

			if (bounds.contains(screenPos) && frame.isVisible()) {
				return frame;
			}
		}
		return null;
	}

	public static JFrame findFrameForDockable(Dockable dockable) {
		return getWrapper(dockable).getFrame();
	}

	public static RootDockingPanel rootForFrame(JFrame frame) {
		if (Docking.getInstance().getRootPanels().containsKey(frame)) {
			return Docking.getInstance().getRootPanels().get(frame);
		}
		throw new DockableRegistrationFailureException("No root panel for frame has been registered.");
	}

	public static JFrame frameForRoot(RootDockingPanel root) {
		Optional<JFrame> first = Docking.getInstance().getRootPanels().keySet().stream()
				.filter(frame -> Docking.getInstance().getRootPanels().get(frame) == root)
				.findFirst();

		return first.orElse(null);
	}

	public static Dockable findDockableAtScreenPos(Point screenPos) {
		JFrame frame = findRootAtScreenPos(screenPos);

		// no frame found at the location, return null
		if (frame == null) {
			return null;
		}

		return findDockableAtScreenPos(screenPos, frame);
	}

	public static Dockable findDockableAtScreenPos(Point screenPos, JFrame frame) {
		// frame is null so there's no dockable to find
		if (frame == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, frame);

		Component component = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);

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

	public static DockingPanel findDockingPanelAtScreenPos(Point screenPos) {
		JFrame frame = findRootAtScreenPos(screenPos);

		// no frame found at the location, return null
		if (frame == null) {
			return null;
		}

		return findDockingPanelAtScreenPos(screenPos, frame);
	}

	public static DockingPanel findDockingPanelAtScreenPos(Point screenPos, JFrame frame) {
		// no frame found at the location, return null
		if (frame == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, frame);

		Component component = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);

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

	public static void removeIllegalFloats(JFrame frame) {
		// remove panels from frame if they return false for allowFloating() and there are no other dockables in the frame
		RootDockingPanel root = rootForFrame(frame);

		if (Docking.canDisposeFrame(frame) && root != null) {
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
}
