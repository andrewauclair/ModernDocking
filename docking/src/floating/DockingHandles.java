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
import docking.DockingColors;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import static floating.DockingHandle.HANDLE_ICON_SIZE;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandles implements MouseMotionListener, MouseListener {
	public int handleSpacing(JLabel handle) {
		return handle.getWidth() + 8;
	}

	public int rootHandleSpacing(JLabel handle) {
		return handle.getWidth() + 16;
	}

	private Dockable floating;
	private Dockable targetDockable;
	private final RootDockingPanel targetRoot;

	private final JLabel rootCenter = new DockingHandle();
	private final JLabel rootWest = new DockingHandle();
	private final JLabel rootNorth = new DockingHandle();
	private final JLabel rootEast = new DockingHandle();
	private final JLabel rootSouth = new DockingHandle();

	private final JLabel dockableCenter = new DockingHandle();
	private final JLabel dockableWest = new DockingHandle();
	private final JLabel dockableNorth = new DockingHandle();
	private final JLabel dockableEast = new DockingHandle();
	private final JLabel dockableSouth = new DockingHandle();

	private final Map<JLabel, DockingRegion> rootRegions = new HashMap<>();
	private final Map<JLabel, DockingRegion> dockableRegions = new HashMap<>();
	private final Map<JLabel, Boolean> rootMouseOver = new HashMap<>();
	private final Map<JLabel, Boolean> dockableMouseOver = new HashMap<>();

	private DockingRegion rootRegion = null;
	private DockingRegion dockableRegion = null;
	private final JFrame utilFrame;

	public DockingHandles(JFrame utilFrame, RootDockingPanel root) {
		this.utilFrame = utilFrame;

		this.targetRoot = root;

		utilFrame.addMouseListener(this);
		utilFrame.addMouseMotionListener(this);

		setupRootLabel(rootCenter, DockingRegion.CENTER);
		setupRootLabel(rootWest, DockingRegion.WEST);
		setupRootLabel(rootNorth, DockingRegion.NORTH);
		setupRootLabel(rootEast, DockingRegion.EAST);
		setupRootLabel(rootSouth, DockingRegion.SOUTH);

		setupDockableLabel(dockableCenter, DockingRegion.CENTER);
		setupDockableLabel(dockableWest, DockingRegion.WEST);
		setupDockableLabel(dockableNorth, DockingRegion.NORTH);
		setupDockableLabel(dockableEast, DockingRegion.EAST);
		setupDockableLabel(dockableSouth, DockingRegion.SOUTH);
	}

	public void setActive(boolean active) {
		utilFrame.setVisible(active);
	}

	private void setupRootLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);

		rootRegions.put(label, region);
		rootMouseOver.put(label, false);

		utilFrame.add(label);
	}

	private void setupDockableLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);

		dockableRegions.put(label, region);
		dockableMouseOver.put(label, false);

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

		if (targetDockable != null) {
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

		for (JLabel label : rootRegions.keySet()) {
			if (label.isVisible() && label.getBounds().contains(framePoint)) {
				rootRegion = rootRegions.get(label);
				rootMouseOver.put(label, true);
			}
			else {
				rootMouseOver.put(label, false);
			}
		}

		dockableRegion = null;

		for (JLabel label : dockableRegions.keySet()) {
			if (label.isVisible() && label.getBounds().contains(framePoint)) {
				dockableRegion = dockableRegions.get(label);
				dockableMouseOver.put(label, true);
			}
			else {
				dockableMouseOver.put(label, false);
			}
		}

		utilFrame.revalidate();
		utilFrame.repaint();
	}

	public DockingRegion getDockableRegion() {
		return dockableRegion;
	}

	public DockingRegion getRootRegion() {
		return rootRegion;
	}

	private void setLocation(Component component, int x, int y) {
		component.setLocation(x - (HANDLE_ICON_SIZE / 2), y - (HANDLE_ICON_SIZE / 2));
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	// we don't want to use the mouse events in this overlay frame because that would break the app
	// pass them off to the component that we really need them in, the drag source
	private void dispatchEvent(MouseEvent e) {
		if (floating != null && floating.dragSource() != null) {
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

	public void paint(Graphics g) {
//		Rectangle bounds = g.getClipBounds();

		int centerX = dockableCenter.getX() + (dockableCenter.getWidth() / 2);
		int centerY = dockableCenter.getY() + (dockableCenter.getWidth() / 2);

		int spacing = handleSpacing(dockableCenter) - dockableCenter.getWidth();
		int half_icon = dockableCenter.getWidth() / 2;
		int one_and_a_half_icons = (int) (dockableCenter.getWidth() * 1.5);

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
		Color outline = DockingColors.getHandlesOutline();

		Color hover = DockingColors.getHandlesFill();

		Graphics2D g2 = (Graphics2D) g.create();
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				0, new float[]{3}, 0);
		g2.setStroke(dashed);

		// draw the root handles
		if (rootCenter.isVisible()) {
			spacing = handleSpacing(rootCenter) - rootCenter.getWidth();

			g.setColor(background);
			g.fillRect(rootCenter.getX() - spacing, rootCenter.getY() - spacing, rootCenter.getWidth() + (spacing * 2), rootCenter.getWidth() + (spacing * 2));

			Rectangle bounds = rootCenter.getBounds();

			if (rootMouseOver.get(rootCenter)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}

			g.setColor(border);
			g.drawRect(rootCenter.getX() - spacing, rootCenter.getY() - spacing, rootCenter.getWidth() + (spacing * 2), rootCenter.getWidth() + (spacing * 2));

			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		if (rootEast.isVisible()) {
			spacing = handleSpacing(rootEast) - rootEast.getWidth();

			g.setColor(background);
			g.fillRect(rootEast.getX() - spacing, rootEast.getY() - spacing, rootEast.getWidth() + (spacing * 2), rootEast.getWidth() + (spacing * 2));

			Rectangle bounds = rootEast.getBounds();

			if (rootMouseOver.get(rootEast)) {
				g.setColor(hover);
				g.fillRect(bounds.x + (bounds.width / 2), bounds.y, bounds.width / 2, bounds.height);
			}

			g.setColor(border);
			g.drawRect(rootEast.getX() - spacing, rootEast.getY() - spacing, rootEast.getWidth() + (spacing * 2), rootEast.getWidth() + (spacing * 2));

			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x + halfWidth, bounds.y, bounds.width - halfWidth, bounds.height);
		}

		if (rootWest.isVisible()) {
			spacing = handleSpacing(rootWest) - rootWest.getWidth();

			g.setColor(background);
			g.fillRect(rootWest.getX() - spacing, rootWest.getY() - spacing, rootWest.getWidth() + (spacing * 2), rootWest.getWidth() + (spacing * 2));

			Rectangle bounds = rootWest.getBounds();

			if (rootMouseOver.get(rootWest)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width / 2, bounds.height);
			}

			g.setColor(border);
			g.drawRect(rootWest.getX() - spacing, rootWest.getY() - spacing, rootWest.getWidth() + (spacing * 2), rootWest.getWidth() + (spacing * 2));

			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x, bounds.y, bounds.width - halfWidth, bounds.height);
		}

		if (rootNorth.isVisible()) {
			spacing = handleSpacing(rootNorth) - rootNorth.getWidth();

			g.setColor(background);
			g.fillRect(rootNorth.getX() - spacing, rootNorth.getY() - spacing, rootNorth.getWidth() + (spacing * 2), rootNorth.getWidth() + (spacing * 2));

			Rectangle bounds = rootNorth.getBounds();

			if (rootMouseOver.get(rootNorth)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.width / 2);
			}

			g.setColor(border);
			g.drawRect(rootNorth.getX() - spacing, rootNorth.getY() - spacing, rootNorth.getWidth() + (spacing * 2), rootNorth.getWidth() + (spacing * 2));

			g.setColor(outline);

			int halfWidth = bounds.width / 2;

			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height - halfWidth);
		}

		if (rootSouth.isVisible()) {
			spacing = handleSpacing(rootSouth) - rootSouth.getWidth();

			g.setColor(background);
			g.fillRect(rootSouth.getX() - spacing, rootSouth.getY() - spacing, rootSouth.getWidth() + (spacing * 2), rootSouth.getWidth() + (spacing * 2));

			Rectangle bounds = rootSouth.getBounds();

			if (rootMouseOver.get(rootSouth)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y + (bounds.width / 2), bounds.width, bounds.width / 2);
			}

			g.setColor(border);
			g.drawRect(rootSouth.getX() - spacing, rootSouth.getY() - spacing, rootSouth.getWidth() + (spacing * 2), rootSouth.getWidth() + (spacing * 2));

			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x, bounds.y + halfWidth, bounds.width, bounds.height - halfWidth);
		}

		if (targetDockable != null) {
			g.setColor(background);
			// dockable handles background
			g.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);

			g.setColor(border);
			g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
		}

		// draw the dockable handles
		if (dockableCenter.isVisible()) {
			Rectangle bounds = dockableCenter.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			if (dockableMouseOver.get(dockableCenter)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}

		if (dockableEast.isVisible()) {
			Rectangle bounds = dockableEast.getBounds();

			if (dockableMouseOver.get(dockableEast)) {
				g.setColor(hover);
				g.fillRect(bounds.x + (bounds.width / 2), bounds.y, bounds.width / 2, bounds.height);
			}

			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x + halfWidth, bounds.y, bounds.x + halfWidth, bounds.y + bounds.height);
		}

		if (dockableWest.isVisible()) {
			Rectangle bounds = dockableWest.getBounds();

			if (dockableMouseOver.get(dockableWest)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width / 2, bounds.height);
			}

			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x + halfWidth, bounds.y, bounds.x + halfWidth, bounds.y + bounds.height);
		}

		if (dockableNorth.isVisible()) {
			Rectangle bounds = dockableNorth.getBounds();

			if (dockableMouseOver.get(dockableNorth)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.width / 2);
			}

			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x, bounds.y + halfWidth, bounds.x + bounds.width, bounds.y + halfWidth);
		}

		if (dockableSouth.isVisible()) {
			Rectangle bounds = dockableSouth.getBounds();

			if (dockableMouseOver.get(dockableSouth)) {
				g.setColor(hover);
				g.fillRect(bounds.x, bounds.y + (bounds.width / 2), bounds.width, bounds.width / 2);
			}

			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x, bounds.y + halfWidth, bounds.x + bounds.width, bounds.y + halfWidth);
		}

		g2.dispose();
	}
}
