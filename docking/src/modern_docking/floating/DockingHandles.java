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
package modern_docking.floating;

import modern_docking.Dockable;
import modern_docking.DockingColors;
import modern_docking.DockingRegion;
import modern_docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static modern_docking.floating.DockingHandle.HANDLE_ICON_SIZE;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandles {
	private final DockingHandle rootCenter = new DockingHandle(DockingRegion.CENTER, true);
	private final DockingHandle rootWest = new DockingHandle(DockingRegion.WEST, true);
	private final DockingHandle rootNorth = new DockingHandle(DockingRegion.NORTH, true);
	private final DockingHandle rootEast = new DockingHandle(DockingRegion.EAST, true);
	private final DockingHandle rootSouth = new DockingHandle(DockingRegion.SOUTH, true);

	private final DockingHandle dockableCenter = new DockingHandle(DockingRegion.CENTER, false);
	private final DockingHandle dockableWest = new DockingHandle(DockingRegion.WEST, false);
	private final DockingHandle dockableNorth = new DockingHandle(DockingRegion.NORTH, false);
	private final DockingHandle dockableEast = new DockingHandle(DockingRegion.EAST, false);
	private final DockingHandle dockableSouth = new DockingHandle(DockingRegion.SOUTH, false);

	private final Map<DockingHandle, Boolean> mouseOver = new HashMap<>();

	private DockingRegion rootRegion = null;
	private DockingRegion dockableRegion = null;

	private final JFrame utilFrame;
	private final RootDockingPanel targetRoot;

	// the dockable that we're currently trying to dock and is floating in a TempFloatingFrame
	private Dockable floating;
	// the dockable that the mouse is currently over, can be null
	private Dockable targetDockable = null;

	public DockingHandles(JFrame utilFrame, RootDockingPanel root) {
		this.utilFrame = utilFrame;

		this.targetRoot = root;

		setupHandle(rootCenter);
		setupHandle(rootWest);
		setupHandle(rootNorth);
		setupHandle(rootEast);
		setupHandle(rootSouth);

		setupHandle(dockableCenter);
		setupHandle(dockableWest);
		setupHandle(dockableNorth);
		setupHandle(dockableEast);
		setupHandle(dockableSouth);
	}

	public DockingRegion getDockableRegion() {
		return dockableRegion;
	}

	public DockingRegion getRootRegion() {
		return rootRegion;
	}

	public void setActive(boolean active) {
		utilFrame.setVisible(active);
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	private void setupHandle(DockingHandle label) {
		mouseOver.put(label, false);
		utilFrame.add(label);
	}

	private void setRootHandleLocations() {
		rootCenter.setVisible(targetRoot != null && targetRoot.getPanel() == null);
		rootWest.setVisible(targetRoot != null && targetRoot.getPanel() != null && isRegionAllowed(DockingRegion.WEST));
		rootNorth.setVisible(targetRoot != null && targetRoot.getPanel() != null && isRegionAllowed(DockingRegion.NORTH));
		rootEast.setVisible(targetRoot != null && targetRoot.getPanel() != null && isRegionAllowed(DockingRegion.EAST));
		rootSouth.setVisible(targetRoot != null && targetRoot.getPanel() != null && isRegionAllowed(DockingRegion.SOUTH));

		if (targetRoot != null) {
			Point location = targetRoot.getLocation();
			Dimension size = targetRoot.getSize();
			location.x += size.width / 2;
			location.y += size.height / 2;

			SwingUtilities.convertPointToScreen(location, targetRoot.getParent());
			SwingUtilities.convertPointFromScreen(location, utilFrame);

			setLocation(rootCenter, location.x, location.y);
			setLocation(rootWest, location.x - (size.width / 2) + rootHandleSpacing(rootWest), location.y);
			setLocation(rootNorth, location.x, location.y - (size.height / 2) + rootHandleSpacing(rootNorth));
			setLocation(rootEast, location.x + (size.width / 2) - rootHandleSpacing(rootEast), location.y);
			setLocation(rootSouth, location.x, location.y + (size.height / 2) - rootHandleSpacing(rootSouth));
		}
	}

	private int handleSpacing(JLabel handle) {
		return handle.getWidth() + 8;
	}

	private int rootHandleSpacing(JLabel handle) {
		return handle.getWidth() + 16;
	}

	// set the specific Dockable target which we'll show a basic handle in the center of
	public void setTarget(Dockable dockable) {
		if (dockable == targetDockable) {
			return;
		}

		targetDockable = dockable;

		dockableCenter.setVisible(false);
		dockableWest.setVisible(false);
		dockableNorth.setVisible(false);
		dockableEast.setVisible(false);
		dockableSouth.setVisible(false);
	}

	private boolean isRegionAllowed(DockingRegion region) {
		return !floating.disallowedRegions().contains(region);
	}

	private void setDockableHandleLocations() {
		dockableCenter.setVisible(targetDockable != null);
		dockableWest.setVisible(targetDockable != null && isRegionAllowed(DockingRegion.WEST));
		dockableNorth.setVisible(targetDockable != null && isRegionAllowed(DockingRegion.NORTH));
		dockableEast.setVisible(targetDockable != null && isRegionAllowed(DockingRegion.EAST));
		dockableSouth.setVisible(targetDockable != null && isRegionAllowed(DockingRegion.SOUTH));

		if (targetDockable != null && ((Component) targetDockable).getParent() != null) {
			Point location = ((Component) targetDockable).getLocation();
			Dimension size = ((Component) targetDockable).getSize();

			location.x += size.width / 2;
			location.y += size.height / 2;

			SwingUtilities.convertPointToScreen(location, ((Component) targetDockable).getParent());

			SwingUtilities.convertPointFromScreen(location, utilFrame);
			setLocation(dockableCenter, location.x, location.y);
			setLocation(dockableWest, location.x - handleSpacing(dockableWest), location.y);
			setLocation(dockableNorth, location.x, location.y - handleSpacing(dockableNorth));
			setLocation(dockableEast, location.x + handleSpacing(dockableEast), location.y);
			setLocation(dockableSouth, location.x, location.y + handleSpacing(dockableSouth));
		}
	}

	public void update(Point screenPos) {
		JComponent component = targetRoot;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component.getParent());

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		SwingUtilities.convertPointToScreen(point, component.getParent());

		utilFrame.setLocation(point);
		utilFrame.setSize(size);

		setRootHandleLocations();
		setDockableHandleLocations();

		framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

		rootRegion = null;
		dockableRegion = null;

		for (DockingHandle handle : mouseOver.keySet()) {
			boolean over = handle.isVisible() && handle.getBounds().contains(framePoint);

			mouseOver.put(handle, over);

			if (over) {
				if (handle.isRoot()) {
					rootRegion = handle.getRegion();
				}
				else {
					dockableRegion = handle.getRegion();
				}
			}
		}

		utilFrame.revalidate();
		utilFrame.repaint();
	}

	private void setLocation(Component component, int x, int y) {
		component.setLocation(x - (HANDLE_ICON_SIZE / 2), y - (HANDLE_ICON_SIZE / 2));
	}

	public void paint(Graphics g) {
		int centerX = dockableCenter.getX() + (dockableCenter.getWidth() / 2);
		int centerY = dockableCenter.getY() + (dockableCenter.getWidth() / 2);

		int spacing = handleSpacing(dockableCenter) - dockableCenter.getWidth();
		int half_icon = dockableCenter.getWidth() / 2;
		int one_and_a_half_icons = (int) (dockableCenter.getWidth() * 1.5);

		// create a polygon of the docking handles background
		Polygon poly = new Polygon(
				new int[] {
						centerX - half_icon - spacing,
						centerX + half_icon + spacing,
						centerX + half_icon + spacing,
						centerX + half_icon + (spacing * 2),
						centerX + one_and_a_half_icons + (spacing * 2),
						centerX + one_and_a_half_icons + (spacing * 2),
						centerX + half_icon + (spacing * 2),
						centerX + half_icon + spacing,
						centerX + half_icon + spacing,
						centerX - half_icon - spacing,
						centerX - half_icon - spacing,
						centerX - half_icon - (spacing * 2),
						centerX - one_and_a_half_icons - (spacing * 2),
						centerX - one_and_a_half_icons - (spacing * 2),
						centerX - half_icon - (spacing * 2),
						centerX - half_icon - spacing,
						centerX - half_icon - spacing
				},
				new int[] {
						centerY - one_and_a_half_icons - (spacing * 2),
						centerY - one_and_a_half_icons - (spacing * 2),
						centerY - half_icon - (spacing * 2),
						centerY - half_icon - spacing,
						centerY - half_icon - spacing,
						centerY + half_icon + spacing,
						centerY + half_icon + spacing,
						centerY + half_icon + (spacing * 2),
						centerY + one_and_a_half_icons + (spacing * 2),
						centerY + one_and_a_half_icons + (spacing * 2),
						centerY + half_icon + (spacing * 2),
						centerY + half_icon + spacing,
						centerY + half_icon + spacing,
						centerY - half_icon - spacing,
						centerY - half_icon - spacing,
						centerY - half_icon - (spacing * 2),
						centerY - one_and_a_half_icons - (spacing * 2),
				},
				17
		);

		Color background = DockingColors.getHandlesBackground();
		Color border = DockingColors.getHandlesBackgroundBorder();

		Graphics2D g2 = (Graphics2D) g.create();
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
		g2.setStroke(dashed);

		// draw root handles
		paintHandle(g, g2, rootCenter);
		paintHandle(g, g2, rootEast);
		paintHandle(g, g2, rootWest);
		paintHandle(g, g2, rootNorth);
		paintHandle(g, g2, rootSouth);

		// draw the dockable handles background over the root handles in case they overlap
		if (targetDockable != null) {
			// fill the dockable handles background
			g.setColor(background);
			g.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);

			// draw the dockable handles border
			g.setColor(border);
			g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
		}

		// draw the docking handles over the docking handles background
		paintHandle(g, g2, dockableCenter);
		paintHandle(g, g2, dockableEast);
		paintHandle(g, g2, dockableWest);
		paintHandle(g, g2, dockableNorth);
		paintHandle(g, g2, dockableSouth);

		g2.dispose();
	}

	private void paintHandle(Graphics g, Graphics2D g2, DockingHandle handle) {
		if (handle.isVisible()) {
			handle.paintHandle(g, g2, mouseOver.get(handle));
		}
	}
}
