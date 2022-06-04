package docking;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// DockingPanel that has a JTabbedPane inside its center
public class DockedTabbedPanel extends DockingPanel {
	private final List<DockableWrapper> panels = new ArrayList<>();

	private final JTabbedPane tabs = new JTabbedPane();
	private DockingPanel parent;

	public DockedTabbedPanel(DockingPanel parent) {
		this.parent = parent;
		setLayout(new BorderLayout());

		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		add(tabs, BorderLayout.CENTER);
	}

	public void addPanel(DockableWrapper dockable) {
		panels.add(dockable);
		tabs.add(dockable.getDockable().tabText(), (JComponent) dockable.getDockable());

		tabs.setSelectedIndex(tabs.getTabCount() - 1);

		dockable.docked();
	}

	public void removePanel(DockableWrapper dockable) {
		panels.remove(dockable);
		tabs.remove((JComponent) dockable.getDockable());

		dockable.undocked();
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		if (region == DockingRegion.CENTER) {
			addPanel(new DockableWrapper(dockable));
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel();

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(new DockedSimplePanel(split, new DockableWrapper(dockable)));
			}
			else {
				split.setLeft(new DockedSimplePanel(split, new DockableWrapper(dockable)));
				split.setRight(this);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
				split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
			else {
				split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}

			parent.replaceChild(this, split);
			parent = split;
		}
	}

	@Override
	public boolean undock(Dockable dockable) {
		for (DockableWrapper panel : panels) {
			if (panel.getDockable() == dockable) {
				removePanel(panel);
				return true;
			}
		}
		return false;
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {

	}

	public int getPanelCount() {
		return panels.size();
	}

	public DockableWrapper getPanel(int i) {
		return panels.get(i);
	}
}
