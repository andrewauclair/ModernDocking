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
import io.github.andrewauclair.moderndocking.internal.*;

import javax.swing.*;
import java.util.*;
import java.util.List;

public class DockingLayouts {
	private static final List<DockingLayoutListener> listeners = new ArrayList<>();
	private static final Map<String, ApplicationLayout> layouts = new HashMap<>();

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

	public static void addLayout(String name, ApplicationLayout layout) {
		removeLayout(name);
		layouts.put(name, layout);
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.ADDED, name, layout)));
	}

	public static void removeLayout(String name) {
		ApplicationLayout layout = layouts.remove(name);

		if (layout != null) {
			listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.REMOVED, name, layout)));
		}
	}

	public static void layoutRestored(ApplicationLayout layout) {
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.RESTORED, "current", layout)));
	}

	public static void layoutPersisted(ApplicationLayout layout) {
		listeners.forEach(l -> l.layoutChange(new DockingLayoutEvent(DockingLayoutEvent.ID.PERSISTED, "current", layout)));
	}

	/**
	 * Lookup a layout by name
	 *
	 * @param name Name of the layout to find
	 * @return The layout, or null if it is not found
	 */
	public static ApplicationLayout getLayout(String name) {
		return layouts.get(name);
	}

	public static List<String> getLayoutNames() {
		return new ArrayList<>(layouts.keySet());
	}

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

	private static DockingLayoutNode panelToNode(DockingAPI docking, DockingPanel panel) {
		DockingLayoutNode node;

		if (panel instanceof DockedSimplePanel) {
			DockableWrapper wrapper = ((DockedSimplePanel) panel).getWrapper();

			Map<String, Property> properties = DockableProperties.saveProperties(wrapper);

			node = new DockingSimplePanelNode(docking, wrapper.getDockable().getPersistentID(), wrapper.getDockable().getClass().getCanonicalName(), properties);
		}
		else if (panel instanceof DockedSplitPanel) {
			node = splitPanelToNode(docking, (DockedSplitPanel) panel);
		}
		else if (panel instanceof DockedTabbedPanel) {
			node = tabbedPanelToNode(docking, (DockedTabbedPanel) panel);
		}
		else if (panel instanceof DockingAnchorPanel) {
			// TODO something real here
			node = new EmptyPanelNode();
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
		return new DockingSplitPanelNode(docking, panelToNode(docking, panel.getLeft()), panelToNode(docking, panel.getRight()), splitPane.getOrientation(), dividerProportion);
	}

	private static DockingLayoutNode tabbedPanelToNode(DockingAPI docking, DockedTabbedPanel panel) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(DockingInternal.get(docking).getDockable(panel.getSelectedTabID()));

		DockingTabPanelNode node = new DockingTabPanelNode(docking, panel.getSelectedTabID(), "", DockableProperties.saveProperties(wrapper));

		for (DockableWrapper dockable : panel.getDockables()) {
			node.addTab(dockable.getDockable().getPersistentID(), "", DockableProperties.saveProperties(dockable));
		}
		return node;
	}
}
