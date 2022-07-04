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
package modern_docking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// simple docking panel that only has a single Dockable in the center
public class DockedSimplePanel extends DockingPanel implements MouseListener {
	private final DockableWrapper dockable;

	private DockingPanel parent;

	public DockedSimplePanel(DockableWrapper dockable) {
		setLayout(new BorderLayout());

		setNotSelectedBorder();

		dockable.setParent(this);

		this.dockable = dockable;

		add((JComponent) dockable.getDockable(), BorderLayout.CENTER);

		((Component) dockable.getDockable()).addMouseListener(this);
	}

	public DockableWrapper getWrapper() {
		return dockable;
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		// docking to CENTER: Simple -> Tabbed
		// docking else where: Simple -> Split
		DockableWrapper wrapper = Docking.getWrapper(dockable);
		wrapper.setFrame(this.dockable.getFrame());

		if (region == DockingRegion.CENTER) {
			DockedTabbedPanel tabbedPanel = new DockedTabbedPanel();

			tabbedPanel.addPanel(this.dockable);
			tabbedPanel.addPanel(wrapper);

			parent.replaceChild(this, tabbedPanel);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel(this.dockable.getFrame());
			parent.replaceChild(this, split);

			DockedSimplePanel newPanel = new DockedSimplePanel(wrapper);

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(newPanel);
				dividerProportion = 1.0 - dividerProportion;
			}
			else {
				split.setLeft(newPanel);
				split.setRight(this);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
				split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
			else {
				split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}

			split.setDividerLocation(dividerProportion);
		}

		revalidate();
		repaint();
	}

	@Override
	public void undock(Dockable dockable) {
		if (this.dockable.getDockable() == dockable) {
//			System.out.println("Undocked panel from DockedSimplePanel");
			remove((JComponent) this.dockable.getDockable());

			parent.removeChild(this);

			this.dockable.setParent(null);

			revalidate();
			repaint();
		}
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		// no-op, simple panel has no children
	}

	@Override
	public void removeChild(DockingPanel child) {
		// no-op, simple panel has no children
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Color color = UIManager.getColor("Component.focusColor");
		setBorder(BorderFactory.createLineBorder(color, 2));
	}

	@Override
	public void mouseExited(MouseEvent e) {
//		setNotSelectedBorder();
	}

	private void setNotSelectedBorder() {
		Color color = UIManager.getColor("Component.borderColor");

		setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 1),
						BorderFactory.createLineBorder(color, 1)
				)
		);
	}
}
