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
package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.event.DockingLayoutEvent;
import io.github.andrewauclair.moderndocking.event.DockingLayoutListener;
import io.github.andrewauclair.moderndocking.internal.DockableProperties;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockedAnchorPanel;
import io.github.andrewauclair.moderndocking.internal.DockedSimplePanel;
import io.github.andrewauclair.moderndocking.internal.DockedSplitPanel;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.DockingPanel;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JSplitPane;

/**
 * Manage storage, persistence and restoration of application layouts
 */
public class DockingLayouts {
	private static final List<DockingLayoutListener> listeners = new ArrayList<>();
	private static final Map<String, ApplicationLayout> layouts = new HashMap<>();

	/**
	 * Unused. All methods are static
	 */
	private DockingLayouts() {
	}

	/**
	 * Add a new layouts listener
	 *
	 * @param listener New listener to add
	 */
	public static void addLayoutsListener(DockingLayoutListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a layout listener
	 *
	 * @param listener Listener to remove
	 */
	public static void removeLayoutsListener(DockingLayoutListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Store a new layout with the given name
	 *
	 * @param name The name of the layout
	 * @param layout The layout to store
	 */
	public static void addLayout(String name, ApplicationLayout layout) {
		removeLayout(name);
		layouts.put(name, layout);
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.ADDED, name, layout)));
	}

	/**
	 * Remove a layout with the given name
	 *
	 * @param name The name of the layout
	 */
	public static void removeLayout(String name) {
		ApplicationLayout layout = layouts.remove(name);

		if (layout != null) {
			listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.REMOVED, name, layout)));
		}
	}

	/**
	 * Send out docking layout restored events
	 *
	 * @param layout The layout that has been restored
	 */
	public static void layoutRestored(ApplicationLayout layout) {
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.RESTORED, "current", layout)));
	}

	/**
	 * Send out docking layout persisted events
	 *
	 * @param layout The layout that has been restored
	 */
	public static void layoutPersisted(ApplicationLayout layout) {
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.PERSISTED, "current", layout)));
	}

	/**
	 * Lookup a layout by name
	 *
	 * @param name Name of the layout to find
	 *
	 * @return The layout, or null if it is not found
	 */
	public static ApplicationLayout getLayout(String name) {
		return layouts.get(name);
	}

	/**
	 * Get a list of names of all layouts
	 *
	 * @return List of layout names
	 */
	public static List<String> getLayoutNames() {
		return new ArrayList<>(layouts.keySet());
	}

	/**
	 * Create a layout from an existing window
	 *
	 * @param docking Docking instance
	 * @param root The root of the window to get the layout for
	 *
	 * @return Layout of the window
	 */
	public static WindowLayout layoutFromRoot(DockingAPI docking, RootDockingPanelAPI root) {
		InternalRootDockingPanel internalRoot = DockingInternal.get(docking).getRootPanels().entrySet().stream()
				.filter(entry -> entry.getValue().getRootPanel() == root)
				.findFirst()
				.map(Map.Entry::getValue)
				.get();

		WindowLayout layout = new WindowLayout(DockingComponentUtils.windowForRoot(docking, root), panelToNode(docking, internalRoot.getPanel()));

		layout.setWestAutoHideToolbarIDs(internalRoot.getWestAutoHideToolbarIDs());
		layout.setEastAutoHideToolbarIDs(internalRoot.getEastAutoHideToolbarIDs());
		layout.setSouthAutoHideToolbarIDs(internalRoot.getSouthAutoHideToolbarIDs());

		return layout;
	}

	/**
	 * Convert a displayed panel into its layout node representation
	 *
	 * @param docking The docking instance the panel belongs to
	 * @param panel The panel to convert
	 *
	 * @return The resulting layout node
	 */
	private static DockingLayoutNode panelToNode(DockingAPI docking, DockingPanel panel) {
		DockingLayoutNode node;

		if (panel instanceof DockedSimplePanel) {
			DockableWrapper wrapper = ((DockedSimplePanel) panel).getWrapper();

			Map<String, Property> properties = DockableProperties.saveProperties(wrapper);

			node = new DockingSimplePanelNode(docking, wrapper.getDockable().getPersistentID(), wrapper.getDockable().getClass().getCanonicalName(), panel.getAnchor(), properties);
		}
		else if (panel instanceof DockedSplitPanel) {
			node = splitPanelToNode(docking, (DockedSplitPanel) panel);
		}
		else if (panel instanceof DockedTabbedPanel) {
			node = tabbedPanelToNode(docking, (DockedTabbedPanel) panel);
		}
		else if (panel instanceof DockedAnchorPanel) {
			DockableWrapper wrapper = ((DockedAnchorPanel) panel).getWrapper();

			node = new DockingAnchorPanelNode(docking, wrapper.getDockable().getPersistentID(), wrapper.getClass().getCanonicalName());
		}
		else if (panel == null) {
			// the main frame root node contains a null panel if there is nothing docked
			node = new EmptyPanelNode();
		}
		else {
			throw new RuntimeException("Unknown panel");
		}
		return node;
	}

	private static DockingLayoutNode splitPanelToNode(DockingAPI docking, DockedSplitPanel panel) {
		JSplitPane splitPane = panel.getSplitPane();

		int orientation = splitPane.getOrientation();
		int height = splitPane.getHeight();
		int dividerSize = splitPane.getDividerSize();
		int dividerLocation = splitPane.getDividerLocation();
		int width = splitPane.getWidth();
		double dividerProportion = orientation == JSplitPane.VERTICAL_SPLIT ? dividerLocation / (float) (height - dividerSize) :
				dividerLocation / (float) (width - dividerSize);

		return new DockingSplitPanelNode(docking, panelToNode(docking, panel.getLeft()), panelToNode(docking, panel.getRight()), splitPane.getOrientation(), dividerProportion, panel.getAnchor());
	}

	private static DockingLayoutNode tabbedPanelToNode(DockingAPI docking, DockedTabbedPanel panel) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(DockingInternal.get(docking).getDockable(panel.getSelectedTabID()));

		DockingTabPanelNode node = new DockingTabPanelNode(docking, panel.getSelectedTabID(), "", panel.getAnchor(), DockableProperties.saveProperties(wrapper));

		for (DockableWrapper dockable : panel.getDockables()) {
			node.addTab(dockable.getDockable().getPersistentID(), "", DockableProperties.saveProperties(dockable));
		}
		return node;
	}
}
