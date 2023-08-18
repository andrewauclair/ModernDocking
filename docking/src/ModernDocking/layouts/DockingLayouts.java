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
package ModernDocking.layouts;

import ModernDocking.Dockable;
import ModernDocking.DockingProperty;
import ModernDocking.RootDockingPanel;
import ModernDocking.event.LayoutsListener;
import ModernDocking.internal.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DockingLayouts {
	private static final List<LayoutsListener> listeners = new ArrayList<>();
	private static final Map<String, ApplicationLayout> layouts = new HashMap<>();

	/**
	 * Add a new layouts listener
	 *
	 * @param listener New listener to add
	 */
	public static void addLayoutsListener(LayoutsListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a layout listener
	 *
	 * @param listener Listener to remove
	 */
	public static void removeLayoutsListener(LayoutsListener listener) {
		listeners.remove(listener);
	}

	public static void addLayout(String name, ApplicationLayout layout) {
		removeLayout(name);
		layouts.put(name, layout);
		listeners.forEach(l -> l.layoutAdded(name, layout));
	}

	public static void removeLayout(String name) {
		if (layouts.containsKey(name)) {
			layouts.remove(name);
			listeners.forEach(l -> l.layoutRemoved(name));
		}
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

	public static WindowLayout layoutFromRoot(RootDockingPanel root) {
		WindowLayout layout = new WindowLayout(DockingComponentUtils.windowForRoot(root), panelToNode(root.getPanel()));

		layout.setWestUnpinnedToolbarIDs(root.getWestUnpinnedToolbarIDs());
		layout.setEastUnpinnedToolbarIDs(root.getEastUnpinnedToolbarIDs());
		layout.setSouthUnpinnedToolbarIDs(root.getSouthUnpinnedToolbarIDs());

		return layout;
	}

	private static DockingLayoutNode panelToNode(DockingPanel panel) {
		DockingLayoutNode node;

		if (panel instanceof DockedSimplePanel) {
			Dockable dockable = ((DockedSimplePanel) panel).getWrapper().getDockable();

			Map<String, String> properties = DockableProperties.saveProperties(dockable);

			node = new DockingSimplePanelNode(dockable.getPersistentID(), dockable.getClass().getCanonicalName(), properties);
		}
		else if (panel instanceof DockedSplitPanel) {
			node = splitPanelToNode((DockedSplitPanel) panel);
		}
		else if (panel instanceof DockedTabbedPanel) {
			node = tabbedPanelToNode((DockedTabbedPanel) panel);
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

	private static DockingLayoutNode splitPanelToNode(DockedSplitPanel panel) {
		JSplitPane splitPane = panel.getSplitPane();

		int orientation = splitPane.getOrientation();
		int height = splitPane.getHeight();
		int dividerSize = splitPane.getDividerSize();
		int dividerLocation = splitPane.getDividerLocation();
		int width = splitPane.getWidth();
		double dividerProportion = orientation == JSplitPane.VERTICAL_SPLIT ? dividerLocation / (float) (height - dividerSize) :
				dividerLocation / (float) (width - dividerSize);
		return new DockingSplitPanelNode(panelToNode(panel.getLeft()), panelToNode(panel.getRight()), splitPane.getOrientation(), dividerProportion);
	}

	private static DockingLayoutNode tabbedPanelToNode(DockedTabbedPanel panel) {
		DockingTabPanelNode node = new DockingTabPanelNode(panel.getSelectedTabID(), DockableProperties.saveProperties(DockingInternal.getDockable(panel.getSelectedTabID())));

		for (DockableWrapper dockable : panel.getDockables()) {
			node.addTab(dockable.getDockable().getPersistentID(), DockableProperties.saveProperties(dockable.getDockable()));
		}
		return node;
	}
}
