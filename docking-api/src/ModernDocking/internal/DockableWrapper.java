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
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.api.DockingAPI;
import ModernDocking.floating.FloatListener;
import ModernDocking.settings.Settings;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;
import java.awt.*;

/**
 * internal wrapper around the Dockable implemented by the application.
 * lets us provide access to the dockable and its parent in the hierarchy
 */
public class DockableWrapper {
	private final HeaderController headerController;
	private Window window;
	private DockingPanel parent = null;
	private final Dockable dockable;
	private final DockingAPI docking;

	private FloatListener floatListener;
	private final DockingHeaderUI headerUI;

	private final DisplayPanel displayPanel;

	private boolean maximized = false;
	private boolean unpinned = false;
	private RootDockingPanelAPI root;

	/**
	 * Create a new wrapper for the dockable
	 *
	 * @param dockable Dockable to contain in this wrapper
	 */
	public DockableWrapper(DockingAPI docking, Dockable dockable) {
		this.docking = docking;
		this.dockable = dockable;

		HeaderModel headerModel = new HeaderModel(dockable, docking);
		headerController = new HeaderController(dockable, docking, headerModel);
		headerUI = dockable.createHeaderUI(headerController, headerModel);
		headerController.setUI(headerUI);

		floatListener = new FloatListener(docking, this, (JComponent) headerUI);
		displayPanel = new DisplayPanel(this);
	}

	/**
	 * Get the window for the dockable
	 *
	 * @return The window that the contained dockable is in
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Set the new window of the dockable
	 *
	 * @param window New window
	 */
	public void setWindow(Window window) {
		this.window = window;
	}

	/**
	 * Set the new parent of the dockable
	 *
	 * @param parent New parent
	 */
	public void setParent(DockingPanel parent) {
		this.parent = parent;

		if (parent instanceof DockedTabbedPanel && Settings.alwaysDisplayTabsMode()) {
			floatListener = new FloatListener(docking, this, ((DockedTabbedPanel) parent).getTabForDockable(this));
		}

		displayPanel.parentChanged();
	}

	/**
	 * Get the contained dockable
	 *
	 * @return The dockable contained in this wrapper
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Remove any floating listeners that this wrapper has added to the dockables header controller
	 */
	public void removedListeners() {
		if (floatListener != null) {
			headerController.removeListeners();

			// make sure we don't get a stackoverflow
			FloatListener listener = floatListener;
			floatListener = null;

			listener.removeListeners();
		}
	}

	/**
	 * Get the parent of this wrapper
	 *
	 * @return Parent of wrapper
	 */
	public DockingPanel getParent() {
		return parent;
	}

	/**
	 * Check if the dockable is maximized
	 *
	 * @return Whether the dockable is maximized
	 */
	public boolean isMaximized() {
		return maximized;
	}

	/**
	 * Set the dockable to maximized
	 *
	 * @param maximized Maximized flag
	 */
	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}

	/**
	 * Check if the dockable is unpinned
	 *
	 * @return Whether the dockable is unpinned
	 */
	public boolean isUnpinned() {
		return unpinned;
	}

	/**
	 * Set the dockable to unpinned
	 *
	 * @param unpinned Unpinned flag
	 */
	public void setUnpinned(boolean unpinned) {
		this.unpinned = unpinned;

		displayPanel.parentChanged();
	}

	/**
	 * Get the header UI of the dockable
	 *
	 * @return Header UI instance
	 */
	public DockingHeaderUI getHeaderUI() {
		return headerUI;
	}

	/**
	 * Get the display panel
	 *
	 * @return Display panel instance
	 */
	public DisplayPanel getDisplayPanel() {
		return displayPanel;
	}

	/**
	 * Change the root that this dockable is in
	 *
	 * @param root New root of dockable
	 */
	public void setRoot(RootDockingPanelAPI root) {
		this.root = root;
	}

	/**
	 * Get the root that contains this dockable
	 *
	 * @return Root panel containing dockable
	 */
	public RootDockingPanelAPI getRoot() {
		return root;
	}
}
