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
package layouts;

import docking.DockingRegion;

import javax.swing.*;

public class DockingSimplePanelNode implements DockingLayoutNode {
	private final String persistentID;
	private DockingLayoutNode parent;

	public DockingSimplePanelNode(String persistentID) {
		this.persistentID = persistentID;
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
	public void dock(String persistentID, DockingRegion region) {
		if (region == DockingRegion.CENTER) {
			DockingTabPanelNode tab = new DockingTabPanelNode();

			tab.addTab(this.persistentID);
			tab.addTab(persistentID);

			parent.replaceChild(this, tab);
		}
		else {
			int orientation = region == DockingRegion.NORTH || region == DockingRegion.SOUTH ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
			DockingLayoutNode left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingSimplePanelNode(persistentID);
			DockingLayoutNode right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingSimplePanelNode(persistentID) : this;

			DockingSplitPanelNode split = new DockingSplitPanelNode(left, right, orientation);
			parent.replaceChild(this, split);
		}
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
	}

	public String persistentID() {
		return persistentID;
	}
}
