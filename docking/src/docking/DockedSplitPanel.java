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
package docking;

import javax.swing.*;
import java.awt.*;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel {
	private DockingPanel left = null;
	private DockingPanel right = null;

	private final JSplitPane splitPane = new JSplitPane();
	private DockingPanel parent;

	public DockedSplitPanel(DockingPanel parent) {
		this.parent = parent;
		setLayout(new BorderLayout());

		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.5);

		add(splitPane, BorderLayout.CENTER);
	}

	public DockingPanel getLeft() {
		return left;
	}

	public void setLeft(DockingPanel panel) {
		left = panel;
		left.setParent(this);
		splitPane.setLeftComponent(panel);
	}

	public DockingPanel getRight() {
		return right;
	}

	public void setRight(DockingPanel panel) {
		right = panel;
		right.setParent(this);
		splitPane.setRightComponent(panel);
	}

	public void setOrientation(int orientation) {
		splitPane.setOrientation(orientation);
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		// docking to the center of a split isn't something we allow
		// wouldn't be difficult to support, but isn't a complication we want in this framework
		if (region != DockingRegion.CENTER) {
			DockedSplitPanel split = new DockedSplitPanel(parent);
			parent.replaceChild(this, split);

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(new DockedSimplePanel(new DockableWrapper(dockable)));
			}
			else {
				split.setLeft(new DockedSimplePanel(new DockableWrapper(dockable)));
				split.setRight(this);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
				split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
			else {
				split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}
		}
	}

	@Override
	public void undock(Dockable dockable) {
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		if (left == child) {
			setLeft(newChild);
		}
		else if (right == child) {
			setRight(newChild);
		}
	}

	@Override
	public void removeChild(DockingPanel child) {
		if (left == child) {
			parent.replaceChild(this, right);
		}
		else if (right == child) {
			parent.replaceChild(this, left);
		}
	}
}
