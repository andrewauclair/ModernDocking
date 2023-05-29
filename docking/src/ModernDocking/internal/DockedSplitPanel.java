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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.persist.AppState;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel implements MouseListener, PropertyChangeListener {
	private DockingPanel left = null;
	private DockingPanel right = null;

	private final JSplitPane splitPane = new JSplitPane();
	private DockingPanel parent;
	private final Window window;

	public DockedSplitPanel(Window window) {
		this.window = window;
		setLayout(new BorderLayout());

		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(null);
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

		setDividerLocation(splitPane.getResizeWeight());

		if (splitPane.getUI() instanceof BasicSplitPaneUI) {
			((BasicSplitPaneUI) splitPane.getUI()).getDivider().addMouseListener(this);
		}

		add(splitPane, BorderLayout.CENTER);
	}

	public void setDividerLocation(final double proportion) {
		// calling setDividerLocation on a JSplitPane that isn't visible does nothing, so we need to check if it is showing first
		if (splitPane.isShowing()) {
			if (splitPane.getWidth() > 0 && splitPane.getHeight() > 0) {
				splitPane.setDividerLocation(proportion);
			}
			else {
				// split hasn't been completely calculated yet, wait until componentResize
				splitPane.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						// remove this listener, it's a one off
						splitPane.removeComponentListener(this);
						// call the function again, this time it should actually set the divider location
						setDividerLocation(proportion);
					}
				});
			}
		}
		else {
			// split hasn't been shown yet, wait until it's showing
			splitPane.addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					boolean isShowingChangeEvent = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;

					if (isShowingChangeEvent && splitPane.isShowing()) {
						// remove this listener, it's a one off
						splitPane.removeHierarchyListener(this);
						// call the function again, this time it might set the size or wait for componentResize
						setDividerLocation(proportion);
					}
				}
			});
		}
	}

	public void setDividerLocation(final int location) {
		if (splitPane.isShowing()) {
			if ((splitPane.getWidth() > 0) && (splitPane.getHeight() > 0)) {
				splitPane.setDividerLocation(location);

				AppState.persist();
			}
			else {
				// split hasn't been completely calculated yet, wait until componentResize
				splitPane.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						// remove this listener, it's a one off
						splitPane.removeComponentListener(this);
						// call the function again, this time it should actually set the divider location
						setDividerLocation(location);
					}
				});
			}
		}
		else {
			// split hasn't been shown yet, wait until it's showing
			splitPane.addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					boolean isShowingChangeEvent = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;

					if (isShowingChangeEvent && splitPane.isShowing()) {
						// remove this listener, it's a one off
						splitPane.removeHierarchyListener(this);
						// call the function again, this time it might set the size or wait for componentResize
						setDividerLocation(location);
					}
				}
			});
		}
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

	public DockingPanel getLeft() {
		return left;
	}

	public void setLeft(DockingPanel panel) {
		left = panel;
		left.setParent(this);

		// remember where the divider was and put it back
		int dividerLocation = splitPane.getDividerLocation();

		splitPane.setLeftComponent(panel);

		splitPane.setDividerLocation(dividerLocation);
	}

	public DockingPanel getRight() {
		return right;
	}

	public void setRight(DockingPanel panel) {
		right = panel;
		right.setParent(this);

		// remember where the divider was and put it back
		int dividerLocation = splitPane.getDividerLocation();

		splitPane.setRightComponent(panel);

		splitPane.setDividerLocation(dividerLocation);
	}

	public void setOrientation(int orientation) {
		splitPane.setOrientation(orientation);

		if (splitPane.getUI() instanceof BasicSplitPaneUI) {
			// grab the divider from the UI and remove the border from it
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI())
					.getDivider();

			if (divider != null && divider.getBorder() != null) {
				divider.setBorder(null);
			}
		}
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		DockableWrapper wrapper = DockingInternal.getWrapper(dockable);

		// docking to the center of a split isn't something we allow
		// wouldn't be difficult to support, but isn't a complication we want in this framework
		if (region == DockingRegion.CENTER) {
			region = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? DockingRegion.WEST : DockingRegion.NORTH;
		}

		wrapper.setWindow(window);

		DockedSplitPanel split = new DockedSplitPanel(window);
		parent.replaceChild(this, split);

		DockedSimplePanel newPanel = new DockedSimplePanel(wrapper);

		if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
			split.setLeft(this);
			split.setRight(newPanel);
			dividerProportion = 1.0 - dividerProportion;
		}
		else {
			split.setLeft(newPanel);
			split.setRight(this);
		}

		if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
			split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		}
		else {
			split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}

		split.setDividerLocation(dividerProportion);
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
		// safety against partially configured layout restorations
		if (parent == null) {
			return;
		}

		if (left == child) {
			parent.replaceChild(this, right);
		}
		else if (right == child) {
			parent.replaceChild(this, left);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			setDividerLocation(splitPane.getResizeWeight());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		AppState.persist();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		AppState.persist();
	}
}
