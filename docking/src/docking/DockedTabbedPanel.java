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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// DockingPanel that has a JTabbedPane inside its center
public class DockedTabbedPanel extends DockingPanel {
	private final List<DockableWrapper> panels = new ArrayList<>();

	private final JTabbedPane tabs = new JTabbedPane();
	private DockingPanel parent;

	public DockedTabbedPanel() {
		setLayout(new BorderLayout());

		setBorder(null);

		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		add(tabs, BorderLayout.CENTER);
	}

	public void addPanel(DockableWrapper dockable) {
		dockable.setParent(this);

		panels.add(dockable);
		tabs.add(dockable.getDockable().tabText(), (JComponent) dockable.getDockable());

		tabs.setSelectedIndex(tabs.getTabCount() - 1);
	}

	public void removePanel(DockableWrapper dockable) {
		panels.remove(dockable);
		tabs.remove((JComponent) dockable.getDockable());
	}

	public List<String> persistentIDs() {
		List<String> ids = new ArrayList<>();

		for (DockableWrapper panel : panels) {
			ids.add(panel.getDockable().persistentID());
		}
		return ids;
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		DockableWrapper wrapper = Docking.getWrapper(dockable);

		if (region == DockingRegion.CENTER) {
			addPanel(wrapper);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel();
			parent.replaceChild(this, split);

			DockedSimplePanel newPanel = new DockedSimplePanel(wrapper);

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(newPanel);
			}
			else {
				split.setLeft(newPanel);
				split.setRight(this);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
				split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
			else {
				split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}
		}

		revalidate();
		repaint();
	}

	@Override
	public void undock(Dockable dockable) {
		DockableWrapper toRemove = null;

		for (DockableWrapper panel : panels) {
			if (panel.getDockable() == dockable) {
				toRemove = panel;
			}
		}

		if (toRemove != null) {
			removePanel(toRemove);
		}

		if (tabs.getTabCount() == 1) {
			parent.replaceChild(this, new DockedSimplePanel(panels.get(0)));
		}
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		// no-op, docked tab can't have panel children, wrappers only
	}

	@Override
	public void removeChild(DockingPanel child) {
		// no-op, docked tab can't have panel children, wrappers only
	}
}
