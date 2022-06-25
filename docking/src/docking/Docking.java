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
import layouts.*;
import persist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.*;

// TODO persistence (saving and loading) -- in memory done, next up persist to file
// TODO saving/loading relies on divider absolute positions. if the dialog is resized before restoring then it looks wrong

// TODO perspectives/views/layouts, probably calling them "layouts"

// TODO allow the app to set the divider location when docking dockables

// TODO add buttons for maximize/minimize, pin and close

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private final Map<String, DockableWrapper> dockables = new HashMap<>();

	private final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	private JFrame frameToDispose = null;

	private final JFrame mainFrame;

	private static Docking instance;

	private DockingPanel activePanel = null;

	private final Map<JFrame, RootDockState> maximizeRestoreState = new HashMap<>();

	public Docking(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		instance = this;
		FloatListener.reset();
		frameBorderSize = new Dimension(0, 0);

		long eventMask = AWTEvent.MOUSE_EVENT_MASK;

		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
//				System.out.println(e.getID());
			if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_EXITED) {
				DockingPanel dockable = findDockingPanelAtScreenPos(((MouseEvent) e).getLocationOnScreen());

				if (activePanel != null && dockable == null) {
					setNotSelectedBorder();
					activePanel = null;
				}

				if (activePanel != dockable && (dockable instanceof DockedSimplePanel || dockable instanceof DockedTabbedPanel)) {
					if (activePanel != null) {
						setNotSelectedBorder();
					}
					activePanel = dockable;
					setSelectedBorder();
				}

			}
		}, eventMask);
	}

	private void setSelectedBorder() {
		Color color = UIManager.getLookAndFeelDefaults().getColor("Component.focusColor");
		activePanel.setBorder(BorderFactory.createLineBorder(color, 2));
	}

	private void setNotSelectedBorder() {
		Color color = UIManager.getLookAndFeelDefaults().getColor("Component.borderColor");

		activePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 1),
						BorderFactory.createLineBorder(color, 1)
				)
		);
	}

	public static void registerDockable(Dockable dockable) {
		if (instance.dockables.containsKey(dockable.persistentID())) {
			throw new DockableRegistrationFailureException("Registration for Dockable failed. Persistent ID " + dockable.persistentID() + " already exists.");
		}
		((Component) dockable).setName(dockable.persistentID());
		instance.dockables.put(dockable.persistentID(), new DockableWrapper(dockable));
	}

	// Dockables must be deregistered so it can be properly disposed
	public static void deregisterDockable(Dockable dockable) {
		instance.dockables.remove(dockable.persistentID());
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

		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}
		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingFrame(parent, panel);
	}

	// TODO add a possible listener for this. I'd like a way to listen for panels being auto undocked and being able to redock them somewhere else depending on what they are
	// TODO ... for example: dockable in a floating frame, closing it would dock it back into the main window in its default location (root east, for example)
	public static void deregisterDockingPanel(JFrame parent) {
		if (instance.rootPanels.containsKey(parent)) {
			RootDockingPanel root = instance.rootPanels.get(parent);

			undockComponents(root);
		}

		instance.rootPanels.remove(parent);
	}

	private static void undockComponents(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof Dockable) {
				undock((Dockable) component);
			}
			else if (component instanceof Container) {
				undockComponents((Container) component);
			}
		}
	}

	public static JFrame findRootAtScreenPos(Point screenPos) {
		for (JFrame frame : instance.rootPanels.keySet()) {
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
				if (instance.rootPanels.containsKey(parent)) {
					return (JFrame) parent;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	public static RootDockingPanel rootForFrame(JFrame frame) {
		if (instance.rootPanels.containsKey(frame)) {
			return instance.rootPanels.get(frame);
		}
		return null;
	}

	public static JFrame frameForRoot(RootDockingPanel root) {
		Optional<JFrame> first = instance.rootPanels.keySet().stream()
				.filter(frame -> instance.rootPanels.get(frame) == root)
				.findFirst();

		return first.orElse(null);
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
		if (instance.frameToDispose != null) {
			instance.frameToDispose.dispose();
			instance.frameToDispose = null;
		}

		RootDockingPanel root = instance.rootPanels.get(frame);

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
		return frame != instance.mainFrame;
	}

	public static Dockable getDockable(String persistentID) {
		if (instance.dockables.containsKey(persistentID)) {
			return instance.dockables.get(persistentID).getDockable();
		}
		return null;
	}

	// internal function to get the dockable wrapper
	static DockableWrapper getWrapper(Dockable dockable) {
		if (instance.dockables.containsKey(dockable.persistentID())) {
			return instance.dockables.get(dockable.persistentID());
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

	public static DockingLayout getCurrentLayout(JFrame frame) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		return DockingLayouts.layoutFromRoot(root);
	}

	public static FullAppLayout getFullLayout() {
		FullAppLayout layout = new FullAppLayout();

		layout.setMainFrame(getCurrentLayout(instance.mainFrame));

		for (JFrame frame : instance.rootPanels.keySet()) {
			if (frame != instance.mainFrame) {
				layout.addFrame(getCurrentLayout(frame));
			}
		}

		return layout;
	}

	public static void restoreFullLayout(FullAppLayout layout) {
		// get rid of all existing frames and undock all dockables
		Set<JFrame> frames = new HashSet<>(instance.rootPanels.keySet());
		for (JFrame frame : frames) {
			if (frame != instance.mainFrame) {
				undockComponents(frame);
				frame.dispose();
			}
		}

		// setup main frame
		setLayout(instance.mainFrame, layout.getMainFrameLayout());

		// setup rest of floating frames from layout
		for (DockingLayout frameLayout : layout.getFloatingFrameLayouts()) {
			FloatingFrame frame = new FloatingFrame(frameLayout.getLocation(), frameLayout.getSize(), frameLayout.getState());
			frame.setVisible(true);

			setLayout(frame, frameLayout);
		}
	}

	public static void setLayout(JFrame frame, DockingLayout layout) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		frame.setLocation(layout.getLocation());
		frame.setSize(layout.getSize());
		frame.setExtendedState(layout.getState());

		undockComponents(root);

		root.setPanel(restoreState(layout.getRootNode()));
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

	private static DockingPanel restoreState(DockingLayoutNode node) {
		if (node instanceof DockingSimplePanelNode) {
			return restoreSimple((DockingSimplePanelNode) node);
		}
		else if (node instanceof DockingSplitPanelNode) {
			return restoreSplit((DockingSplitPanelNode) node);
		}
		else if (node instanceof DockingTabPanelNode) {
			return restoreTabbed((DockingTabPanelNode) node);
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(DockingSplitPanelNode node) {
		DockedSplitPanel panel = new DockedSplitPanel();

		panel.setLeft(restoreState(node.getLeft()));
		panel.setRight(restoreState(node.getRight()));
		panel.setOrientation(node.getOrientation());
//		panel.setDividerLocation(node.getDividerLocation());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(DockingTabPanelNode node) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : node.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new RuntimeException("Dockable with persistent ID " + persistentID + " does not exist.");
			}

			undock(dockable);

			panel.addPanel(getWrapper(dockable));
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(DockingSimplePanelNode node) {
		Dockable dockable = getDockable(node.persistentID());

		if (dockable == null) {
			throw new RuntimeException("Dockable with persistent ID " + node.persistentID() + " does not exist.");
		}

		undock(dockable);

		return new DockedSimplePanel(getWrapper(dockable));
	}

	public static void removeIllegalFloats(JFrame frame) {
		// remove panels from frame if they return false for allowFloating() and there are no other dockables in the frame
		RootDockingPanel root = rootForFrame(frame);

		if (Docking.canDisposeFrame(frame) && root != null) {
			if (shouldUndock(root)) {
				undockIllegalFloats(root);
			}
		}
	}

	private static boolean shouldUndock(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof Dockable) {
				if (((Dockable) component).floatingAllowed()) {
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
			if (component instanceof Dockable) {
				Dockable dockable = (Dockable) component;

				DockableWrapper wrapper = getWrapper(dockable);
				wrapper.getParent().undock(dockable);
			}
			else if (component instanceof Container) {
				undockIllegalFloats((Container) component);
			}
		}
	}

	public static boolean isMaximized(Dockable dockable) {
		return getWrapper(dockable).isMaximized();
	}

	public static void maximize(Dockable dockable) {
		JFrame frame = findFrameForDockable(dockable);

		// can only maximize one panel per root
		if (!instance.maximizeRestoreState.containsKey(frame)) {
			getWrapper(dockable).setMaximized(true);
			instance.maximizeRestoreState.put(frame, getRootState(frame));
			undockComponents(rootForFrame(frame));
			dock(dockable, frame);
		}
	}

	public static void minimize(Dockable dockable) {
		JFrame frame = findFrameForDockable(dockable);

		// can only minimize if already maximized
		if (instance.maximizeRestoreState.containsKey(frame)) {
			getWrapper(dockable).setMaximized(false);
			restoreState(frame, instance.maximizeRestoreState.get(frame));
			instance.maximizeRestoreState.remove(frame);
		}
	}
}
