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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * simple docking panel that only has a single Dockable in the center
 */
public class DockedSimplePanel extends DockingPanel {
	/**
	 * Wrapper of the dockable in this simple panel
	 */
	private final DockableWrapper dockable;

	private final DockingAPI docking;

	private DockedAnchorPanel anchor;

	/**
	 * Parent panel of this simple panel
	 */
	private DockingPanel parent;

	/**
	 * Create a new instance of DockedSimplePanel with a wrapper
	 *
	 * @param docking Instance of the docking framework that this panel belongs to
	 * @param dockable Wrapper of the dockable in this simple panel
	 */
	public DockedSimplePanel(DockingAPI docking, DockableWrapper dockable, DockedAnchorPanel anchor) {
		this(docking, dockable, anchor, dockable.getDisplayPanel());
	}

	/**
	 * Create a new instance of DockedSimplePanel with a wrapper
	 *
	 * @param docking Instance of the docking framework that this panel belongs to
	 * @param dockable Wrapper of the dockable in this simple panel
	 * @param displayPanel The panel to display in the DockedSimplePanel for this dockable
	 */
	public DockedSimplePanel(DockingAPI docking, DockableWrapper dockable, DockedAnchorPanel anchor, DisplayPanel displayPanel) {
		setLayout(new BorderLayout());

		setNotSelectedBorder();

		dockable.setParent(this);

		this.dockable = dockable;
		this.docking = docking;
		this.anchor = anchor;

		add(displayPanel, BorderLayout.CENTER);
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
	public DockedAnchorPanel getAnchor() {
		return anchor;
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
			DockedTabbedPanel tabbedPanel = new DockedTabbedPanel(docking, this.dockable, anchor);

			tabbedPanel.addPanel(wrapper);

			DockingListeners.fireHiddenEvent(this.dockable.getDockable());

			parent.replaceChild(this, tabbedPanel);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel(docking, this.dockable.getWindow(), anchor);
			parent.replaceChild(this, split);

			DockingPanel newPanel;

			if (wrapper.isAnchor()) {
				newPanel = new DockedAnchorPanel(docking, wrapper);
			}
			else if (Settings.alwaysDisplayTabsMode()) {
				newPanel = new DockedTabbedPanel(docking, wrapper, anchor);
			}
			else {
				newPanel = new DockedSimplePanel(docking, wrapper, anchor);
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
	public void undock(Dockable dockable) {
		if (this.dockable.getDockable() == dockable) {
			remove(this.dockable.getDisplayPanel());

//			parent.removeChild(this);



			DockedAnchorPanel anchorPanel = anchor;
			anchor = null;

			if (anchorPanel == null || !DockingComponentUtils.isAnchorEmpty(docking, anchorPanel.getWrapper().getDockable())) {
				parent.removeChild(this);
			}
			else {
				parent.replaceChild(this, anchorPanel);
			}

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

	public List<DockingPanel> getChildren() {
		return Collections.emptyList();
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
