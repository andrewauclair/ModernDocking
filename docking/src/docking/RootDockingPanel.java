/*
Copyright (c) 2022 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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
		boolean repaint = removeExistingPanel();

		this.panel = panel;
		this.panel.setParent(this);

		add(panel, BorderLayout.CENTER);

		if (repaint) {
			revalidate();
			repaint();
		}
	}

	private boolean removeExistingPanel() {
		if (panel != null) {
			remove(panel);
			panel = null;
			return true;
		}
		return false;
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
//		Docking.registerDockingPanel(this, frame);
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
	public void setParent(DockingPanel parent) {
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		if (panel == null) {
			setPanel(new DockedSimplePanel(new DockableWrapper(dockable)));
		}
		else if (panel instanceof DockedSimplePanel) {
			DockedSimplePanel first = (DockedSimplePanel) panel;

			if (region == DockingRegion.CENTER) {
				DockedTabbedPanel tabbedPanel = new DockedTabbedPanel();

				tabbedPanel.addPanel(first.getDockable());
				tabbedPanel.addPanel(new DockableWrapper(dockable));

				setPanel(tabbedPanel);
			}
			else {
				DockedSplitPanel split = new DockedSplitPanel(this);

				if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
					split.setLeft(first);
					split.setRight(new DockedSimplePanel(new DockableWrapper(dockable)));
				}
				else {
					split.setLeft(new DockedSimplePanel(new DockableWrapper(dockable)));
					split.setRight(first);
				}

				if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
					split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				}
				else {
					split.setOrientation(JSplitPane.VERTICAL_SPLIT);
				}

				setPanel(split);
			}
		}
		else if (panel instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) panel;

			tabbedPanel.addPanel(new DockableWrapper(dockable));
		}

		revalidate();
		repaint();
	}

	@Override
	public void undock(Dockable dockable) {
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		if (panel == child) {
			setPanel(newChild);
		}
	}

	@Override
	public void removeChild(DockingPanel child) {
		if (child == panel) {
			if (removeExistingPanel()) {
				revalidate();
				repaint();
			}
		}
	}
}
