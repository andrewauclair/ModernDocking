/*
Copyright (c) 2022-2023 Andrew Auclair

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JSplitPane;

/**
 * Docking layout node for a simple panel. A that contains a single dockable.
 */
public class DockingSimplePanelNode implements DockingLayoutNode {
	private final DockingAPI docking;
	private final String persistentID;
	private final String className;
    private String anchor = "";
    private String titleText;
    private String tabText;

    private Map<String, Property> properties = new HashMap<>();
	private DockingLayoutNode parent;

	/**
	 * Create a new DockingSimplePanelNode with just a persistent ID
	 *
	 * @param docking The docking instance for this node
	 * @param persistentID The persistent ID of the contained dockable
	 * @param className The name of the dockable class to instantiate, if dockable is not found
	 * @param anchor The anchor associated with this node
	 */
	public DockingSimplePanelNode(DockingAPI docking, String persistentID, String className, String anchor, String titleText, String tabText) {
		this.docking = docking;
		this.persistentID = persistentID;
		this.className = className;
        this.anchor = anchor;
        this.titleText = titleText;
        this.tabText = tabText;
    }

	/**
	 * Create a new DockingSimplePanelNode with properties
	 *
	 * @param docking The docking instance for this node
	 * @param persistentID The persistent ID of the contained dockable
	 * @param className The name of the dockable class to instantiate, if dockable is not found
	 * @param anchor The anchor associated with this node
	 * @param properties Properties of the dockable
	 */
	public DockingSimplePanelNode(DockingAPI docking, String persistentID, String className, String anchor, String titleText, String tabText, Map<String, Property> properties) {
		this.docking = docking;
		this.persistentID = persistentID;
		this.className = className;
        this.anchor = anchor;
		this.titleText = titleText;
		this.tabText = tabText;
        this.properties.putAll(properties);
	}

	@Override
	public DockingLayoutNode getParent() {
		return parent;
	}

	public void setParent(DockingLayoutNode parent) {
		this.parent = parent;
	}

	@Override
	public DockingLayoutNode findNode(String persistentID) {
		if (this.persistentID.equals(persistentID)) {
			return this;
		}
		return null;
	}

	@Override
	public void dock(String persistentID, DockingRegion region, double dividerProportion) {
		if (getParent() instanceof DockingTabPanelNode) {
			getParent().dock(persistentID, region, dividerProportion);
		}
		else if (region == DockingRegion.CENTER) {
			DockingTabPanelNode tab = new DockingTabPanelNode(docking, persistentID, "", anchor, titleText, tabText);

			Dockable dockable = DockingInternal.get(docking).getDockable(persistentID);

			tab.addTab(this.persistentID, "", anchor, titleText, tabText);
			tab.addTab(persistentID, "", anchor, dockable.getTitleText(), dockable.getTabText());

			parent.replaceChild(this, tab);
		}
		else {
			int orientation = region == DockingRegion.EAST || region == DockingRegion.WEST ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

			DockingLayoutNode left;
			DockingLayoutNode right;

			if (Settings.alwaysDisplayTabsMode()) {
				if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
					left = region == DockingRegion.EAST ? this : new DockingTabPanelNode(docking, persistentID, "", anchor, titleText, tabText);
					right = region == DockingRegion.EAST ? new DockingTabPanelNode(docking, persistentID, "", anchor, titleText, tabText) : this;
				}
				else {
					left = region == DockingRegion.SOUTH ? this : new DockingTabPanelNode(docking, persistentID, "", anchor, titleText, tabText);
					right = region == DockingRegion.SOUTH ? new DockingTabPanelNode(docking, persistentID, "", anchor, titleText, tabText) : this;
				}
			}
			else {
				if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
					left = region == DockingRegion.EAST ? this : new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText);
					right = region == DockingRegion.EAST ? new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText) : this;
				}
				else {
					left = region == DockingRegion.SOUTH ? this : new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText);
					right = region == DockingRegion.SOUTH ? new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText) : this;
				}
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
	 * Get Persistent ID of the contained dockable
	 *
	 * @return Persistent ID
	 */
	public String getPersistentID() {
		return persistentID;
	}

	/**
	 * The name of the class of the application dockable panel
	 *
	 * @return Class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Get the properties of the dockable
	 *
	 * @return properties map
	 */
	public Map<String, Property> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Set the properties of the dockable
	 *
	 * @param properties New properties for the dockable
	 */
	public void setProperties(Map<String, Property> properties) {
		this.properties = new HashMap<>(properties);
	}

	/**
	 * The persistent ID of the anchor this node is associated with
	 *
	 * @return Anchor this node is associated with or null
	 */
	public String getAnchor() {
		return anchor;
	}

	public String getTitleText() {
		return titleText;
	}

	public String getTabText() {
		return tabText;
	}
}
