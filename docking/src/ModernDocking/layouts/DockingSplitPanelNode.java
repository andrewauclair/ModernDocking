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

public class DockingSplitPanelNode implements DockingLayoutNode {
	private DockingLayoutNode left;
	private DockingLayoutNode right;
	private int orientation;
	private double dividerProportion;

	private DockingLayoutNode parent;

	public DockingSplitPanelNode(DockingLayoutNode left, DockingLayoutNode right, int orientation, double dividerProportion) {
		this.left = left;
		this.right = right;
		this.orientation = orientation;
		this.dividerProportion = dividerProportion;

		this.left.setParent(this);
		this.right.setParent(this);
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
		DockingLayoutNode left = this.left.findNode(persistentID);

		if (left != null) {
			return left;
		}

		return this.right.findNode(persistentID);
	}

	@Override
	public void dock(String persistentID, DockingRegion region, double dividerProportion) {
		if (region == DockingRegion.CENTER) {
			region = orientation == JSplitPane.HORIZONTAL_SPLIT ? DockingRegion.WEST : DockingRegion.NORTH;
		}

		DockingSimplePanelNode newPanel = new DockingSimplePanelNode(persistentID);

		int orientation = region == DockingRegion.EAST || region == DockingRegion.WEST ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;
		DockingLayoutNode left;
		DockingLayoutNode right;

		if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
			dividerProportion = 1.0 - dividerProportion;
			left = this;
			right = newPanel;
		}
		else {
			left = newPanel;
			right = this;
		}
		DockingLayoutNode oldParent = parent;
		DockingSplitPanelNode split = new DockingSplitPanelNode(left, right, orientation, dividerProportion);

//		oldParent.replaceChild(this, split);
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
		if (left == child) {
			left = newChild;
			left.setParent(this);
		}
		else if (right == child) {
			right = newChild;
			right.setParent(this);
		}
	}

	public DockingLayoutNode getLeft() {
		return left;
	}

	public DockingLayoutNode getRight() {
		return right;
	}

	public int getOrientation() {
		return orientation;
	}

	public double getDividerProportion() {
		return dividerProportion;
	}
}
