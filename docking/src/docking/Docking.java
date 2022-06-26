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
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// TODO find a good solution for where to dock new dockables. For example, I might select a view menu item which docks a certain dockable, that dockable should go in a logical location which is entirely app dependent (might depend on what other dockables are docked)

// TODO support pinning dockables, this should also support icons so that text or an icon can be shown on the toolbar button

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking implements ComponentListener, WindowStateListener {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private final Map<String, DockableWrapper> dockables = new HashMap<>();

	private final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	private JFrame frameToDispose = null;

	private final JFrame mainFrame;

	private static Docking instance;

	private DockingPanel activePanel = null;

	private final Map<JFrame, DockingLayout> maximizeRestoreLayout = new HashMap<>();

	public Docking(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		instance = this;
		FloatListener.reset();
		frameBorderSize = new Dimension(0, 0);

		// use an AWT event listener to set a border around the dockable that the mouse is currently over
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
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
		}, AWTEvent.MOUSE_EVENT_MASK);
	}

	private void setSelectedBorder() {
		Color color = UIManager.getColor("Component.focusColor");
		activePanel.setBorder(BorderFactory.createLineBorder(color, 2));
	}

	private void setNotSelectedBorder() {
		Color color = UIManager.getColor("Component.borderColor");

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

	// registration function for DockingPanel
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

		parent.addComponentListener(instance);
		parent.addWindowStateListener(instance);
	}

	// allows the user to configure pinning per frame. by default pinning is only enabled on the frames the docking framework creates
	public static void configurePinning(JFrame frame, int layer, boolean allow) {
		if (!instance.rootPanels.containsKey(frame)) {
			throw new DockableRegistrationFailureException("No root panel for frame has been registered.");
		}

		RootDockingPanel root = rootForFrame(frame);
		root.setPinningSupported(allow);
		root.setPinningLayer(layer);
	}

	public static boolean pinningAllowed(Dockable dockable) {
		RootDockingPanel root = rootForFrame(findFrameForDockable(dockable));

		return dockable.allowPinning() && root.isPinningSupported();
	}

	public static void deregisterDockingPanel(JFrame parent) {
		if (instance.rootPanels.containsKey(parent)) {
			RootDockingPanel root = instance.rootPanels.get(parent);

			undockComponents(root);
		}

		instance.rootPanels.remove(parent);

		parent.removeComponentListener(instance);
		parent.removeWindowStateListener(instance);
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
		return getWrapper(dockable).getFrame();
	}

	public static RootDockingPanel rootForFrame(JFrame frame) {
		if (instance.rootPanels.containsKey(frame)) {
			return instance.rootPanels.get(frame);
		}
		throw new DockableRegistrationFailureException("No root panel for frame has been registered.");
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

	public static void dock(String persistentID, JFrame frame) {
		dock(getDockable(persistentID), frame, DockingRegion.CENTER);
	}

	public static void dock(Dockable dockable, JFrame frame) {
		dock(dockable, frame, DockingRegion.CENTER);
	}

	public static void dock(String persistentID, JFrame frame, DockingRegion region) {
		dock(getDockable(persistentID), frame, region, 0.5);
	}

	public static void dock(Dockable dockable, JFrame frame, DockingRegion region) {
		dock(dockable, frame, region, 0.5);
	}

	public static void dock(String persistentID, JFrame frame, DockingRegion region, double dividerProportion) {
		dock(getDockable(persistentID), frame, region, dividerProportion);
	}

	public static void dock(Dockable dockable, JFrame frame, DockingRegion region, double dividerProportion) {
		if (instance.frameToDispose != null) {
			instance.frameToDispose.dispose();
			instance.frameToDispose = null;
		}

		RootDockingPanel root = instance.rootPanels.get(frame);

		if (root == null) {
			throw new DockableRegistrationFailureException("Frame does not have a RootDockingPanel: " + frame);
		}

		root.dock(dockable, region, dividerProportion);

		getWrapper(dockable).setFrame(frame);

		// fire a docked event when the component is actually added
		DockingListeners.fireDockedEvent(dockable);

		AppState.persist();
	}

	public static void dock(String source, String target, DockingRegion region) {
		dock(getDockable(source), getDockable(target), region, 0.5);
	}

	public static void dock(String source, Dockable target, DockingRegion region) {
		dock(getDockable(source), target, region, 0.5);
	}

	public static void dock(Dockable source, String target, DockingRegion region) {
		dock(source, getDockable(target), region, 0.5);
	}

	public static void dock(Dockable source, Dockable target, DockingRegion region) {
		dock(source, target, region, 0.5);
	}

	public static void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion) {
		DockableWrapper wrapper = Docking.getWrapper(target);
		wrapper.getParent().dock(source, region, dividerProportion);

		getWrapper(source).setFrame(wrapper.getFrame());

		DockingListeners.fireDockedEvent(source);

		AppState.persist();
	}

	public static void newWindow(Dockable dockable) {
		Point location = ((JComponent) dockable).getLocationOnScreen();
		Dimension size = ((JComponent) dockable).getSize();
		size.width += frameBorderSize.width;
		size.height += frameBorderSize.height;

		FloatingFrame frame = new FloatingFrame(location, size, JFrame.NORMAL);

		undock(dockable);
		dock(dockable, frame);
	}

	public static void bringToFront(Dockable dockable) {
		if (!isDocked(dockable)) {
			return;
		}

		JFrame frame = findFrameForDockable(dockable);
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);

		if (getWrapper(dockable).getParent() instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) getWrapper(dockable).getParent();
			tabbedPanel.bringToFront(dockable);
		}
	}

	public static void undock(String persistentID) {
		undock(getDockable(persistentID));
	}

	public static void undock(Dockable dockable) {
		if (!isDocked(dockable)) {
			return;
		}

		JFrame frame = findFrameForDockable(dockable);

		RootDockingPanel root = rootForFrame(frame);

		DockableWrapper wrapper = getWrapper(dockable);

		wrapper.getParent().undock(dockable);
		wrapper.setFrame(null);

		DockingListeners.fireUndockedEvent(dockable);

		if (frame != null && root != null && canDisposeFrame(frame) && root.isEmpty()) {
			deregisterDockingPanel(frame);
			frame.dispose();
		}

		AppState.persist();
	}

	public static boolean isDocked(String persistentID) {
		return isDocked(getDockable(persistentID));
	}

	public static boolean isDocked(Dockable dockable) {
		return getWrapper(dockable).getParent() != null;
	}

	public static boolean canDisposeFrame(JFrame frame) {
		return frame != instance.mainFrame && !instance.maximizeRestoreLayout.containsKey(frame);
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

		DockingLayout maxLayout = instance.maximizeRestoreLayout.get(frame);

		if (maxLayout != null) {
			return maxLayout;
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

		AppState.setPaused(true);

		// setup main frame
		restoreLayoutFromFull(instance.mainFrame, layout.getMainFrameLayout());

		// setup rest of floating frames from layout
		for (DockingLayout frameLayout : layout.getFloatingFrameLayouts()) {
			FloatingFrame frame = new FloatingFrame(frameLayout.getLocation(), frameLayout.getSize(), frameLayout.getState());

			restoreLayoutFromFull(frame, frameLayout);
		}

		AppState.setPaused(false);
		AppState.persist();

		// everything has been restored, go through the list of dockables and fire docked events for the ones that are docked
		for (DockableWrapper wrapper : instance.dockables.values()) {
			if (isDocked(wrapper.getDockable())) {
				DockingListeners.fireDockedEvent(wrapper.getDockable());
			}
		}
	}

	private static void restoreLayoutFromFull(JFrame frame, DockingLayout layout) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		frame.setLocation(layout.getLocation());
		frame.setSize(layout.getSize());
		frame.setExtendedState(layout.getState());

		undockComponents(root);

		if (layout.getMaximizedDockable() != null) {
			DockableWrapper wrapper = getWrapper(getDockable(layout.getMaximizedDockable()));
			wrapper.setMaximized(true);
			DockingListeners.fireMaximizeEvent(wrapper.getDockable(), true);

			layout.setMaximizedDockable(null);

			instance.maximizeRestoreLayout.put(frame, layout);

			dock(wrapper.getDockable(), frame);
		}
		else {
			root.setPanel(restoreState(layout.getRootNode(), frame));
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

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(layout.getRootNode(), frame));

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	public static void restoreState(JFrame frame, RootDockState state) {
		RootDockingPanel root = rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		undockComponents(root);

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(state.getState(), frame));

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	private static DockingPanel restoreState(DockingState state, JFrame frame) {
		if (state instanceof PanelState) {
			return restoreSimple((PanelState) state, frame);
		}
		else if (state instanceof SplitState) {
			return restoreSplit((SplitState) state, frame);
		}
		else if (state instanceof TabState) {
			return restoreTabbed((TabState) state, frame);
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(SplitState state, JFrame frame) {
		DockedSplitPanel panel = new DockedSplitPanel(frame);

		panel.setLeft(restoreState(state.getLeft(), frame));
		panel.setRight(restoreState(state.getRight(), frame));
		panel.setOrientation(state.getOrientation());
		panel.setDividerLocation(state.getDividerLocation());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(TabState state, JFrame frame) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : state.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new RuntimeException("Dockable with persistent ID " + persistentID + " does not exist.");
			}

			undock(dockable);

			DockableWrapper wrapper = getWrapper(dockable);
			wrapper.setFrame(frame);

			panel.addPanel(wrapper);
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(PanelState state, JFrame frame) {
		Dockable dockable = getDockable(state.getPersistentID());

		if (dockable == null) {
			throw new RuntimeException("Dockable with persistent ID " + state.getPersistentID() + " does not exist.");
		}

		undock(dockable);

		DockableWrapper wrapper = getWrapper(dockable);
		wrapper.setFrame(frame);

		return new DockedSimplePanel(wrapper);
	}

	private static DockingPanel restoreState(DockingLayoutNode node, JFrame frame) {
		if (node instanceof DockingSimplePanelNode) {
			return restoreSimple((DockingSimplePanelNode) node, frame);
		}
		else if (node instanceof DockingSplitPanelNode) {
			return restoreSplit((DockingSplitPanelNode) node, frame);
		}
		else if (node instanceof DockingTabPanelNode) {
			return restoreTabbed((DockingTabPanelNode) node, frame);
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(DockingSplitPanelNode node, JFrame frame) {
		DockedSplitPanel panel = new DockedSplitPanel(frame);

		panel.setLeft(restoreState(node.getLeft(), frame));
		panel.setRight(restoreState(node.getRight(), frame));
		panel.setOrientation(node.getOrientation());
		panel.setDividerLocation(node.getDividerProportion());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(DockingTabPanelNode node, JFrame frame) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : node.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new RuntimeException("Dockable with persistent ID " + persistentID + " does not exist.");
			}

			undock(dockable);

			DockableWrapper wrapper = getWrapper(dockable);
			wrapper.setFrame(frame);

			panel.addPanel(wrapper);
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(DockingSimplePanelNode node, JFrame frame) {
		Dockable dockable = getDockable(node.persistentID());

		if (dockable == null) {
			throw new RuntimeException("Dockable with persistent ID " + node.persistentID() + " does not exist.");
		}

		undock(dockable);

		DockableWrapper wrapper = getWrapper(dockable);
		wrapper.setFrame(frame);

		return new DockedSimplePanel(wrapper);
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

				DockingListeners.fireUndockedEvent(dockable);
				DockingListeners.fireAutoUndockedEvent(dockable);
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
		RootDockingPanel root = rootForFrame(frame);

		// can only maximize one panel per root
		if (!instance.maximizeRestoreLayout.containsKey(frame) && root != null) {
			getWrapper(dockable).setMaximized(true);
			DockingListeners.fireMaximizeEvent(dockable, true);

			DockingLayout layout = getCurrentLayout(frame);
			layout.setMaximizedDockable(dockable.persistentID());

			instance.maximizeRestoreLayout.put(frame, layout);

			undockComponents(root);

			dock(dockable, frame);
		}
	}

	public static void minimize(Dockable dockable) {
		JFrame frame = findFrameForDockable(dockable);

		// can only minimize if already maximized
		if (instance.maximizeRestoreLayout.containsKey(frame)) {
			getWrapper(dockable).setMaximized(false);
			DockingListeners.fireMaximizeEvent(dockable, false);

			setLayout(frame, instance.maximizeRestoreLayout.get(frame));

			instance.maximizeRestoreLayout.remove(frame);

			// everything has been restored, go through the list of dockables and fire docked events for the ones that are docked
			List<DockableWrapper> dockables = instance.dockables.values().stream()
					.filter(wrapper -> wrapper.getFrame() == frame)
					.collect(Collectors.toList());

			for (DockableWrapper wrapper : dockables) {
				DockingListeners.fireDockedEvent(wrapper.getDockable());
			}
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void windowStateChanged(WindowEvent e) {
		AppState.persist();
	}
}
