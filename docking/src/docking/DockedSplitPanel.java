package docking;

import javax.swing.*;
import java.awt.*;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel {
	private DockingPanel left = null;
	private DockingPanel right = null;

	private final JSplitPane splitPane = new JSplitPane();

	public DockedSplitPanel() {
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
		splitPane.setLeftComponent(panel);
	}

	public DockingPanel getRight() {
		return right;
	}

	public void setRight(DockingPanel panel) {
		right = panel;
		splitPane.setRightComponent(panel);
	}

	public void setOrientation(int orientation) {
		splitPane.setOrientation(orientation);
	}

	public boolean hasDockables() {
		return left != null && right != null;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		throw new RuntimeException("Implement docking to split panel if needed");
//		if (root.getPanel() == null) {
//			root.setPanel(new DockedSimplePanel(new DockableWrapper(dockable)));
//		}
//		else if (root.getPanel() instanceof DockedSimplePanel) {
//			DockedSimplePanel first = (DockedSimplePanel) root.getPanel();
//
//			if (region == DockingRegion.CENTER) {
//				DockedTabbedPanel tabbedPanel = new DockedTabbedPanel();
//
//				tabbedPanel.addPanel(first.getDockable());
//				tabbedPanel.addPanel(new DockableWrapper(dockable));
//
//				root.setPanel(tabbedPanel);
//			}
//			else {
//				DockedSplitPanel split = new DockedSplitPanel();
//
//				if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
//					split.setLeft(first);
//					split.setRight(new DockedSimplePanel(new DockableWrapper(dockable)));
//				}
//				else {
//					split.setLeft(new DockedSimplePanel(new DockableWrapper(dockable)));
//					split.setRight(first);
//				}
//
//				if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
//					split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
//				}
//				else {
//					split.setOrientation(JSplitPane.VERTICAL_SPLIT);
//				}
//
//				root.setPanel(split);
//			}
//		}
//		else if (root.getPanel() instanceof DockedTabbedPanel) {
//			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();
//
//			tabbedPanel.addPanel(new DockableWrapper(dockable));
//		}
	}

	@Override
	public boolean undock(Dockable dockable) {
		if (left != null && left.undock(dockable)) {
			remove(left);
			left = null;
			return true;
		}
		else if (right != null && right.undock(dockable)) {
			remove(right);
			right = null;
			return true;
		}
		return false;
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
}
