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
package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.DockingRegion;

/**
 * Base interface for docking layout nodes, simple, split, tab and empty
 */
public interface DockingLayoutNode {
	/**
	 * Find a node in the layout
	 *
	 * @param persistentID Persistent ID to search for
	 * @return The layout node, if found. null if not found.
	 */
	DockingLayoutNode findNode(String persistentID);

	/**
	 * Dock a new persistent ID into this node
	 *
	 * @param persistentID Persistent ID of dockable to add
	 * @param region Region to dock into
	 * @param dividerProportion Proportion to use if in a splitpane
	 */
	void dock(String persistentID, DockingRegion region, double dividerProportion);

	/**
	 * Replace an existing layout node child
	 *
	 * @param child Child to replace
	 * @param newChild Child to add
	 */
	void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild);

	/**
	 * Get the parent of this node
	 *
	 * @return Parent of node, null if root.
	 */
	DockingLayoutNode getParent();

	/**
	 * Set the parent of this node
	 *
	 * @param parent New parent
	 */
	void setParent(DockingLayoutNode parent);
}
