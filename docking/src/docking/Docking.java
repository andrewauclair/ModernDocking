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
import persist.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// TODO we need to check if the window loses focus and kill the floating dialog. other wise strange things happen. -- Think I've gotten pretty close on this, requires more testing

// TODO persistence (saving and loading) -- in memory done, next up persist to file

// TODO perspectives/views/layouts, probably calling them "layouts"

// TODO allow the app to set the divider location when docking dockables

// TODO make empty root panel look better and add a "RC" Root Center docking handle in case the root is empty

// TODO support maximize (should have icon that shows that you can maximize and then it should change when it is maxed)

// TODO add buttons for maximize/minimize, pin and close

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private static final Map<String, DockableWrapper> dockables = new HashMap<>();

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
		dockables.put(dockable.persistentID(), new DockableWrapper(dockable));
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
	// TODO ... for example: dockable in a floating frame, closing it would dock it back into the main window in its default location (root east, for example)
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

	// TODO allow setting the split weight somehow
	public static void dock(Dockable dockable, JFrame frame) {
		dock(dockable, frame, DockingRegion.CENTER);
	}

	public static void dock(Dockable dockable, JFrame frame, DockingRegion region) {
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

	public static void dock(Dockable source, Dockable target, DockingRegion region) {
		DockableWrapper wrapper = Docking.getWrapper(target);
		wrapper.getParent().dock(source, region);
	}

	public static void undock(Dockable dockable) {
		JFrame frame = findFrameForDockable(dockable);

		RootDockingPanel root = rootForFrame(frame);

		DockableWrapper wrapper = getWrapper(dockable);
		wrapper.getParent().undock(dockable);

		if (frame != null && root != null && canDisposeFrame(frame) && root.isEmpty()) {
			deregisterDockingPanel(frame);
			frame.dispose();
		}
	}

	public static boolean canDisposeFrame(JFrame frame) {
		return frame != mainFrame;
	}

	public static Dockable getDockable(String persistentID) {
		if (dockables.containsKey(persistentID)) {
			return dockables.get(persistentID).getDockable();
		}
		return null;
	}

	// internal function to get the dockable wrapper
	static DockableWrapper getWrapper(Dockable dockable) {
		if (dockables.containsKey(dockable.persistentID())) {
			return dockables.get(dockable.persistentID());
		}
		throw new DockableRegistrationFailureException("Dockable with Persistent ID " + dockable.persistentID() + " has not been registered.");
	}

	public static RootDockState getRootState(JFrame frame) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		return new RootDockState(root);
	}

	public static void restoreState(JFrame frame, RootDockState state) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		undockComponents(root);

		root.setPanel(restoreState(state.getState()));
	}

	private static DockingPanel restoreState(DockingState state) {
		if (state instanceof PanelState) {
			return restoreSimple((PanelState) state);
		}
		else if (state instanceof SplitState) {
			return restoreSplit((SplitState) state);
		}
		else if (state instanceof TabState) {
			return restoreTabbed((TabState) state);
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(SplitState state) {
		DockedSplitPanel panel = new DockedSplitPanel();

		panel.setLeft(restoreState(state.getLeft()));
		panel.setRight(restoreState(state.getRight()));
		panel.setOrientation(state.getOrientation());
		panel.setDividerLocation(state.getDividerLocation());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(TabState state) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : state.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new RuntimeException("Dockable with persistent ID " + persistentID + " does not exist.");
			}

			undock(dockable);

			panel.addPanel(getWrapper(dockable));
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(PanelState state) {
		Dockable dockable = getDockable(state.getPersistentID());

		if (dockable == null) {
			throw new RuntimeException("Dockable with persistent ID " + state.getPersistentID() + " does not exist.");
		}

		undock(dockable);

		return new DockedSimplePanel(getWrapper(dockable));
	}
}
