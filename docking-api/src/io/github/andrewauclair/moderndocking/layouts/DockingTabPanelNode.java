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

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.settings.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JSplitPane;

/**
 * Docking layout node for a tabbed panel. A that contains multiple dockables in a JTabbedPane.
 */
public class DockingTabPanelNode implements DockingLayoutNode {
	private final List<DockingSimplePanelNode> tabs = new ArrayList<>();
	private final DockingAPI docking;
    private String selectedTabID;
	private String anchor = "";

	private DockingLayoutNode parent;

	/**
	 * Create a new tab panel node with a single dockable to start
	 *
	 * @param docking The docking instance this node belongs to
	 * @param selectedTabID Persistent ID of first dockable
	 * @param selectedTabClassName The name of the class for the selected tab, used to create the dockable if not found
	 * @param anchor The anchor associated with this node
	 */
	public DockingTabPanelNode(DockingAPI docking, String selectedTabID, String selectedTabClassName, String anchor, String titleText, String tabText) {
		this.docking = docking;
        addTab(selectedTabID, selectedTabClassName, anchor, titleText, tabText);
		this.selectedTabID = selectedTabID;
	}

	/**
	 * Create a new tab panel node with a single dockable to start
	 *
	 * @param docking The docking instance this node belongs to
	 * @param selectedTabID Persistent ID of first dockable
	 * @param anchor The anchor associated with this node
	 * @param selectedTabClassName The name of the class for the selected tab, used to create the dockable if not found
	 * @param properties Properties of the dockable
	 */
	public DockingTabPanelNode(DockingAPI docking, String selectedTabID, String selectedTabClassName, String anchor, String titleText, String tabText, Map<String, Property> properties) {
		this.docking = docking;
		addTab(selectedTabID, selectedTabClassName, anchor, titleText, tabText, properties);
		this.selectedTabID = selectedTabID;
	}

	/**
	 * Add a new dockable to the tab panel
	 *
	 * @param persistentID Dockable persistent ID to add
	 * @param className The name of the class for this dockable
	 */
	public void addTab(String persistentID, String className, String anchor, String titleText, String tabText) {
		if (findNode(persistentID) != null) {
			DockingSimplePanelNode node = null;
			for (DockingSimplePanelNode tab : tabs) {
				if (persistentID.equals(tab.getPersistentID())) {
					node = tab;
				}
			}

			// this is the selected tab, bump it up into the correct order
			if (node != null) {
				tabs.remove(node);
				tabs.add(node);
			}
			return;
		}

		if (className.isEmpty()) {
			try {
				className = DockingInternal.get(docking).getDockable(persistentID).getClass().getTypeName();
			}
			catch (Exception ignored) {
			}
		}

		DockingSimplePanelNode tab = new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText);
		tab.setParent(this);
		tabs.add(tab);
	}

	/**
	 * Add a new dockable to the tab panel
	 *
	 * @param persistentID Dockable persistent ID to add
	 * @param className The name of the class for this dockable
	 * @param properties Properties of the dockable
	 */
	public void addTab(String persistentID, String className, String anchor, String titleText, String tabText, Map<String, Property> properties) {
		if (findNode(persistentID) != null) {
			DockingSimplePanelNode node = null;
			for (DockingSimplePanelNode tab : tabs) {
				if (persistentID.equals(tab.getPersistentID())) {
					node = tab;
				}
			}

			// this is the selected tab, bump it up into the correct order
			if (node != null) {
				tabs.remove(node);
				tabs.add(node);
			}
			return;
		}

		if (className.isEmpty()) {
			try {
				className = DockingInternal.get(docking).getDockable(persistentID).getClass().getTypeName();
			}
			catch (Exception ignored) {
			}
		}

		DockingSimplePanelNode tab = new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText, properties);
		tab.setParent(this);
		tabs.add(tab);
	}

	/**
	 * Set the properties that belong to a tab in this node
	 *
	 * @param persistentID The persistent ID of the dockable tab we're setting properties for
	 * @param properties The properties of the dockable
	 */
	public void setProperties(String persistentID, Map<String, Property> properties) {
		Optional<DockingSimplePanelNode> first = tabs.stream()
				.filter(dockingSimplePanelNode -> dockingSimplePanelNode.getPersistentID().equals(persistentID))
				.findFirst();

		first.ifPresent(dockingSimplePanelNode -> dockingSimplePanelNode.setProperties(properties));
	}

	/**
	 * Get a list of the persistent IDs in the tab panel
	 *
	 * @return List of persistent IDs
	 */
	public List<DockingSimplePanelNode> getPersistentIDs() {
		return new ArrayList<>(tabs);
	}

	/**
	 * Get the persistent ID of the selected tab
	 *
	 * @return persistent ID
	 */
	public String getSelectedTabID() {
		return selectedTabID;
	}

	@Override
	public DockingLayoutNode getParent() {
		return parent;
	}

	@Override
	public void setParent(DockingLayoutNode parent) {
		this.parent = parent;
	}

	@Override
	public DockingLayoutNode findNode(String persistentID) {
		for (DockingSimplePanelNode tab : tabs) {
			if (persistentID.equals(tab.getPersistentID())) {
				return tab;
			}
		}
		return null;
	}

	@Override
	public void dock(String persistentID, DockingRegion region, double dividerProportion) {
		if (region == DockingRegion.CENTER) {
			Dockable dockable = DockingInternal.get(docking).getDockable(persistentID);
			String className = dockable.getClass().getTypeName();

			addTab(persistentID, className, anchor, dockable.getTitleText(), dockable.getTabText());
		}
		else {
			int orientation = region == DockingRegion.EAST || region == DockingRegion.WEST ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

			DockingLayoutNode left;
			DockingLayoutNode right;

			Dockable dockable = DockingInternal.get(docking).getDockable(persistentID);
			String className = dockable.getClass().getTypeName();

			if (Settings.alwaysDisplayTabsMode()) {
				left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingTabPanelNode(docking, persistentID, className, anchor, dockable.getTitleText(), dockable.getTabText()) : this;
				right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingTabPanelNode(docking, persistentID, className, anchor, dockable.getTitleText(), dockable.getTabText());
			}
			else {
				left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingSimplePanelNode(docking, persistentID, className, anchor, dockable.getTitleText(), dockable.getTabText()) : this;
				right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingSimplePanelNode(docking, persistentID, className, anchor, dockable.getTitleText(), dockable.getTabText());
			}

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				dividerProportion = 1.0 - dividerProportion;
			}

			DockingLayoutNode oldParent = parent;
			DockingSplitPanelNode split = new DockingSplitPanelNode(docking, left, right, orientation, dividerProportion, anchor);
			oldParent.replaceChild(this, split);
		}
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
	}

	/**
	 * Bring the layout node to front by setting it as the selected tab
	 *
	 * @param node Tab to set as selected
	 */
	public void bringToFront(DockingLayoutNode node) {
		for (DockingSimplePanelNode tab : tabs) {
			if (tab == node) {
				selectedTabID = tab.getPersistentID();
				break;
			}
		}
	}

	public String getAnchor() {
		return anchor;
	}
}
