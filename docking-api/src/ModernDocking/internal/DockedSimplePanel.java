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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.DockableTabGroup;
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.floating.FloatListener;
import ModernDocking.settings.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * simple docking panel that only has a single Dockable in the center
 */
public class DockedSimplePanel extends DockingPanel {
	/**
	 * Wrapper of the dockable in this simple panel
	 */
	private final DockableWrapper dockable;

	private final DockingAPI docking;

	/**
	 * Parent panel of this simple panel
	 */
	private DockingPanel parent;

	private FloatListener floatListener;

	/**
	 * Create a new instance of DockedSimplePanel with a wrapper
	 *
	 * @param docking Instance of the docking framework that this panel belongs to
	 * @param dockable Wrapper of the dockable in this simple panel
	 */
	public DockedSimplePanel(DockingAPI docking, DockableWrapper dockable) {
		this(docking, dockable, dockable.getDisplayPanel());
	}

	/**
	 * Create a new instance of DockedSimplePanel with a wrapper
	 *
	 * @param docking Instance of the docking framework that this panel belongs to
	 * @param dockable Wrapper of the dockable in this simple panel
	 * @param displayPanel The panel to display in the DockedSimplePanel for this dockable
	 */
	public DockedSimplePanel(DockingAPI docking, DockableWrapper dockable, DisplayPanel displayPanel) {
		setLayout(new BorderLayout());

		setNotSelectedBorder();

		dockable.setParent(this);

		this.dockable = dockable;
		this.docking = docking;

		add(displayPanel, BorderLayout.CENTER);

		floatListener = new FloatListener(docking, dockable.getDisplayPanel());
	}

	/**
	 * Get the wrapper of the dockable contained in this simple panel
	 *
	 * @return Contained dockable
	 */
	public DockableWrapper getWrapper() {
		return dockable;
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		// docking to CENTER: Simple -> Tabbed
		// docking else where: Simple -> Split
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
		wrapper.setWindow(this.dockable.getWindow());

		if (getParent() instanceof DockedTabbedPanel) {
			((DockedTabbedPanel) parent).addPanel(wrapper);
		}
		else if (region == DockingRegion.CENTER) {
			DockedTabbedPanel tabbedPanel = new DockedTabbedPanel(docking, this.dockable);

			tabbedPanel.addPanel(wrapper);

			DockingListeners.fireHiddenEvent(this.dockable.getDockable());

			parent.replaceChild(this, tabbedPanel);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel(docking, this.dockable.getWindow());
			parent.replaceChild(this, split);

			DockingPanel newPanel;

			if (Settings.alwaysDisplayTabsMode()) {
				newPanel = new DockedTabbedPanel(docking, wrapper);
			}
			else {
				newPanel = new DockedSimplePanel(docking, wrapper);
			}

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
	public void dock(DockableTabGroup group, Dockable firstDockable, DockingRegion region, double dividerProportion) {
		if (region == DockingRegion.CENTER) {
			return;
		}

		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(firstDockable);

		DockedSplitPanel split = new DockedSplitPanel(docking, this.dockable.getWindow());
		parent.replaceChild(this, split);

		DockingPanel newPanel = new DockedTabbedPanel(docking, group, wrapper);

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

		repaint();
		revalidate();
	}

	@Override
	public void undock(Dockable dockable) {
		if (this.dockable.getDockable() == dockable) {
			remove(this.dockable.getDisplayPanel());

			parent.removeChild(this);

			this.dockable.setParent(null);

			revalidate();
			repaint();
		}
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		// no-op, simple panel has no children
	}

	@Override
	public void removeChild(DockingPanel child) {
		// no-op, simple panel has no children
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
}
