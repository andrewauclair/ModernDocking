package docking;

import exception.DockableRegistrationFailureException;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// TODO we need to check if the window loses focus and kill the floating dialog. other wise strange things happen

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private static final Map<String, Dockable> dockables = new HashMap<>();

	private static final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	private static JFrame frameToDispose = null;

	public static void registerDockable(Dockable dockable) {
		if (dockables.containsKey(dockable.persistentID())) {
			throw new DockableRegistrationFailureException("Registration for Dockable failed. Persistent ID " + dockable.persistentID() + " already exists.");
		}
		dockables.put(dockable.persistentID(), dockable);
	}

	// Dockables must be deregistered so it can be properly disposed
	public static void deregisterDockable(Dockable dockable) {
		dockables.remove(dockable.persistentID());
	}

	// package private registration function for DockingPanel
	public static void registerDockingPanel(RootDockingPanel panel, JFrame parent) {
		if (frameBorderSize.height == 0) {
			SwingUtilities.invokeLater(() -> {
				Dimension size = parent.getSize();
				Dimension contentsSize = parent.getContentPane().getSize();
				Insets insets = parent.getContentPane().getInsets();

				frameBorderSize = new Dimension(size.width - contentsSize.width - insets.left, size.height - contentsSize.height - insets.top);

				System.out.println("size: " + size + "\ncontents size: " + contentsSize + "\ninsets: " + insets + "\nframe border size: " + frameBorderSize);
			});
		}

		if (rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}
		rootPanels.put(parent, panel);
	}

	// TODO add a possible listener for this. I'd like a way to listen for panels being auto undocked and being able to redock them somewhere else depending on what they are
	static void deregisterDockingPanel(JFrame parent) {
		if (rootPanels.containsKey(parent)) {
			RootDockingPanel root = rootPanels.get(parent);

			undockComponents(root);
		}

		rootPanels.remove(parent);
	}

	private static void undockComponents(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof Container) {
				undockComponents((Container) component);
			}
			else if (component instanceof Dockable) {
				undock((Dockable) component);
			}
		}
	}
	public static JFrame findRootAtScreenPos(Point screenPos) {
		for (JFrame frame : rootPanels.keySet()) {
			Point point = new Point(frame.getLocation());
			Rectangle bounds = new Rectangle(point.x, point.y, frame.getWidth(), frame.getHeight());

			if (bounds.contains(screenPos)) {
				return frame;
			}
		}
		return null;
	}

	public static RootDockingPanel rootForFrame(JFrame frame) {
		if (rootPanels.containsKey(frame)) {
			return rootPanels.get(frame);
		}
		return null;
	}

	public static Dockable findDockableAtScreenPos(Point screenPos) {
		JFrame frame = findRootAtScreenPos(screenPos);

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

		while (!(component instanceof Dockable) && component.getParent() != null) {
			component = component.getParent();
		}

		// didn't find a Dockable, return null
		if (!(component instanceof Dockable)) {
			return null;
		}
		return (Dockable) component;
	}

	public static DockingPanel findDockingPanelAtScreenPos(Point screenPos) {
		JFrame frame = findRootAtScreenPos(screenPos);

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

	// TODO support docking to non-CENTER of tabbed group
	// TODO allow setting the split weight somehow
	public static void dock(JFrame frame, Dockable dockable) {
		dock(frame, dockable, DockingRegion.CENTER);
	}

	public static void dock(JFrame frame, Dockable dockable, DockingRegion region) {
		if (frameToDispose != null) {
			frameToDispose.dispose();
			frameToDispose = null;
		}

		RootDockingPanel root = rootPanels.get(frame);

		if (root == null) {
			throw new DockableRegistrationFailureException("Frame does not have a RootDockingPanel: " + frame);
		}

		appendDockable(root, dockable, region);
	}

	public static void dock(Dockable dockable, DockingRegion region) {

	}

	public static void undock(Dockable dockable) {
		// find the right panel for this and undock it
		for (JFrame frame : rootPanels.keySet()) {
			RootDockingPanel root = rootPanels.get(frame);

			if (root.undock(dockable)) {
				if (root.getPanel() instanceof DockedSimplePanel) {
					rootPanels.remove(frame);

					// don't dispose it here, or it'll mess up the mouseMove for FloatListener
					frameToDispose = frame;
					frame.setVisible(false);
				}
				else if (root.getPanel() instanceof DockedSplitPanel) {
					DockedSplitPanel splitPanel = (DockedSplitPanel) root.getPanel();

					// no longer need the split, get the panel that's still left and turn it into a simple panel
					if (!splitPanel.hasDockables()) {
						if (splitPanel.getLeft() != null) {
							root.setPanel(splitPanel.getLeft());
						}
						else if (splitPanel.getRight() != null) {
							root.setPanel(splitPanel.getRight());
						}
					}
				}
				else if (root.getPanel() instanceof DockedTabbedPanel) {
					DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();

					// no longer need tabs, switch back to DockedSimplePanel
					if (tabbedPanel.getPanelCount() == 1) {
						root.setPanel(new DockedSimplePanel(root, tabbedPanel.getPanel(0)));
					}

				}
				return;
			}
		}
	}

	private static void appendDockable(RootDockingPanel root, Dockable dockable, DockingRegion region) {
		if (root.getPanel() == null) {
			root.setPanel(new DockedSimplePanel(root, new DockableWrapper(dockable)));
		}
		else if (root.getPanel() instanceof DockedSimplePanel) {
			DockedSimplePanel first = (DockedSimplePanel) root.getPanel();

			if (region == DockingRegion.CENTER) {
				DockedTabbedPanel tabbedPanel = new DockedTabbedPanel(root);

				tabbedPanel.addPanel(first.getDockable());
				tabbedPanel.addPanel(new DockableWrapper(dockable));

				root.setPanel(tabbedPanel);
			}
			else {
				DockedSplitPanel split = new DockedSplitPanel();

				if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
					split.setLeft(first);
					split.setRight(new DockedSimplePanel(split, new DockableWrapper(dockable)));
				}
				else {
					split.setLeft(new DockedSimplePanel(split, new DockableWrapper(dockable)));
					split.setRight(first);
				}

				if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
					split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				}
				else {
					split.setOrientation(JSplitPane.VERTICAL_SPLIT);
				}

				root.setPanel(split);
			}
		}
		else if (root.getPanel() instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();

			tabbedPanel.addPanel(new DockableWrapper(dockable));
		}
	}
}
