package docking;

import javax.swing.*;
import java.awt.*;

// simple docking panel that only has a single Dockable in the center
public class DockedSimplePanel extends DockingPanel {
	private final DockableWrapper dockable;

	private DockingPanel parent;
	public DockedSimplePanel(DockingPanel parent, DockableWrapper dockable) {
		setLayout(new BorderLayout());

		this.parent = parent;
		this.dockable = dockable;

		add((JComponent) dockable.getDockable(), BorderLayout.CENTER);

		dockable.docked();
	}

	public DockableWrapper getDockable() {
		return dockable;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		// docking to CENTER: Simple -> Tabbed
		// docking else where: Simple -> Split

		if (region == DockingRegion.CENTER) {
			DockedTabbedPanel tabbedPanel = new DockedTabbedPanel(parent);

			tabbedPanel.addPanel(this.dockable);
			tabbedPanel.addPanel(new DockableWrapper(dockable));

			parent.replaceChild(this, tabbedPanel);
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

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		// no-op, simple panel has no children
	}
}
