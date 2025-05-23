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
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.floating.DisplayPanelFloatListener;
import io.github.andrewauclair.moderndocking.floating.FloatListener;
import io.github.andrewauclair.moderndocking.ui.DockingHeaderUI;
import io.github.andrewauclair.moderndocking.ui.HeaderController;
import io.github.andrewauclair.moderndocking.ui.HeaderModel;
import java.awt.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * internal wrapper around the Dockable implemented by the application.
 * lets us provide access to the dockable and its parent in the hierarchy
 */
public class DockableWrapper {
	private final HeaderController headerController;
	private Window window;
	// TODO this is suddenly very confusing to me. This is really just the display panel it's connected to. which for simple panels is direct, but for tabbed, it's not
	private DockingPanel parent = null;
	private final Dockable dockable;
	private final DockingAPI docking;

	private String anchor = "";

	private final DockingHeaderUI headerUI;

	private final DisplayPanel displayPanel;
	private DockingPanel internalPanel;

	private final FloatListener floatListener;

	private boolean maximized = false;
	private boolean hidden = false;
	private boolean isAnchor = false;
	private InternalRootDockingPanel root;

	private final Map<String, Property> properties = new HashMap<>();

	/**
	 * Create a new wrapper for the dockable
	 *
	 * @param docking The docking instance
	 * @param dockable Dockable to contain in this wrapper
	 * @param isAnchor Is this wrapper an anchor instead of a dockable
	 */
	public DockableWrapper(DockingAPI docking, Dockable dockable, boolean isAnchor) {
		this.docking = docking;
		this.dockable = dockable;
		this.isAnchor = isAnchor;

		HeaderModel headerModel = new HeaderModel(dockable, docking);
		headerController = new HeaderController(dockable, docking, headerModel);
		headerUI = dockable.createHeaderUI(headerController, headerModel);
		headerController.setUI(headerUI);

		displayPanel = new DisplayPanel(this, isAnchor);

		floatListener = new DisplayPanelFloatListener(docking, displayPanel);
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
	 * Get the float listener tied to the display panel of this dockable
	 *
	 * @return Float listener
	 */
	public FloatListener getFloatListener() {
		return floatListener;
	}

	/**
	 * Remove any listeners that this wrapper has added for the dockable
	 */
	public void removeListeners() {
		headerController.removeListeners();
		floatListener.removeListeners();
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
	 * Check if the dockable is auto hide enabled
	 *
	 * @return Whether the dockable is auto hide enabled
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Set the dockable to auto hide
	 *
	 * @param hidden Hidden flag
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;

		displayPanel.parentChanged();
	}

	/**
	 * Check if this wrapper represents an anchor instead of a dockable
	 *
	 * @return Is this wrapper an anchor?
	 */
	public boolean isAnchor() {
		return isAnchor;
	}

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
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

	public DockingPanel getInternalPanel() {
		return internalPanel;
	}

	public void setInternalPanel(DockingPanel panel) {
		this.internalPanel = panel;
	}

	/**
	 * Change the root that this dockable is in
	 *
	 * @param root New root of dockable
	 */
	public void setRoot(InternalRootDockingPanel root) {
		this.root = root;
	}

	/**
	 * Get the root that contains this dockable
	 *
	 * @return Root panel containing dockable
	 */
	public InternalRootDockingPanel getRoot() {
		return root;
	}

	/**
	 * Get the properties for this dockable
	 *
	 * @return Map of properties for this dockable
	 */
	public Map<String, Property> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Get the value of a property
	 *
	 * @param propertyName The name of the property to lookup
	 * @return Value of property, or null if it does not exist
	 */
	public Property getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	/**
	 * Set a new value for a property. The property will be created if missing
	 *
	 * @param propertyName The name of the property to set
	 * @param value The new value of the property
	 */
	public void setProperty(String propertyName, Property value) {
		properties.put(propertyName, value);
	}

	/**
	 * Remove a property from this dockable
	 *
	 * @param propertyName The name of the property to remove
	 */
	public void removeProperty(String propertyName) {
		properties.remove(propertyName);
	}
}
