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

import ModernDocking.exception.DockableNotFoundException;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.internal.*;
import ModernDocking.layouts.*;
import ModernDocking.persist.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DockingState {
	// cached layout for when a maximized dockable is minimized
	public static final Map<Window, DockingLayout> maximizeRestoreLayout = new HashMap<>();

	public static RootDockState getRootState(Window window) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (root == null) {
			throw new RuntimeException("Root for window does not exist: " + window);
		}

		return new RootDockState(root);
	}

	public static DockingLayout getCurrentLayout(Window window) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + window);
		}

		DockingLayout maxLayout = maximizeRestoreLayout.get(window);

		if (maxLayout != null) {
			return maxLayout;
		}

		return DockingLayouts.layoutFromRoot(root);
	}

	public static FullAppLayout getFullLayout() {
		FullAppLayout layout = new FullAppLayout();

		layout.setMainFrame(getCurrentLayout(Docking.getInstance().getMainWindow()));

		for (Window frame : Docking.getInstance().getRootPanels().keySet()) {
			if (frame != Docking.getInstance().getMainWindow()) {
				layout.addFrame(getCurrentLayout(frame));
			}
		}

		return layout;
	}

	public static void restoreFullLayout(FullAppLayout layout) {
		// get rid of all existing windows and undock all dockables
		Set<Window> windows = new HashSet<>(Docking.getInstance().getRootPanels().keySet());
		for (Window window : windows) {
			if (window != Docking.getInstance().getMainWindow()) {
				DockingComponentUtils.undockComponents(window);
				window.dispose();
			}
		}

		AppState.setPaused(true);

		// setup main frame
		restoreLayoutFromFull(Docking.getInstance().getMainWindow(), layout.getMainFrameLayout());

		// setup rest of floating windows from layout
		for (DockingLayout frameLayout : layout.getFloatingFrameLayouts()) {
			FloatingFrame frame = new FloatingFrame(frameLayout.getLocation(), frameLayout.getSize(), frameLayout.getState());

			restoreLayoutFromFull(frame, frameLayout);
		}

		AppState.setPaused(false);
		AppState.persist();

		DockingInternal.fireDockedEventForAll();
	}

	private static void restoreLayoutFromFull(Window window, DockingLayout layout) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (root == null) {
			throw new RuntimeException("Root for window does not exist: " + window);
		}

		window.setLocation(layout.getLocation());
		window.setSize(layout.getSize());

		if (window instanceof JFrame) {
			((JFrame) window).setExtendedState(layout.getState());
		}

		DockingComponentUtils.undockComponents(root);

		root.setPanel(restoreState(layout.getRootNode(), window));

		// undock and destroy any failed dockables
		undockFailedComponents(root);

		for (String id : layout.getWestUnpinnedToolbarIDs()) {
			Dockable dockable = getDockable(id);
			root.setDockableUnpinned(dockable, DockableToolbar.Location.WEST);
			root.hideUnpinnedPanels();
			DockingInternal.getWrapper(dockable).setUnpinned(true);
		}

		for (String id : layout.getEastUnpinnedToolbarIDs()) {
			Dockable dockable = getDockable(id);
			root.setDockableUnpinned(dockable, DockableToolbar.Location.EAST);
			root.hideUnpinnedPanels();
			DockingInternal.getWrapper(dockable).setUnpinned(true);
		}

		for (String id : layout.getSouthUnpinnedToolbarIDs()) {
			Dockable dockable = getDockable(id);
			root.setDockableUnpinned(dockable, DockableToolbar.Location.SOUTH);
			root.hideUnpinnedPanels();
			DockingInternal.getWrapper(dockable).setUnpinned(true);
		}

		if (layout.getMaximizedDockable() != null) {
			Docking.maximize(getDockable(layout.getMaximizedDockable()));
		}
	}

	public static void setLayout(Window window, DockingLayout layout) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (root == null) {
			throw new RuntimeException("Root for window does not exist: " + window);
		}

		window.setLocation(layout.getLocation());
		window.setSize(layout.getSize());

		if (window instanceof JFrame) {
			((JFrame) window).setExtendedState(layout.getState());
		}

		DockingComponentUtils.undockComponents(root);

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(layout.getRootNode(), window));

		// undock and destroy any failed dockables
		undockFailedComponents(root);

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	public static void restoreState(Window window, RootDockState state) {
		RootDockingPanel root = DockingComponentUtils.rootForWindow(window);

		if (root == null) {
			throw new RuntimeException("Root for window does not exist: " + window);
		}

		DockingComponentUtils.undockComponents(root);

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(state.getState(), window));

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	private static DockingPanel restoreState(DockableState state, Window window) {
		if (state instanceof PanelState) {
			return restoreSimple((PanelState) state, window);
		}
		else if (state instanceof SplitState) {
			return restoreSplit((SplitState) state, window);
		}
		else if (state instanceof TabState) {
			return restoreTabbed((TabState) state, window);
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(SplitState state, Window window) {
		DockedSplitPanel panel = new DockedSplitPanel(window);

		panel.setLeft(restoreState(state.getLeft(), window));
		panel.setRight(restoreState(state.getRight(), window));
		panel.setOrientation(state.getOrientation());
		panel.setDividerLocation(state.getDividerLocation());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(TabState state, Window window) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : state.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new DockableNotFoundException(persistentID);
			}

			Docking.undock(dockable);

			DockableWrapper wrapper = DockingInternal.getWrapper(dockable);
			wrapper.setWindow(window);

			panel.addPanel(wrapper);
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(PanelState state, Window window) {
		Dockable dockable = getDockable(state.getPersistentID());

		if (dockable == null) {
			throw new DockableNotFoundException(state.getPersistentID());
		}

		Docking.undock(dockable);

		DockableWrapper wrapper = DockingInternal.getWrapper(dockable);
		wrapper.setWindow(window);

		return new DockedSimplePanel(wrapper);
	}

	private static DockingPanel restoreState(DockingLayoutNode node, Window window) {
		if (node instanceof DockingSimplePanelNode) {
			return restoreSimple((DockingSimplePanelNode) node, window);
		}
		else if (node instanceof DockingSplitPanelNode) {
			return restoreSplit((DockingSplitPanelNode) node, window);
		}
		else if (node instanceof DockingTabPanelNode) {
			return restoreTabbed((DockingTabPanelNode) node, window);
		}
		else if (node == null) {
			// the main window root can contain a null panel if nothing is docked
			return null;
		}
		else {
			throw new RuntimeException("Unknown state type");
		}
	}

	private static DockedSplitPanel restoreSplit(DockingSplitPanelNode node, Window window) {
		DockedSplitPanel panel = new DockedSplitPanel(window);

		panel.setLeft(restoreState(node.getLeft(), window));
		panel.setRight(restoreState(node.getRight(), window));
		panel.setOrientation(node.getOrientation());
		panel.setDividerLocation(node.getDividerProportion());

		return panel;
	}

	private static DockedTabbedPanel restoreTabbed(DockingTabPanelNode node, Window window) {
		DockedTabbedPanel panel = new DockedTabbedPanel();

		for (String persistentID : node.getPersistentIDs()) {
			Dockable dockable = getDockable(persistentID);

			if (dockable == null) {
				throw new DockableNotFoundException(persistentID);
			}

			Docking.undock(dockable);

			DockableWrapper wrapper = DockingInternal.getWrapper(dockable);
			wrapper.setWindow(window);

			panel.addPanel(wrapper);
		}

		if (!node.getSelectedTabID().isEmpty()) {
			panel.bringToFront(getDockable(node.getSelectedTabID()));
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(DockingSimplePanelNode node, Window window) {
		Dockable dockable = getDockable(node.persistentID());

		if (dockable == null) {
			throw new DockableNotFoundException(node.persistentID());
		}

		Docking.undock(dockable);

		DockableWrapper wrapper = DockingInternal.getWrapper(dockable);
		wrapper.setWindow(window);

		return new DockedSimplePanel(wrapper);
	}

	private static Dockable getDockable(String persistentID) {
		try {
			return DockingInternal.getDockable(persistentID);
		}
		catch (DockableRegistrationFailureException ignore) {
		}
		return new FailedDockable(persistentID);
	}

	private static void undockFailedComponents(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof FailedDockable) {
				FailedDockable dockable = (FailedDockable) component;
				Docking.undock(getDockable(dockable.persistentID()));
				dockable.destroy();
			}
			else if (component instanceof Container) {
				undockFailedComponents((Container) component);
			}
		}
	}
}
