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
package ModernDocking;

import ModernDocking.internal.DockableToolbar;
import ModernDocking.internal.DockedSimplePanel;
import ModernDocking.internal.DockingInternal;
import ModernDocking.internal.DockingPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

// only class that should be used by clients
public class RootDockingPanel extends DockingPanel {
	private final JFrame frame;

	DockingPanel panel;

	private JPanel emptyPanel = new JPanel();

	private boolean pinningSupported = false;
	private int pinningLayer = JLayeredPane.MODAL_LAYER;

	// "toolbar" panels for unpinned dockables
	private final DockableToolbar southToolbar;
	private final DockableToolbar westToolbar;
	private final DockableToolbar eastToolbar;

	public RootDockingPanel(JFrame frame) {
		Docking.registerDockingPanel(this, frame);

		setLayout(new GridBagLayout());
		this.frame = frame;

		southToolbar = new DockableToolbar(frame, this, DockableToolbar.Location.SOUTH);
		westToolbar = new DockableToolbar(frame, this, DockableToolbar.Location.WEST);
		eastToolbar = new DockableToolbar(frame, this, DockableToolbar.Location.EAST);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setEmptyPanel(JPanel panel) {
		this.emptyPanel = panel;
	}

	public boolean isPinningSupported() {
		return pinningSupported;
	}

	public void setPinningSupported(boolean supported) {
		pinningSupported = supported;
	}

	public int getPinningLayer() {
		return pinningLayer;
	}

	public void setPinningLayer(int layer) {
		pinningLayer = layer;
	}

	public DockingPanel getPanel() {
		return panel;
	}

	public boolean isEmpty() {
		return panel == null;
	}

	public void setPanel(DockingPanel panel) {
		this.panel = panel;

		if (panel != null) {
			this.panel.setParent(this);

			createContents();
		}
	}

	private boolean removeExistingPanel() {
		remove(emptyPanel);

		if (panel != null) {
			remove(panel);
			panel = null;
			return true;
		}
		return false;
	}

	@Override
	public void removeNotify() {
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		Docking.deregisterDockingPanel(frame);

		super.removeNotify();
	}

	@Override
	public void setParent(DockingPanel parent) {
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		// pass docking to panel if it exists
		// panel does not exist, create new simple panel
		if (panel != null) {
			panel.dock(dockable, region, dividerProportion);
		}
		else {
			setPanel(new DockedSimplePanel(DockingInternal.getWrapper(dockable)));
			DockingInternal.getWrapper(dockable).setFrame(frame);
		}
	}

	@Override
	public void undock(Dockable dockable) {
		if (westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);
		}
		else if (eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);
		}
		else if (southToolbar.hasDockable(dockable)) {
			southToolbar.removeDockable(dockable);
		}

		createContents();
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
				createContents();
			}
		}
	}

	public void setDockablePinned(Dockable dockable) {
		// if the dockable is currently unpinned, remove it from the toolbar, then adjust the toolbars
		if (westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.WEST, 0.25f);
		}
		else if (eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.EAST, 0.25f);
		}
		else if (southToolbar.hasDockable(dockable)) {
			southToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.SOUTH, 0.25f);
		}

		createContents();
	}

	// set a dockable to be unpinned at the given location
	public void setDockableUnpinned(Dockable dockable, DockableToolbar.Location location) {
		switch (location) {
			case WEST: {
				westToolbar.addDockable(dockable);
				break;
			}
			case SOUTH: {
				southToolbar.addDockable(dockable);
				break;
			}
			case EAST: {
				eastToolbar.addDockable(dockable);
				break;
			}
		}

		createContents();
	}

	public List<String> unpinnedPersistentIDs(DockableToolbar.Location location) {
		switch (location) {
			case WEST: return westToolbar.getPersistentIDs();
			case EAST: return eastToolbar.getPersistentIDs();
			case SOUTH: return southToolbar.getPersistentIDs();
		}
		return Collections.emptyList();
	}

	private void createContents() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.VERTICAL;

		if (westToolbar.shouldDisplay()) {
			add(westToolbar, gbc);
			gbc.gridx++;
		}

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		if (panel == null) {
			add(emptyPanel, gbc);
		}
		else {
			add(panel, gbc);
		}
		gbc.gridx++;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.VERTICAL;

		if (eastToolbar.shouldDisplay()) {
			add(eastToolbar, gbc);
		}

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if (southToolbar.shouldDisplay()) {
			add(southToolbar, gbc);
		}

		revalidate();
		repaint();
	}

	public void hideUnpinnedPanels() {
		westToolbar.hideAll();
		southToolbar.hideAll();
		eastToolbar.hideAll();
	}

	public List<String> getWestUnpinnedToolbarIDs() {
		return westToolbar.getPersistentIDs();
	}

	public List<String> getEastUnpinnedToolbarIDs() {
		return eastToolbar.getPersistentIDs();
	}

	public List<String> getSouthUnpinnedToolbarIDs() {
		return southToolbar.getPersistentIDs();
	}
}
