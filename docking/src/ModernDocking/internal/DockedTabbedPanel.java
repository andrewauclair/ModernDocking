/*
Copyright (c) 2022-2023 Andrew Auclair

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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.floating.FloatListener;
import ModernDocking.persist.AppState;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DockingPanel that has a JTabbedPane inside its center
 */
public class DockedTabbedPanel extends DockingPanel implements ChangeListener {
	private final List<DockableWrapper> panels = new ArrayList<>();

	private final JTabbedPane tabs = new JTabbedPane();
	private DockingPanel parent;

	private int selectedTab = -1;

	/**
	 * Create a new instance of DockedTabbedPanel
	 */
	public DockedTabbedPanel() {
		setLayout(new BorderLayout());

		// set the initial border. Docking handles the border after this using a global AWT listener
		setNotSelectedBorder();

		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		add(tabs, BorderLayout.CENTER);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		tabs.addChangeListener(this);
	}

	@Override
	public void removeNotify() {
		tabs.removeChangeListener(this);

		super.removeNotify();
	}

	public void addPanel(DockableWrapper dockable) {
		dockable.setParent(this);

		panels.add(dockable);
		tabs.add(dockable.getDockable().getTabText(), dockable.getDisplayPanel());

		tabs.setIconAt(tabs.getTabCount() - 1, dockable.getDockable().getIcon());
		tabs.setSelectedIndex(tabs.getTabCount() - 1);
		selectedTab = tabs.getSelectedIndex();
	}

	/**
	 * Remove a panel from this DockedTabbedPanel. This is done when the dockable is closed or docked elsewhere
	 *
	 * @param dockable The dockable to remove
	 */
	public void removePanel(DockableWrapper dockable) {
		tabs.remove(dockable.getDisplayPanel());
		panels.remove(dockable);

		dockable.setParent(null);
	}

	public List<String> persistentIDs() {
		List<String> ids = new ArrayList<>();

		for (DockableWrapper panel : panels) {
			ids.add(panel.getDockable().getPersistentID());
		}
		return ids;
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		DockableWrapper wrapper = DockingInternal.getWrapper(dockable);
		wrapper.setWindow(panels.get(0).getWindow());

		if (region == DockingRegion.CENTER) {
			addPanel(wrapper);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel(panels.get(0).getWindow());
			parent.replaceChild(this, split);

			DockedSimplePanel newPanel = new DockedSimplePanel(wrapper);

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(newPanel);
				dividerProportion = 1.0 - dividerProportion;
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

			split.setDividerLocation(dividerProportion);
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

		if (tabs.getTabCount() == 1 && parent != null) {
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

	private void setNotSelectedBorder() {
		Color color = UIManager.getColor("Component.borderColor");

		setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 1),
						BorderFactory.createLineBorder(color, 1)
				)
		);
	}

	public void bringToFront(Dockable dockable) {
		for (int i = 0; i < panels.size(); i++) {
			DockableWrapper panel = panels.get(i);

			if (panel.getDockable() == dockable) {
				if (tabs.getSelectedIndex() != i) {
					if (tabs.getSelectedIndex() != -1) {
						DockingListeners.fireHiddenEvent(panels.get(tabs.getSelectedIndex()).getDockable());
					}
					DockingListeners.fireShownEvent(panels.get(i).getDockable());
				}
				tabs.setSelectedIndex(i);
				selectedTab = tabs.getSelectedIndex();
			}
		}
	}

	public String getSelectedTabID() {
		return panels.get(tabs.getSelectedIndex()).getDockable().getPersistentID();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		AppState.persist();

		if (tabs.getSelectedIndex() == -1) {
			return;
		}

		if (selectedTab != -1 && !FloatListener.isFloating) {
			panels.get(selectedTab).getDockable().hidden();
			DockingListeners.fireHiddenEvent(panels.get(selectedTab).getDockable());
		}
		selectedTab = tabs.getSelectedIndex();

		if (selectedTab != -1) {
			panels.get(selectedTab).getDockable().shown();
			DockingListeners.fireShownEvent(panels.get(selectedTab).getDockable());
		}
	}
}
