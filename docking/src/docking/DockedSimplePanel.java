package docking;

import javax.swing.*;
import java.awt.*;

// simple docking panel that only has a single Dockable in the center
public class DockedSimplePanel extends DockingPanel {
	private final DockableWrapper dockable;

	public DockedSimplePanel(DockableWrapper dockable) {
		setLayout(new BorderLayout());

		this.dockable = dockable;

		add((JComponent) dockable.getDockable(), BorderLayout.CENTER);

		dockable.docked();
	}

	public DockableWrapper getDockable() {
		return dockable;
	}

	@Override
	public boolean undock(Dockable dockable) {
		if (this.dockable.getDockable() == dockable) {
			System.out.println("Undocked panel from DockedSimplePanel");
			remove((JComponent) this.dockable.getDockable());

			this.dockable.undocked();

			revalidate();
			repaint();

			return true;
		}
		return false;
	}
}
