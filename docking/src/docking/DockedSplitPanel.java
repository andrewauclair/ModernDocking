package docking;

// DockingPanel that has a split pane with 2 dockables, split can be vertical or horizontal
public class DockedSplitPanel extends DockingPanel {
	private DockingPanel left;
	private DockingPanel right;

	public void setLeft(DockingPanel panel) {
		left = panel;
	}

	public void setRight(DockingPanel panel) {
		right = panel;
	}

	@Override
	public boolean undock(Dockable dockable) {
		return false;
	}
}
