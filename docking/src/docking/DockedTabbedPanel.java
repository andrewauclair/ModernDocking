package docking;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// DockingPanel that has a JTabbedPane inside its center
public class DockedTabbedPanel extends DockingPanel {
	private final List<DockableWrapper> panels = new ArrayList<>();

	private final JTabbedPane tabs = new JTabbedPane();

	public DockedTabbedPanel() {
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
	public boolean undock(Dockable dockable) {
		for (DockableWrapper panel : panels) {
			if (panel.getDockable() == dockable) {
				removePanel(panel);
				return true;
			}
		}
		return false;
	}

	public int getPanelCount() {
		return panels.size();
	}

	public DockableWrapper getPanel(int i) {
		return panels.get(i);
	}
}
