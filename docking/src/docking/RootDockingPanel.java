package docking;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

// only class that should be used by clients
public class RootDockingPanel extends DockingPanel implements AncestorListener, HierarchyListener {
	DockingPanel panel;

	public RootDockingPanel() {
		setLayout(new BorderLayout());
	}

	public DockingPanel getPanel() {
		return panel;
	}

	public void setPanel(DockingPanel panel) {
		boolean repaint = false;
		if (this.panel != null) {
			remove(this.panel);
			repaint = true;
		}
		this.panel = panel;
		add(panel, BorderLayout.CENTER);

		if (repaint) {
			revalidate();
			repaint();
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();

		addAncestorListener(this);
		addHierarchyListener(this);
	}

	@Override
	public void removeNotify() {
		removeAncestorListener(this);

		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		Docking.deregisterDockingPanel(frame);

		super.removeNotify();
	}


	@Override
	public void ancestorAdded(AncestorEvent event) {
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		Docking.registerDockingPanel(this, frame);
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if ( (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
			if (getParent() == e.getChangedParent()) {
				System.out.println("*** Added to parent " + e.getChangedParent());

				if (getParent() instanceof JFrame) {
					Docking.registerDockingPanel(this, (JFrame) getParent());
				}
				else {
					// TODO throw an exception, currently we only support JFrame
				}
			}
		}
	}

	@Override
	public boolean undock(Dockable dockable) {
		return panel.undock(dockable);
	}
}
