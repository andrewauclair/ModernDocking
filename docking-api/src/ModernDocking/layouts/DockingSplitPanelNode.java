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
package ModernDocking.layouts;

 import ModernDocking.DockingRegion;
 import ModernDocking.api.DockingAPI;
 import ModernDocking.internal.DockingInternal;
 import ModernDocking.settings.Settings;

 import javax.swing.*;

 /**
  * Layout node that represents a splitpane
  */
 public class DockingSplitPanelNode implements DockingLayoutNode {
	 private final DockingAPI docking;
	 private DockingLayoutNode left;
	private DockingLayoutNode right;
	private final int orientation;
	private final double dividerProportion;

	private DockingLayoutNode parent;

	 /**
	  * Create a new DockingSplitPanelNode for a layout
	  *
	  * @param left The left component of the split
	  * @param right The right component of the split
	  * @param orientation The orientation of the split
	  * @param dividerProportion The divider proportion of the split
	  */
	public DockingSplitPanelNode(DockingAPI docking, DockingLayoutNode left, DockingLayoutNode right, int orientation, double dividerProportion) {
		this.docking = docking;
		this.left = left;
		this.right = right;
		this.orientation = orientation;
		this.dividerProportion = dividerProportion;

		if (this.left != null) {
			this.left.setParent(this);
		}
		if (this.right != null) {
			this.right.setParent(this);
		}
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
		if (this.left != null) {
			DockingLayoutNode left = this.left.findNode(persistentID);

			if (left != null) {
				return left;
			}
		}

		if (this.right == null) {
			return null;
		}
		return this.right.findNode(persistentID);
	}

	@Override
	public void dock(String persistentID, DockingRegion region, double dividerProportion) {
		if (region != DockingRegion.CENTER) {
			int orientation = region == DockingRegion.EAST || region == DockingRegion.WEST ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

			DockingLayoutNode left;
			DockingLayoutNode right;

			if (Settings.alwaysDisplayTabsMode(DockingInternal.get(docking).getDockable(persistentID))) {
				left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingTabPanelNode(docking, persistentID) : this;
				right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingTabPanelNode(docking, persistentID);
			}
			else {
				String className = DockingInternal.get(docking).getDockable(persistentID).getClass().getCanonicalName();

				left = region == DockingRegion.NORTH || region == DockingRegion.WEST ? new DockingSimplePanelNode(docking, persistentID, className) : this;
				right = region == DockingRegion.NORTH || region == DockingRegion.WEST ? this : new DockingSimplePanelNode(docking, persistentID, className);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				dividerProportion = 1.0 - dividerProportion;
			}

			DockingLayoutNode oldParent = parent;
			DockingSplitPanelNode split = new DockingSplitPanelNode(docking, left, right, orientation, dividerProportion);
			oldParent.replaceChild(this, split);
		}
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

	 /**
	  * Get the left component of the split
	  *
	  * @return Left component
	  */
	public DockingLayoutNode getLeft() {
		return left;
	}

	 /**
	  * Get the right component of the split
	  *
	  * @return Right component
	  */
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
