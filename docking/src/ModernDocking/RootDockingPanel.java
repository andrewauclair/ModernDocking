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

import ModernDocking.internal.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Panel that should be added to each frame that should support docking
 */
public class RootDockingPanel extends DockingPanel {
	private Window window;

	private DockingPanel panel;

	private JPanel emptyPanel = new JPanel();

	private boolean pinningSupported = false;
	private int pinningLayer = JLayeredPane.MODAL_LAYER;

	/**
	 * South toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar southToolbar;
	/**
	 * West toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar westToolbar;
	/**
	 * East toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar eastToolbar;

	private EnumSet<DockableToolbar.Location> supportedToolbars;

	/**
	 * Create root panel with GridBagLayout as the layout
	 */
	public RootDockingPanel() {
		setLayout(new GridBagLayout());
	}

	/**
	 * Create a new RootDockingPanel for the given window
	 *
	 * @param window Window this root panel is attached to
	 */
	public RootDockingPanel(Window window) {
		setLayout(new GridBagLayout());

		this.window = window;

		if (window instanceof JFrame) {
			Docking.registerDockingPanel(this, (JFrame) window);
		}
		else {
			Docking.registerDockingPanel(this, (JDialog) window);
		}

		southToolbar = new DockableToolbar(window, this, DockableToolbar.Location.SOUTH);
		westToolbar = new DockableToolbar(window, this, DockableToolbar.Location.WEST);
		eastToolbar = new DockableToolbar(window, this, DockableToolbar.Location.EAST);

		supportedToolbars = EnumSet.allOf(DockableToolbar.Location.class);
		pinningSupported = !supportedToolbars.isEmpty();
	}

	/**
	 * Create a new RootDockingPanel for the given window and set of supported toolbars
	 *
	 * @param window Window this root panel is attached to
	 * @param supportedToolbars Supported toolbars
	 */
	public RootDockingPanel(Window window, EnumSet<DockableToolbar.Location> supportedToolbars) {
		this(window);

		this.supportedToolbars = supportedToolbars;
		pinningSupported = !supportedToolbars.isEmpty();
	}

	/**
	 * Set the parent window of this root
	 *
	 * @param window Parent window of root
	 */
	public void setWindow(Window window) {
		if (this.window != null) {
			Docking.deregisterDockingPanel(this.window);
		}
		this.window = window;

		if (window instanceof JFrame) {
			Docking.registerDockingPanel(this, (JFrame) window);
		}
		else {
			Docking.registerDockingPanel(this, (JDialog) window);
		}

		southToolbar = new DockableToolbar(window, this, DockableToolbar.Location.SOUTH);
		westToolbar = new DockableToolbar(window, this, DockableToolbar.Location.WEST);
		eastToolbar = new DockableToolbar(window, this, DockableToolbar.Location.EAST);

		supportedToolbars = EnumSet.allOf(DockableToolbar.Location.class);
	}

	/**
	 * Get the window that contains this RootDockingPanel
	 *
	 * @return Parent window
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Set the panel that should be displayed when the root is empty
	 *
	 * @param panel New empty panel
	 */
	public void setEmptyPanel(JPanel panel) {
		this.emptyPanel = panel;
	}

	/**
	 * Check if pinning is supported on this root
	 *
	 * @return True if pinning is supported
	 */
	public boolean isPinningSupported() {
		return pinningSupported;
	}

	/**
	 * Set pinning supported flag
	 *
	 * @param supported Is pinning supported?
	 */
	public void setPinningSupported(boolean supported) {
		pinningSupported = supported;
	}

	/**
	 * Get the layer that is being used for pinning
	 *
	 * @return Pinning layer
	 */
	public int getPinningLayer() {
		return pinningLayer;
	}

	/**
	 * Set the pinning layer used for unpinned dockable toolbars
	 *
	 * @param layer Pinning layer
	 */
	public void setPinningLayer(int layer) {
		pinningLayer = layer;
	}

	/**
	 * Get the main panel contained in this root panel
	 *
	 * @return Main panel
	 */
	public DockingPanel getPanel() {
		return panel;
	}

	/**
	 * Check if this root is empty
	 *
	 * @return True if empty
	 */
	public boolean isEmpty() {
		return panel == null;
	}

	/**
	 * Set the main panel
	 *
	 * @param panel New main panel
	 */
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
		Window rootWindow = (Window) SwingUtilities.getRoot(this);
		Docking.deregisterDockingPanel(rootWindow);

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
		else if (Docking.alwaysDisplayTabsMode()) {
			setPanel(new DockedTabbedPanel(DockingInternal.getWrapper(dockable)));
			DockingInternal.getWrapper(dockable).setWindow(window);
		}
		else {
			setPanel(new DockedSimplePanel(DockingInternal.getWrapper(dockable)));
			DockingInternal.getWrapper(dockable).setWindow(window);
		}
	}

	@Override
	public void undock(Dockable dockable) {
		if (supportedToolbars.contains(DockableToolbar.Location.WEST) && westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);
		}
		else if (supportedToolbars.contains(DockableToolbar.Location.EAST) && eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);
		}
		else if (supportedToolbars.contains(DockableToolbar.Location.SOUTH) && southToolbar.hasDockable(dockable)) {
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

	/**
	 * Remove a dockable from its toolbar and pin it back into the root
	 *
	 * @param dockable Dockable to pin
	 */
	public void setDockablePinned(Dockable dockable) {
		// if the dockable is currently unpinned, remove it from the toolbar, then adjust the toolbars
		if (supportedToolbars.contains(DockableToolbar.Location.WEST) && westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.WEST, 0.25f);
		}
		else if (supportedToolbars.contains(DockableToolbar.Location.EAST) && eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.EAST, 0.25f);
		}
		else if (supportedToolbars.contains(DockableToolbar.Location.SOUTH) && southToolbar.hasDockable(dockable)) {
			southToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.SOUTH, 0.25f);
		}

		createContents();
	}

	/**
	 * set a dockable to be unpinned at the given location
	 *
	 * @param dockable Dockable to unpin
	 * @param location Toolbar to unpin to
	 */
	public void setDockableUnpinned(Dockable dockable, DockableToolbar.Location location) {
		switch (location) {
			case WEST: {
				if (supportedToolbars.contains(DockableToolbar.Location.WEST)) {
					westToolbar.addDockable(dockable);
				}
				break;
			}
			case SOUTH: {
				if (supportedToolbars.contains(DockableToolbar.Location.SOUTH)) {
					southToolbar.addDockable(dockable);
				}
				break;
			}
			case EAST: {
				if (supportedToolbars.contains(DockableToolbar.Location.EAST)) {
					eastToolbar.addDockable(dockable);
				}
				break;
			}
		}

		createContents();
	}

	/**
	 * Get a list of the unpinned dockables on the specified toolbar
	 *
	 * @param location Toolbar location
	 * @return List of unpinned dockables
	 */
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

		if (isPinningSupported() && westToolbar.shouldDisplay()) {
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

		if (isPinningSupported() && eastToolbar.shouldDisplay()) {
			add(eastToolbar, gbc);
		}

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if (isPinningSupported() && southToolbar.shouldDisplay()) {
			add(southToolbar, gbc);
		}

		revalidate();
		repaint();
	}

	/**
	 * Hide all unpinned panels on the west, south and east toolbars
	 */
	public void hideUnpinnedPanels() {
		westToolbar.hideAll();
		southToolbar.hideAll();
		eastToolbar.hideAll();
	}

	/**
	 * Get a list of IDs for unpinned dockables on the west toolbar
	 *
	 * @return Persistent IDs
	 */
	public List<String> getWestUnpinnedToolbarIDs() {
		if (westToolbar == null) {
			return Collections.emptyList();
		}
		return westToolbar.getPersistentIDs();
	}

	/**
	 * Get a list of IDs for unpinned dockables on the east toolbar
	 *
	 * @return Persistent IDs
	 */
	public List<String> getEastUnpinnedToolbarIDs() {
		if (eastToolbar == null) {
			return Collections.emptyList();
		}
		return eastToolbar.getPersistentIDs();
	}

	/**
	 * Get a list of IDs for unpinned dockables on the south toolbar
	 *
	 * @return Persistent IDs
	 */
	public List<String> getSouthUnpinnedToolbarIDs() {
		if (southToolbar == null) {
			return Collections.emptyList();
		}
		return southToolbar.getPersistentIDs();
	}
}
