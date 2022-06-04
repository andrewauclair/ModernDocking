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
}
