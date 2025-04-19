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
package io.github.andrewauclair.moderndocking.api;

import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.EnumSet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * Panel that should be added to each frame that should support docking
 */
public class RootDockingPanelAPI extends JPanel {
	/**
	 * The docking instance this root docking panel belongs to
	 */
	private DockingAPI docking = null;

	/**
	 * The window this root docking panel is contained in
	 */
	private Window window = null;

	/**
	 * A panel to display when there are no dockables in the root docking panel
	 */
	private JPanel emptyPanel = new JPanel();

	/**
	 * The layer to display auto-hide dockables in when showing. This is used because the dockable panel needs
	 * to be displayed above the rest of the panels
	 */
	private int autoHideLayer = JLayeredPane.MODAL_LAYER;

	/**
	 * The toolbars that are supported on this root docking panel. The application can turn some off
	 */
	private EnumSet<ToolbarLocation> supportedToolbars = EnumSet.noneOf(ToolbarLocation.class);

	/**
	 * Does this root docking panel support the toolbars and display of auto-hide dockables?
	 */
	private boolean autoHideSupported = false;

	/**
	 * Create a new default panel with a GridBagLayout
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
		this.docking = docking;
		this.window = window;

		supportedToolbars = EnumSet.allOf(ToolbarLocation.class);
		autoHideSupported = !supportedToolbars.isEmpty();

		setWindow(window);
	}

	/**
	 * Set the parent window of this root
	 *
	 * @param window Parent window of root
	 */
	private void setWindow(Window window) {
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

		supportedToolbars = EnumSet.allOf(ToolbarLocation.class);
		autoHideSupported = !supportedToolbars.isEmpty();
	}

	/**
	 * Create a new RootDockingPanel for the given window and set of supported toolbars
	 *
	 * @param docking The docking instance for this root
	 * @param window Window this root panel is attached to
	 * @param supportedToolbars Supported toolbars
	 */
	protected RootDockingPanelAPI(DockingAPI docking, Window window, EnumSet<ToolbarLocation> supportedToolbars) {
		this(docking, window);

		this.supportedToolbars = supportedToolbars;
		autoHideSupported = !supportedToolbars.isEmpty();
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
	 * Get the empty panel
	 *
	 * @return Current empty panel
	 */
	public JPanel getEmptyPanel() {
		return emptyPanel;
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
	 * Check if auto hide is supported on this root
	 *
	 * @return True if auto hide is supported
	 */
	public boolean isAutoHideSupported() {
		if (supportedToolbars.isEmpty()) {
			// if there are no auto hide tool bars then we can't support auto hide
			return false;
		}
		return autoHideSupported;
	}

	/**
	 * Set auto hide supported flag
	 *
	 * @param supported Is auto hide supported?
	 */
	public void setAutoHideSupported(boolean supported) {
		autoHideSupported = supported;
	}

	/**
	 * Get the layer that is being used for auto hide
	 *
	 * @return Auto hide layer
	 */
	public int getAutoHideLayer() {
		return autoHideLayer;
	}

	/**
	 * Set the auto hide layer used for auto hide dockable toolbars
	 *
	 * @param layer Auto hide layer
	 */
	public void setAutoHideLayer(int layer) {
		autoHideLayer = layer;
	}

	/**
	 * Check if an auto-hide toolbar location is supported
	 * @param location Location to check
	 *
	 * @return Is the location supported?
	 */
	public boolean isLocationSupported(ToolbarLocation location) {
		return supportedToolbars.contains(location);
	}
}
