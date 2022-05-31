package docking;

import javax.swing.*;
import java.awt.*;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel {
	private DockableWrapper left;
	private DockableWrapper right;

	private final JSplitPane splitPane = new JSplitPane();

	public DockedSplitPanel() {
		setLayout(new BorderLayout());

		splitPane.setContinuousLayout(true);

		add(splitPane, BorderLayout.CENTER);
	}

	public void setLeft(DockableWrapper panel) {
		left = panel;
		splitPane.setLeftComponent((JComponent) panel.getDockable());
		splitPane.setDividerLocation(0.5);
	}

	public void setRight(DockableWrapper panel) {
		right = panel;
		splitPane.setRightComponent((JComponent) panel.getDockable());
		splitPane.setDividerLocation(0.5);
	}

	public void setOrientation(int orientation) {
		splitPane.setOrientation(orientation);
	}

	@Override
	public boolean undock(Dockable dockable) {
		return false;
	}
}
