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
package ModernDocking.layouts;

import ModernDocking.DockingRegion;

import javax.swing.*;

// Utility to help create layouts without directly applying them to the actual app
public class WindowLayoutBuilder {
	private final DockingLayoutRootNode rootNode = new DockingLayoutRootNode();

	public WindowLayoutBuilder(String firstID) {
		rootNode.dock(firstID, DockingRegion.CENTER, 0.0);
	}

	public WindowLayoutBuilder dock(String sourceID, String targetID) {
		return dock(sourceID, targetID, DockingRegion.CENTER);
	}

	public WindowLayoutBuilder dock(String sourceID, String targetID, DockingRegion region) {
		return dock(sourceID, targetID, region, 0.5);
	}

	public WindowLayoutBuilder dock(String sourceID, String targetID, DockingRegion region, double dividerProportion) {
		DockingLayoutNode node = findNode(targetID);

		if (exists(sourceID)) {
			throw new RuntimeException("Dockable already in layout: " + sourceID);
		}
		node.dock(sourceID, region, dividerProportion);

		return this;
	}

	public WindowLayoutBuilder dockToRoot(String persistentID, DockingRegion region) {
		return dockToRoot(persistentID, region, 0.25);
	}

	public WindowLayoutBuilder dockToRoot(String persistentID, DockingRegion region, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}

		rootNode.dock(persistentID, region, dividerProportion);
		return this;
	}

	public WindowLayoutBuilder display(String persistentID) {
		DockingLayoutNode node = findNode(persistentID);

		if (node.getParent() != null && node.getParent() instanceof DockingTabPanelNode) {
			((DockingTabPanelNode) node.getParent()).bringToFront(node);
		}

		return this;
	}

	// build a WindowLayout using the rootNode
	public WindowLayout build() {
		return new WindowLayout(rootNode.getNode());
	}

	// shortcut for building an ApplicationLayout from this builder's WindowLayout
	public ApplicationLayout buildApplicationLayout() {
		return new ApplicationLayout(build());
	}

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
