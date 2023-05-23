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
import java.util.ArrayList;
import java.util.List;

public class DockingTabPanelNode implements DockingLayoutNode {
	private final List<DockingSimplePanelNode> tabs = new ArrayList<>();
	private final String selectedTabID;

	private DockingLayoutNode parent;

	public DockingTabPanelNode(String selectedTabID) {
		this.selectedTabID = selectedTabID;
	}

	public void addTab(String persistentID) {
		DockingSimplePanelNode tab = new DockingSimplePanelNode(persistentID);
		tab.setParent(this);
		tabs.add(tab);
	}

	public List<String> getPersistentIDs() {
		List<String> persistentIDs = new ArrayList<>();

		for (DockingSimplePanelNode tab : tabs) {
			persistentIDs.add(tab.getPersistentID());
		}
		return persistentIDs;
	}

	public String getSelectedTabID() {
		return selectedTabID;
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
			addTab(persistentID);
		}
		else {
			int orientation = region == DockingRegion.NORTH || region == DockingRegion.SOUTH ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
			DockingLayoutNode left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingSimplePanelNode(persistentID);
			DockingLayoutNode right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingSimplePanelNode(persistentID) : this;

			DockingSplitPanelNode split = new DockingSplitPanelNode(left, right, orientation, 0.5);
			parent.replaceChild(this, split);
		}
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
	}
}
