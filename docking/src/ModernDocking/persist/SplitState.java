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
package ModernDocking.persist;

import ModernDocking.Dockable;
import ModernDocking.internal.DockedSimplePanel;
import ModernDocking.internal.DockedSplitPanel;
import ModernDocking.internal.DockedTabbedPanel;

import javax.swing.*;

/**
 * State of a split pane
 */
public class SplitState implements DockableState {
	private final DockableState left;
	private final DockableState right;

	private final int orientation;
	private final double dividerProportion;
	private final double resizeWeight;

	/**
	 * Create a SplitState from the split panel
	 *
	 * @param panel Split panel to store state for
	 */
	public SplitState(DockedSplitPanel panel) {
		JSplitPane splitPane = panel.getSplitPane();

		orientation = splitPane.getOrientation();
		resizeWeight = panel.getSplitPane().getResizeWeight();

		int height = splitPane.getHeight();
		int dividerSize = splitPane.getDividerSize();
		int dividerLocation = splitPane.getDividerLocation();
		int width = splitPane.getWidth();
		dividerProportion = orientation == JSplitPane.VERTICAL_SPLIT ? dividerLocation / (float) (height - dividerSize) :
				dividerLocation / (float) (width - dividerSize);

		if (panel.getLeft() instanceof DockedTabbedPanel) {
			left = new TabState((DockedTabbedPanel) panel.getLeft());
		}
		else if (panel.getLeft() instanceof DockedSimplePanel) {
			Dockable dockable = ((DockedSimplePanel) panel.getLeft()).getWrapper().getDockable();
			left = new PanelState(dockable.getPersistentID(), dockable.getClass().getCanonicalName());
		}
		else if (panel.getLeft() instanceof DockedSplitPanel) {
			left = new SplitState((DockedSplitPanel) panel.getLeft());
		}
		else {
			throw new RuntimeException("Unknown panel");
		}

		if (panel.getRight() instanceof DockedTabbedPanel) {
			right = new TabState((DockedTabbedPanel) panel.getRight());
		}
		else if (panel.getRight() instanceof DockedSimplePanel) {
			Dockable dockable = ((DockedSimplePanel) panel.getRight()).getWrapper().getDockable();
			right = new PanelState(dockable.getPersistentID(), dockable.getClass().getCanonicalName());
		}
		else if (panel.getRight() instanceof DockedSplitPanel) {
			right = new SplitState((DockedSplitPanel) panel.getRight());
		}
		else {
			throw new RuntimeException("Unknown panel");
		}
	}

	/**
	 * Get the state of the left/top component
	 *
	 * @return The left/top component state
	 */
	public DockableState getLeft() {
		return left;
	}

	/**
	 * Get the state of the right/bottom component
	 *
	 * @return Right/bottom component state
	 */
	public DockableState getRight() {
		return right;
	}

	/**
	 * Get the stored orientation of the split pane
	 *
	 * @return Orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Get the stored divider location of the split pane
	 *
	 * @return Divider location
	 */
	public double getDividerProprtion() {
		return dividerProportion;
	}

	/**
	 * Get the resize weight of the split pane
	 *
	 * @return Resize weight
	 */
	public double getResizeWeight() {
		return resizeWeight;
	}
}
