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

import javax.swing.*;
import java.util.List;

/**
 * Docking panel with docking regions of: north, south, east, west and center
 */
public abstract class DockingPanel extends JPanel {
	public abstract String getAnchor();

	public void setAnchor(String anchor) {
	}

	/**
	 * Set the parent of this DockingPanel
	 *
	 * @param parent The new parent of this panel
	 */
	public abstract void setParent(DockingPanel parent);

	/**
	 * Dock a dockable to this panel
	 *
	 * @param dockable The dockable to dock
	 * @param region The region to dock into
	 * @param dividerProportion The proportion to use if docking as a split pane
	 */
	public abstract void dock(Dockable dockable, DockingRegion region, double dividerProportion);

	/**
	 * undock the given dockable, returns true if the dockable was found and removed
	 *
	 * @param dockable Dockable to undock
	 */
	public abstract void undock(Dockable dockable);

	/**
	 * Replace one of the children in this panel with a new child
	 *
	 * @param child The child to replace
	 * @param newChild The new child to add
	 */
	public abstract void replaceChild(DockingPanel child, DockingPanel newChild);

	/**
	 * Remove the specified child from this panel
	 *
	 * @param child Child to remove
	 */
	public abstract void removeChild(DockingPanel child);

	public abstract List<DockingPanel> getChildren();

	public boolean isInAutoHideToolbar() {
		return false;
	}
}
