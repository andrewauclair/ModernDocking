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
import ModernDocking.internal.*;
import ModernDocking.layouts.*;
import ModernDocking.persist.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ModernDocking.internal.DockingInternal.getDockable;
import static ModernDocking.internal.DockingInternal.getWrapper;

public class DockingState {
	// cached layout for when a maximized dockable is minimized
	public static final Map<JFrame, DockingLayout> maximizeRestoreLayout = new HashMap<>();

	public static RootDockState getRootState(JFrame frame) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		return new RootDockState(root);
	}

	public static DockingLayout getCurrentLayout(JFrame frame) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		DockingLayout maxLayout = maximizeRestoreLayout.get(frame);

		if (maxLayout != null) {
			return maxLayout;
		}

		return DockingLayouts.layoutFromRoot(root);
	}

	public static FullAppLayout getFullLayout() {
		FullAppLayout layout = new FullAppLayout();

		layout.setMainFrame(getCurrentLayout(Docking.getInstance().getMainFrame()));

		for (JFrame frame : Docking.getInstance().getRootPanels().keySet()) {
			if (frame != Docking.getInstance().getMainFrame()) {
				layout.addFrame(getCurrentLayout(frame));
			}
		}

		return layout;
	}

	public static void restoreFullLayout(FullAppLayout layout) {
		// get rid of all existing frames and undock all dockables
		Set<JFrame> frames = new HashSet<>(Docking.getInstance().getRootPanels().keySet());
		for (JFrame frame : frames) {
			if (frame != Docking.getInstance().getMainFrame()) {
				DockingComponentUtils.undockComponents(frame);
				frame.dispose();
			}
		}

		AppState.setPaused(true);

		// setup main frame
		restoreLayoutFromFull(Docking.getInstance().getMainFrame(), layout.getMainFrameLayout());

		// setup rest of floating frames from layout
		for (DockingLayout frameLayout : layout.getFloatingFrameLayouts()) {
			FloatingFrame frame = new FloatingFrame(frameLayout.getLocation(), frameLayout.getSize(), frameLayout.getState());

			restoreLayoutFromFull(frame, frameLayout);
		}

		AppState.setPaused(false);
		AppState.persist();

		DockingInternal.fireDockedEventForAll();
	}

	private static void restoreLayoutFromFull(JFrame frame, DockingLayout layout) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		frame.setLocation(layout.getLocation());
		frame.setSize(layout.getSize());
		frame.setExtendedState(layout.getState());

		DockingComponentUtils.undockComponents(root);

		if (layout.getMaximizedDockable() != null) {
			DockableWrapper wrapper = getWrapper(getDockable(layout.getMaximizedDockable()));
			wrapper.setMaximized(true);
			DockingListeners.fireMaximizeEvent(wrapper.getDockable(), true);

			layout.setMaximizedDockable(null);

			maximizeRestoreLayout.put(frame, layout);

			Docking.dock(wrapper.getDockable(), frame);
		}
		else {
			root.setPanel(restoreState(layout.getRootNode(), frame));
		}
	}

	public static void setLayout(JFrame frame, DockingLayout layout) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		frame.setLocation(layout.getLocation());
		frame.setSize(layout.getSize());
		frame.setExtendedState(layout.getState());

		DockingComponentUtils.undockComponents(root);

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(layout.getRootNode(), frame));

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	public static void restoreState(JFrame frame, RootDockState state) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (root == null) {
			throw new RuntimeException("Root for frame does not exist: " + frame);
		}

		DockingComponentUtils.undockComponents(root);

		boolean paused = AppState.isPaused();
		AppState.setPaused(true);

		root.setPanel(restoreState(state.getState(), frame));

		AppState.setPaused(paused);

		if (!paused) {
			AppState.persist();
		}
	}

	private static DockingPanel restoreState(DockableState state, JFrame frame) {
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
				throw new DockableNotFoundException(persistentID);
			}

			Docking.undock(dockable);

			DockableWrapper wrapper = getWrapper(dockable);
			wrapper.setFrame(frame);

			panel.addPanel(wrapper);
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(PanelState state, JFrame frame) {
		Dockable dockable = getDockable(state.getPersistentID());

		if (dockable == null) {
			throw new DockableNotFoundException(state.getPersistentID());
		}

		Docking.undock(dockable);

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
		else if (node == null) {
			// the main frame root can contain a null panel if nothing is docked
			return null;
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
				throw new DockableNotFoundException(persistentID);
			}

			Docking.undock(dockable);

			DockableWrapper wrapper = getWrapper(dockable);
			wrapper.setFrame(frame);

			panel.addPanel(wrapper);
		}

		if (!node.getSelectedTabID().isEmpty()) {
			panel.bringToFront(getDockable(node.getSelectedTabID()));
		}

		return panel;
	}

	private static DockedSimplePanel restoreSimple(DockingSimplePanelNode node, JFrame frame) {
		Dockable dockable = getDockable(node.persistentID());

		if (dockable == null) {
			throw new DockableNotFoundException(node.persistentID());
		}

		Docking.undock(dockable);

		DockableWrapper wrapper = getWrapper(dockable);
		wrapper.setFrame(frame);

		return new DockedSimplePanel(wrapper);
	}
}
