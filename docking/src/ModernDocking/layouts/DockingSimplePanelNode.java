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

import ModernDocking.DockingRegion;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class DockingSimplePanelNode implements DockingLayoutNode {
	private final String persistentID;
	private final Map<String, String> properties = new HashMap<>();
	private DockingLayoutNode parent;

	public DockingSimplePanelNode(String persistentID) {
		this.persistentID = persistentID;
	}

	public DockingSimplePanelNode(String persistentID, Map<String, String> properties) {
		this.persistentID = persistentID;
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

		 if (region == DockingRegion.CENTER) {
			DockingTabPanelNode tab = new DockingTabPanelNode(persistentID);

			tab.addTab(this.persistentID);
			tab.addTab(persistentID);

			parent.replaceChild(this, tab);
		}
		 else if (getParent() instanceof DockingTabPanelNode) {
			 getParent().dock(persistentID, region, dividerProportion);
		 }
//		 else if (getParent() instanceof DockingSplitPanelNode) {
//			 getParent().dock(persistentID, region, dividerProportion);
//		 }
		else {
			int orientation = region == DockingRegion.EAST || region == DockingRegion.WEST ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

			DockingLayoutNode left;
			DockingLayoutNode right;

			if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
				left = region == DockingRegion.EAST ? this : new DockingSimplePanelNode(persistentID);
				right = region == DockingRegion.EAST ? new DockingSimplePanelNode(persistentID) : this;
			}
			else {
				left = region == DockingRegion.SOUTH ? this : new DockingSimplePanelNode(persistentID);
				right = region == DockingRegion.SOUTH ? new DockingSimplePanelNode(persistentID) : this;
			}

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				dividerProportion = 1.0 - dividerProportion;
			}

			DockingLayoutNode oldParent = parent;
			DockingSplitPanelNode split = new DockingSplitPanelNode(left, right, orientation, dividerProportion);
			oldParent.replaceChild(this, split);
		}
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
	}

	public String getPersistentID() {
		return persistentID;
	}

	public Map<String, String> getProperties() {
		return properties;
	}
}
