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

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayoutNode;
import io.github.andrewauclair.moderndocking.layouts.DockingLayoutRootNode;
import io.github.andrewauclair.moderndocking.layouts.DockingSimplePanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingTabPanelNode;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to help create layouts without directly applying them to the actual app
 */
public class WindowLayoutBuilderAPI {
	private final DockingLayoutRootNode rootNode;

	private final Map<String, Map<String, Property>> properties = new HashMap<>();

	/**
	 * Start building a new layout
	 *
	 * @param docking The docking instance we're building a layout for
	 * @param firstID First dockable ID in the layout
	 */
	protected WindowLayoutBuilderAPI(DockingAPI docking, String firstID) {
		rootNode = new DockingLayoutRootNode(docking);
		rootNode.dock(firstID, DockingRegion.CENTER, 0.0);
	}

	/**
	 * Dock a new dockable into this layout
	 *
	 * @param sourceID The new dockable
	 * @param targetID The dockable to dock to the center of
	 * @return This builder in order to chain calls
	 */
	public WindowLayoutBuilderAPI dock(String sourceID, String targetID) {
		return dock(sourceID, targetID, DockingRegion.CENTER);
	}

	/**
	 * Dock a new dockable into this layout
	 *
	 * @param sourceID The new dockable
	 * @param targetID The dockable to dock to
	 * @param region The region on the dockable to dock to
	 * @return This builder in order to chain calls
	 */
	public WindowLayoutBuilderAPI dock(String sourceID, String targetID, DockingRegion region) {
		return dock(sourceID, targetID, region, 0.5);
	}

	/**
	 * Dock a new dockable into this layout
	 *
	 * @param sourceID The new dockable
	 * @param targetID The dockable to dock to
	 * @param region The region on the dockable to dock to
	 * @param dividerProportion The divider proportion to use if creating a split pane
	 * @return This builder in order to chain calls
	 */
	public WindowLayoutBuilderAPI dock(String sourceID, String targetID, DockingRegion region, double dividerProportion) {
		DockingLayoutNode node = findNode(targetID);

		if (exists(sourceID)) {
			throw new RuntimeException("Dockable already in layout: " + sourceID);
		}
		node.dock(sourceID, region, dividerProportion);

		return this;
	}

	/**
	 * Dock a dockable to the root in this layout builder
	 *
	 * @param persistentID Persistent ID of the dockable
	 * @param region The region to dock into
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI dockToRoot(String persistentID, DockingRegion region) {
		return dockToRoot(persistentID, region, 0.25);
	}

	/**
	 * Dock a dockable to the root in this layout builder
	 * @param persistentID Persistent ID of the dockable
	 * @param region The region to dock into
	 * @param dividerProportion The divider proportion to use
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI dockToRoot(String persistentID, DockingRegion region, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}

		rootNode.dock(persistentID, region, dividerProportion);
		return this;
	}

	/**
	 * Bring a dockable to the front in this builder
	 *
	 * @param persistentID The dockable to bring to front
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI display(String persistentID) {
		DockingLayoutNode node = findNode(persistentID);

		if (node.getParent() != null && node.getParent() instanceof DockingTabPanelNode) {
			((DockingTabPanelNode) node.getParent()).bringToFront(node);
		}

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, byte value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.ByteProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, short value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.ShortProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, int value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.IntProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, long value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.LongProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, float value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.FloatProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, double value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.DoubleProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, char value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.CharacterProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set a property for the dockable
	 *
	 * @param persistentID The dockable persistent ID
	 * @param property The name of the property to set
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, boolean value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.BooleanProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, String value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, new Property.StringProperty(property, value));

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * Set the value of a custom dockable property
	 *
	 * @param persistentID The persistent ID of the dockable we're setting a property for
	 * @param property The name of the property we're configuring
	 * @param value The value of the property
	 *
	 * @return This WindowLayoutBuilderAPI instance
	 */
	public WindowLayoutBuilderAPI setProperty(String persistentID, String property, Property value) {
		Map<String, Property> props = properties.getOrDefault(persistentID, new HashMap<>());

		props.put(property, value);

		properties.put(persistentID, props);

		return this;
	}

	/**
	 * build a WindowLayout using the rootNode
	 *
	 * @return The built window layout
	 */
	public WindowLayout build() {
		properties.forEach((persistentID, stringPropertyMap) -> {
			DockingLayoutNode node = findNode(persistentID);

			if (node instanceof DockingSimplePanelNode) {
				((DockingSimplePanelNode) node).setProperties(stringPropertyMap);
			}

		});
		return new WindowLayout(rootNode.getNode());
	}

	/**
	 * Shortcut for building an ApplicationLayout from this builder's WindowLayout
	 *
	 * @return Application layout
	 */
	public ApplicationLayout buildApplicationLayout() {
		return new ApplicationLayout(build());
	}

	/**
	 * Find the node for a dockable
	 *
	 * @param persistentID Persistent ID of the dockabe to find
	 *
	 * @return Dockable layout node or null if not found
	 */
	public DockingLayoutNode findNode(String persistentID) {
		DockingLayoutNode node = rootNode.findNode(persistentID);

		if (node == null) {
			throw new RuntimeException("No node for dockable ID found: " + persistentID);
		}
		return node;
	}

	private boolean exists(String persistentID) {
		DockingLayoutNode node = rootNode.findNode(persistentID);

		return node != null;
	}
}
