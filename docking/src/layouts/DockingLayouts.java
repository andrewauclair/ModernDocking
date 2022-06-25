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

import docking.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockingLayouts {
	private final Map<String, DockingLayout> layouts = new HashMap<>();

	public void registerLayout(String name, DockingLayout layout) {
		layouts.put(name, layout);
	}

	public DockingLayout getLayout(String name) {
		return layouts.get(name);
	}

	public List<String> getLayoutNames() {
		return new ArrayList<>(layouts.keySet());
	}



	public static DockingLayout layoutFromRoot(RootDockingPanel root) {
		return new DockingLayout(Docking.frameForRoot(root), panelToNode(root.getPanel()));
	}

	private static DockingLayoutNode panelToNode(DockingPanel panel) {
		DockingLayoutNode node;
		if (panel instanceof DockedSimplePanel) {
			node = new DockingSimplePanelNode(((DockedSimplePanel) panel).getWrapper().getDockable().persistentID());
		}
		else if (panel instanceof DockedSplitPanel) {
			node = splitPanelToNode((DockedSplitPanel) panel);
		}
		else if (panel instanceof DockedTabbedPanel) {
			node = tabbedPanelToNode((DockedTabbedPanel) panel);
		}
		else {
			throw new RuntimeException("Unknown panel");
		}
		return node;
	}
	private static DockingLayoutNode splitPanelToNode(DockedSplitPanel panel) {
		JSplitPane splitPane = panel.getSplitPane();

		int orientation = splitPane.getOrientation();
		int height = splitPane.getHeight();
		int dividerSize = splitPane.getDividerSize();
		int dividerLocation = splitPane.getDividerLocation();
		int width = splitPane.getWidth();
		double dividerProportion = orientation == JSplitPane.VERTICAL_SPLIT ? dividerLocation / (float) (height - dividerSize) :
				dividerLocation / (float) (width - dividerSize);
		return new DockingSplitPanelNode(panelToNode(panel.getLeft()), panelToNode(panel.getRight()), splitPane.getOrientation(), dividerProportion);
	}

	private static DockingLayoutNode tabbedPanelToNode(DockedTabbedPanel panel) {
		DockingTabPanelNode node = new DockingTabPanelNode();

		for (String persistentID : panel.persistentIDs()) {
			node.addTab(persistentID);
		}
		return node;
	}
}
