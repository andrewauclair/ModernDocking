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
package docking;

import exception.DockableRegistrationFailureException;
import floating.FloatListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// TODO we need to check if the window loses focus and kill the floating dialog. other wise strange things happen. -- Think I've gotten pretty close on this, requires more testing

// TODO persistence (saving and loading)

// TODO perspectives/views/layouts, probably calling them "layouts"

// TODO programmatic layout. we can dock/undock pretty well from a user perspective. now we need that ability from the programming side. -- done for root, need to allow the app to get a panel and call its dock/undock functions directly

// TODO allow the app to set the divider resize weight somehow

// TODO make empty root panel look better and add a "RC" Root Center docking handle in case the root is empty

// TODO if we somehow always had a root center option we could dock to a split which would end up with a tab group with a split and simple panel

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private static final Map<String, Dockable> dockables = new HashMap<>();

	private static final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	private static JFrame frameToDispose = null;

	private static JFrame mainFrame = null;

	public static void setMainFrame(JFrame frame) {
		mainFrame = frame;
	}

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
		FloatListener.registerDockingFrame(parent, panel);
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
			Rectangle bounds = new Rectangle(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());

			if (bounds.contains(screenPos) && frame.isVisible()) {
//				System.out.println("found frame at: " + screenPos + ", " + bounds);
				return frame;
			}
		}
		return null;
	}

	public static JFrame findFrameForDockable(Dockable dockable) {
		Container parent = ((Component) dockable).getParent();

		while (parent != null) {
			if (parent instanceof JFrame) {
				if (rootPanels.containsKey(parent)) {
					return (JFrame) parent;
				}
			}
			parent = parent.getParent();
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

		root.dock(dockable, region);
	}

	public static void dock(Dockable dockable, DockingRegion region) {

	}

	public static void undock(Dockable dockable) {
		// find the right panel for this and undock it
		for (JFrame frame : rootPanels.keySet()) {
			RootDockingPanel root = rootPanels.get(frame);

//			if (root.undock(dockable)) {
//				if (root.getPanel() instanceof DockedSimplePanel) {
//					rootPanels.remove(frame);
//
//					// don't dispose it here, or it'll mess up the mouseMove for FloatListener
//					frameToDispose = frame;
//					frame.setVisible(false);
//				}
//				else if (root.getPanel() instanceof DockedSplitPanel) {
//					DockedSplitPanel splitPanel = (DockedSplitPanel) root.getPanel();
//
//					// no longer need the split, get the panel that's still left and turn it into a simple panel
//					if (!splitPanel.hasDockables()) {
//						if (splitPanel.getLeft() != null) {
//							root.setPanel(splitPanel.getLeft());
//						}
//						else if (splitPanel.getRight() != null) {
//							root.setPanel(splitPanel.getRight());
//						}
//					}
//				}
//				else if (root.getPanel() instanceof DockedTabbedPanel) {
//					DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();
//
//					// no longer need tabs, switch back to DockedSimplePanel
//					if (tabbedPanel.getPanelCount() == 1) {
//						root.setPanel(new DockedSimplePanel(root, tabbedPanel.getPanel(0)));
//					}
//
//				}
//				return;
//			}
		}
	}

	public static boolean canDisposeFrame(JFrame frame) {
		return frame != mainFrame;
	}
}
