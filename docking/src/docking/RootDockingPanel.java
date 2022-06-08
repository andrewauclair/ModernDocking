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
package docking;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

// only class that should be used by clients
public class RootDockingPanel extends DockingPanel {
	DockingPanel panel;

	// TODO allow the application to define this empty panel. For example, they might like to display a message along the lines of "dock panels here"
	private JPanel emptyPanel = new JPanel();

	public RootDockingPanel() {
		setLayout(new BorderLayout());
	}

	public DockingPanel getPanel() {
		return panel;
	}

	public void setPanel(DockingPanel panel) {
		boolean repaint = removeExistingPanel();

		this.panel = panel;
		this.panel.setParent(this);

		add(panel, BorderLayout.CENTER);

		if (repaint) {
			revalidate();
			repaint();
		}
	}

	private boolean removeExistingPanel() {
		remove(emptyPanel);

		if (panel != null) {
			remove(panel);
			panel = null;
			return true;
		}
		return false;
	}

	@Override
	public void removeNotify() {
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		Docking.deregisterDockingPanel(frame);

		super.removeNotify();
	}

	@Override
	public void setParent(DockingPanel parent) {
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region) {
		// pass docking to panel if it exists
		// if panel does not exist, create new simple panel
		if (panel != null) {
			panel.dock(dockable, region);
		}
		else {
			setPanel(new DockedSimplePanel(Docking.getWrapper(dockable)));
		}

		revalidate();
		repaint();
	}

	@Override
	public void undock(Dockable dockable) {
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		if (panel == child) {
			setPanel(newChild);
		}
	}

	@Override
	public void removeChild(DockingPanel child) {
		if (child == panel) {
			if (removeExistingPanel()) {
				add(emptyPanel, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		}
	}
}
