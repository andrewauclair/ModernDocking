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
package layouts;

import docking.DockingRegion;

import javax.swing.*;

public class DockingLayoutBuilder {
	private final JFrame frame;
	private DockingLayoutNode rootNode;

	public DockingLayoutBuilder(JFrame frame, String firstID) {
		this.frame = frame;
		rootNode = new DockingSimplePanelNode(firstID);
	}

	public DockingLayoutBuilder dock(String targetID, String persistentID, DockingRegion region) {
		DockingLayoutNode node = findNode(targetID);
		node.dock(persistentID, region);

		return this;
	}

	public DockingLayoutBuilder dockToRootNorth(String persistentID) {
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.VERTICAL_SPLIT, 0.5f);
		return this;
	}

	public DockingLayoutBuilder dockToRootNorth(String persistentID, float dividerProportion) {
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.VERTICAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootSouth(String persistentID) {
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.VERTICAL_SPLIT, 0.5f);
		return this;
	}

	public DockingLayoutBuilder dockToRootSouth(String persistentID, float dividerProportion) {
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.VERTICAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootWest(String persistentID) {
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.HORIZONTAL_SPLIT, 0.5f);
		return this;
	}

	public DockingLayoutBuilder dockToRootWest(String persistentID, float dividerProportion) {
		rootNode = new DockingSplitPanelNode(rootNode, new DockingSimplePanelNode(persistentID), JSplitPane.HORIZONTAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayoutBuilder dockToRootEast(String persistentID) {
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.HORIZONTAL_SPLIT, 0.5f);
		return this;
	}

	public DockingLayoutBuilder dockToRootEast(String persistentID, float dividerProportion) {
		rootNode = new DockingSplitPanelNode(new DockingSimplePanelNode(persistentID), rootNode, JSplitPane.HORIZONTAL_SPLIT, dividerProportion);
		return this;
	}

	public DockingLayout build() {
		return new DockingLayout(frame, rootNode);
	}

	// TODO throw exception if a persistent id already exists
	// TODO probably throw an exception that it doesn't exist
	private DockingLayoutNode findNode(String persistentID) {
		return rootNode.findNode(persistentID);
	}
}
