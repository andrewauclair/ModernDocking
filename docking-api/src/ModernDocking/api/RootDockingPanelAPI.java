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
package ModernDocking.api;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.internal.*;
import ModernDocking.settings.Settings;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Panel that should be added to each frame that should support docking
 */
public class RootDockingPanelAPI extends DockingPanel {
	private DockingAPI docking = null;

	private Window window = null;

	private DockingPanel panel = null;

	private JPanel emptyPanel = new JPanel();

	private boolean pinningSupported = false;
	private int pinningLayer = JLayeredPane.MODAL_LAYER;

	/**
	 * South toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar southToolbar = null;
	/**
	 * West toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar westToolbar = null;
	/**
	 * East toolbar of this panel. Only created if pinning is supported.
	 */
	private DockableToolbar eastToolbar = null;

	private EnumSet<ToolbarLocation> supportedToolbars = EnumSet.noneOf(ToolbarLocation.class);

	/**
	 * Create root panel with GridBagLayout as the layout
	 */
	protected RootDockingPanelAPI() {
		setLayout(new GridBagLayout());
	}

	/**
	 * Create a new RootDockingPanel for the given window with a specific docking instance
	 *
	 * @param window Window this root panel is attached to
	 * @param docking Instance of the docking framework to use, if multiple are in use
	 */
	protected RootDockingPanelAPI(DockingAPI docking, Window window) {
		setLayout(new GridBagLayout());

		this.window = window;
		this.docking = docking;

		if (window instanceof JFrame) {
			docking.registerDockingPanel(this, (JFrame) window);
		}
		else {
			docking.registerDockingPanel(this, (JDialog) window);
		}

		southToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.SOUTH);
		westToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.WEST);
		eastToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.EAST);

		supportedToolbars = EnumSet.allOf(ToolbarLocation.class);
		pinningSupported = !supportedToolbars.isEmpty();
	}

	/**
	 * Create a new RootDockingPanel for the given window and set of supported toolbars
	 *
	 * @param window Window this root panel is attached to
	 * @param supportedToolbars Supported toolbars
	 */
	protected RootDockingPanelAPI(DockingAPI docking, Window window, EnumSet<ToolbarLocation> supportedToolbars) {
		this(docking, window);

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
			docking.deregisterDockingPanel(this.window);
		}
		this.window = window;

		if (window instanceof JFrame) {
			docking.registerDockingPanel(this, (JFrame) window);
		}
		else {
			docking.registerDockingPanel(this, (JDialog) window);
		}

		southToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.SOUTH);
		westToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.WEST);
		eastToolbar = new DockableToolbar(docking, window, this, ToolbarLocation.EAST);

		supportedToolbars = EnumSet.allOf(ToolbarLocation.class);
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
		if (supportedToolbars.isEmpty()) {
			return false;
		}
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
		// this class has a default constructor which could be called and docking would be null
		if (docking != null) {
			Window rootWindow = (Window) SwingUtilities.getRoot(this);

			docking.deregisterDockingPanel(rootWindow);
		}

		super.removeNotify();
	}

	@Override
	public void setParent(DockingPanel parent) {
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

		// pass docking to panel if it exists
		// panel does not exist, create new simple panel
		if (panel != null) {
			panel.dock(dockable, region, dividerProportion);
		}
		else if (Settings.alwaysDisplayTabsMode(dockable)) {
			setPanel(new DockedTabbedPanel(docking, wrapper));
			wrapper.setWindow(window);
		}
		else {
			setPanel(new DockedSimplePanel(docking, wrapper));
			wrapper.setWindow(window);
		}
	}

	@Override
	public void undock(Dockable dockable) {
		if (supportedToolbars.contains(ToolbarLocation.WEST) && westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);
		}
		else if (supportedToolbars.contains(ToolbarLocation.EAST) && eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);
		}
		else if (supportedToolbars.contains(ToolbarLocation.SOUTH) && southToolbar.hasDockable(dockable)) {
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
	public void setDockableShown(Dockable dockable) {
		// if the dockable is currently unpinned, remove it from the toolbar, then adjust the toolbars
		if (supportedToolbars.contains(ToolbarLocation.WEST) && westToolbar.hasDockable(dockable)) {
			westToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.WEST, 0.25f);
		}
		else if (supportedToolbars.contains(ToolbarLocation.EAST) && eastToolbar.hasDockable(dockable)) {
			eastToolbar.removeDockable(dockable);

			dock(dockable, DockingRegion.EAST, 0.25f);
		}
		else if (supportedToolbars.contains(ToolbarLocation.SOUTH) && southToolbar.hasDockable(dockable)) {
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
	public void setDockableHidden(Dockable dockable, ToolbarLocation location) {
		if (!isPinningSupported()) {
			return;
		}

		switch (location) {
			case WEST: {
				if (supportedToolbars.contains(ToolbarLocation.WEST)) {
					westToolbar.addDockable(dockable);
				}
				break;
			}
			case SOUTH: {
				if (supportedToolbars.contains(ToolbarLocation.SOUTH)) {
					southToolbar.addDockable(dockable);
				}
				break;
			}
			case EAST: {
				if (supportedToolbars.contains(ToolbarLocation.EAST)) {
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
	public List<String> hiddenPersistentIDs(ToolbarLocation location) {
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
	public void hideHiddenPanels() {
		if (westToolbar != null) {
			westToolbar.hideAll();
		}
		if (southToolbar != null) {
			southToolbar.hideAll();
		}
		if (eastToolbar != null) {
			eastToolbar.hideAll();
		}
	}

	/**
	 * Get a list of IDs for unpinned dockables on the west toolbar
	 *
	 * @return Persistent IDs
	 */
	public List<String> getWestAutoHideToolbarIDs() {
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
	public List<String> getEastAutoHideToolbarIDs() {
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
	public List<String> getSouthAutoHideToolbarIDs() {
		if (southToolbar == null) {
			return Collections.emptyList();
		}
		return southToolbar.getPersistentIDs();
	}

	public boolean isLocationSupported(ToolbarLocation location) {
		return supportedToolbars.contains(location);
	}

	public void updateLAF() {
		if (southToolbar != null) {
			SwingUtilities.updateComponentTreeUI(southToolbar);
		}
		if (westToolbar != null) {
			SwingUtilities.updateComponentTreeUI(westToolbar);
		}
		if (eastToolbar != null) {
			SwingUtilities.updateComponentTreeUI(eastToolbar);
		}
	}
}
