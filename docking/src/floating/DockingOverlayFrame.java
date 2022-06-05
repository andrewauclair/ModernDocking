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
package floating;

import docking.Dockable;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// displays the overlay highlight of where the panel will be docked
public class DockingOverlayFrame extends JFrame implements MouseMotionListener, MouseListener {
	private static final double REGION_SENSITIVITY = 0.35;

	private RootDockingPanel rootPanel;
	private Dockable targetDockable;
	private Dockable floating;
	private DockingRegion dockableRegion;
	private DockingRegion rootRegion;

	public DockingOverlayFrame() {
		setUndecorated(true);

		setBackground(new Color(0, 0, 0, 50));

		setSize(1, 1);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void dockingComplete() {
		rootPanel = null;
		targetDockable = null;
		floating = null;
		dockableRegion = null;
		rootRegion = null;

		setSize(1, 1);
	}

	public void setRoot(RootDockingPanel panel) {
		rootPanel = panel;

		setVisible(targetDockable != null || rootPanel != null);
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	public void setTargetDockable(Dockable dockable) {
		targetDockable = dockable;

		setVisible(targetDockable != null || rootPanel != null);
	}

	public void update(Point screenPos) {
		if (rootPanel != null && rootRegion != null) {
			Point point = rootPanel.getLocation();
			Dimension size = rootPanel.getSize();

			switch (rootRegion) {
				case WEST -> size = new Dimension(size.width / 2, size.height);
				case NORTH -> size = new Dimension(size.width, size.height / 2);
				case EAST -> {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
				case SOUTH -> {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			SwingUtilities.convertPointToScreen(point, rootPanel);

			setLocation(point);
			setSize(size);
		}
		else if (targetDockable != null && dockableRegion != null) {
			JComponent component = (JComponent) targetDockable;

			Point point = component.getLocation();
			Dimension size = component.getSize();

			switch (dockableRegion) {
				case WEST -> size = new Dimension(size.width / 2, size.height);
				case NORTH -> size = new Dimension(size.width, size.height / 2);
				case EAST -> {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
				case SOUTH -> {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			SwingUtilities.convertPointToScreen(point, component);

			setLocation(point);
			setSize(size);
		}
		else if (targetDockable != null) {
			JComponent component = (JComponent) targetDockable;

			Point framePoint = new Point(screenPos);
			SwingUtilities.convertPointFromScreen(framePoint, component);

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			double horizontalPct = (framePoint.x - point.x) / (double) size.width;
			double verticalPct = (framePoint.y - point.y) / (double) size.height;

			double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
			double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

			if (horizontalEdgeDist < verticalEdgeDist) {
				if (horizontalPct < REGION_SENSITIVITY) {
					size = new Dimension(size.width / 2, size.height);
				}
				else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
			}
			else {
				if (verticalPct < REGION_SENSITIVITY) {
					size = new Dimension(size.width, size.height / 2);
				}
				else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			SwingUtilities.convertPointToScreen(point, component);

			setLocation(point);
			setSize(size);
		}
	}

	public DockingRegion getRegion(Point screenPos) {
		if (rootRegion != null) {
			return rootRegion;
		}

		if (dockableRegion != null) {
			return dockableRegion;
		}

		if (targetDockable == null) {
			return DockingRegion.CENTER;
		}

		JComponent component = (JComponent) targetDockable;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component);

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		double horizontalPct = (framePoint.x - point.x) / (double) size.width;
		double verticalPct = (framePoint.y - point.y) / (double) size.height;

		double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
		double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

		if (horizontalEdgeDist < verticalEdgeDist) {
			if (horizontalPct < REGION_SENSITIVITY) {
				return DockingRegion.WEST;
			}
			else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.EAST;
			}
		}
		else {
			if (verticalPct < REGION_SENSITIVITY) {
				return DockingRegion.NORTH;
			}
			else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.SOUTH;
			}
		}
		return DockingRegion.CENTER;
	}

	// we don't want to use the mouse events in this overlay frame because that would break the app
	// pass them off to the component that we really need them in, the drag source
	private void dispatchEvent(MouseEvent e) {
		if (floating != null) {
			floating.dragSource().dispatchEvent(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		dispatchEvent(e);
	}

	public boolean isDockingToRoot() {
		return rootRegion != null;
	}

	public void setTargetRootRegion(DockingRegion region) {
		rootRegion = region;
	}

	public void setTargetDockableRegion(DockingRegion region) {
		dockableRegion = region;
	}
}
