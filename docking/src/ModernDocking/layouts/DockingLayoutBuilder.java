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
public class DockingLayoutBuilder {
	private final JFrame frame;
	private DockingLayoutNode rootNode;

	public DockingLayoutBuilder(JFrame frame, String firstID) {
		this.frame = frame;
		rootNode = new DockingSimplePanelNode(firstID);
	}

	public DockingLayoutBuilder dock(String targetID, String sourceID, DockingRegion region) {
		DockingLayoutNode node = findNode(targetID);

		if (exists(sourceID)) {
			throw new RuntimeException("Dockable already in layout: " + sourceID);
		}
		node.dock(sourceID, region);

		return this;
	}

	public DockingLayoutBuilder dockToRootNorth(String persistentID) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.VERTICAL_SPLIT, 0.5);
		return this;
	}

	public DockingLayoutBuilder dockToRootNorth(String persistentID, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.VERTICAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootSouth(String persistentID) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.VERTICAL_SPLIT, 0.5);
		return this;
	}

	public DockingLayoutBuilder dockToRootSouth(String persistentID, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.VERTICAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootWest(String persistentID) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.HORIZONTAL_SPLIT, 0.5);
		return this;
	}

	public DockingLayoutBuilder dockToRootWest(String persistentID, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.HORIZONTAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootEast(String persistentID) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.HORIZONTAL_SPLIT, 0.5);
		return this;
	}

	public DockingLayoutBuilder dockToRootEast(String persistentID, double dividerProportion) {
		if (exists(persistentID)) {
			throw new RuntimeException("Dockable already in layout: " + persistentID);
		}
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.HORIZONTAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayout build() {
		return new DockingLayout(frame, rootNode);
	}

	private DockingLayoutNode findNode(String persistentID) {
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
