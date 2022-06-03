package docking;

import javax.swing.*;
import java.awt.*;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel {
	private DockableWrapper left = null;
	private DockableWrapper right = null;

	private final JSplitPane splitPane = new JSplitPane();

	public DockedSplitPanel() {
		setLayout(new BorderLayout());

		splitPane.setContinuousLayout(true);

		add(splitPane, BorderLayout.CENTER);
	}

	public DockableWrapper getLeft() {
		return left;
	}

	public void setLeft(DockableWrapper panel) {
		left = panel;
		splitPane.setLeftComponent((JComponent) panel.getDockable());
		splitPane.setDividerLocation(0.5);
	}

	public DockableWrapper getRight() {
		return right;
	}

	public void setRight(DockableWrapper panel) {
		right = panel;
		splitPane.setRightComponent((JComponent) panel.getDockable());
		splitPane.setDividerLocation(0.5);
	}

	public void setOrientation(int orientation) {
		splitPane.setOrientation(orientation);
	}

	public boolean hasDockables() {
		return left != null && right != null;
	}

	@Override
	public boolean undock(Dockable dockable) {
		if (left != null && dockable == left.getDockable()) {
			remove((Component) left.getDockable());
			left = null;
			return true;
		}
		else if (right != null && dockable == right.getDockable()) {
			remove((Component) left.getDockable());
			right = null;
			return true;
		}
		return false;
	}
}
