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
import docking.DockingIcons;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandlesFrame extends JFrame implements MouseMotionListener, MouseListener {
//	public static final double ROOT_HANDLE_EDGE_DISTANCE = 0.85;
	public static final int HANDLE_ICON_SIZE = 32;
	public static final int HANDLE_SPACING = HANDLE_ICON_SIZE + 8;
	public static final int ROOT_HANDLE_SPACING = HANDLE_ICON_SIZE + 16;

	private static final Color HANDLE_COLOR_NOT_SELECTED = new Color(0, 0, 0, 30);//Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 30);
	private static final Color HANDLE_COLOR_SELECTED = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 50);

	private Dockable floating;
	private Dockable targetDockable;
	private final RootDockingPanel targetRoot;

	private final JLabel rootCenter = new JLabel();
	private final JLabel rootWest = new JLabel();
	private final JLabel rootNorth = new JLabel();
	private final JLabel rootEast = new JLabel();
	private final JLabel rootSouth = new JLabel();

	private final JLabel dockableCenter = new JLabel();
	private final JLabel dockableWest = new JLabel();
	private final JLabel dockableNorth = new JLabel();
	private final JLabel dockableEast = new JLabel();
	private final JLabel dockableSouth = new JLabel();

	private boolean mouseOverDockableCenter = false;

	private final Map<JLabel, DockingRegion> rootRegions = new HashMap<>();
	private final Map<JLabel, DockingRegion> dockableRegions = new HashMap<>();
	private final Map<JLabel, Boolean> rootMouseOver = new HashMap<>();
	private final Map<JLabel, Boolean> dockableMouseOver = new HashMap<>();

	private DockingRegion rootRegion = null;
	private DockingRegion dockableRegion = null;

	public DockingHandlesFrame(RootDockingPanel root) {
		setLayout(null);
		setType(Type.UTILITY);

		this.targetRoot = root;

		setUndecorated(true);

		addMouseListener(this);
		addMouseMotionListener(this);

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

		rootCenter.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-center.png")));
		rootWest.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-west.png")));
		rootNorth.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-north.png")));
		rootEast.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-east.png")));
		rootSouth.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-south.png")));

		dockableCenter.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-center.png")));
		dockableWest.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-west.png")));
//		dockableNorth.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-north.png")));
		dockableNorth.setIcon(DockingIcons.handleNorth());
		dockableEast.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-east.png")));
		dockableSouth.setIcon(new ImageIcon(DockingHandlesFrame.class.getResource("/icons/dock-south.png")));

		setBackground(new Color(0, 0, 0, 0));
	}

	public void setActive(boolean active) {
		setVisible(active);
	}

	private void setupRootLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setText(null);
		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);
		label.setBorder(null);

		rootRegions.put(label, region);
		rootMouseOver.put(label, false);

		add(label);
	}

	private void setupDockableLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);
		label.setBorder(null);

		dockableRegions.put(label, region);
		dockableMouseOver.put(label, false);

		add(label);
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
			SwingUtilities.convertPointFromScreen(location, this);

			setLocation(rootCenter, location.x, location.y);
			setLocation(rootWest, (int) (location.x - (size.width / 2) + ROOT_HANDLE_SPACING), location.y);
			setLocation(rootNorth, location.x, (int) (location.y - (size.height / 2) + ROOT_HANDLE_SPACING));
			setLocation(rootEast, (int) (location.x + (size.width / 2) - ROOT_HANDLE_SPACING), location.y);
			setLocation(rootSouth, location.x, (int) (location.y + (size.height / 2) - ROOT_HANDLE_SPACING));
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

			int spacing = HANDLE_SPACING;// HANDLE_ICON_SIZE + 4;

			SwingUtilities.convertPointFromScreen(location, this);
			setLocation(dockableCenter, location.x, location.y);
			setLocation(dockableWest, location.x - spacing, location.y);
			setLocation(dockableNorth, location.x, location.y - spacing);
			setLocation(dockableEast, location.x + spacing, location.y);
			setLocation(dockableSouth, location.x, location.y + spacing);
		}
	}

	public void update(Point screenPos) {
		JComponent component = targetRoot;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component.getParent());

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		SwingUtilities.convertPointToScreen(point, component.getParent());

		setLocation(point);
		setSize(size);

		setRootHandleLocations();
		setDockableHandleLocations();

		framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, this);

		rootRegion = null;

		for (JLabel label : rootRegions.keySet()) {
			if (label.isVisible() && label.getBounds().contains(framePoint)) {
				rootRegion = rootRegions.get(label);
				label.setBackground(HANDLE_COLOR_SELECTED);
				rootMouseOver.put(label, true);
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
				rootMouseOver.put(label, false);
			}
		}

		dockableRegion = null;

		for (JLabel label : dockableRegions.keySet()) {
			if (label.isVisible() && label.getBounds().contains(framePoint)) {
				dockableRegion = dockableRegions.get(label);
				label.setBackground(HANDLE_COLOR_SELECTED);
				dockableMouseOver.put(label, true);
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
				dockableMouseOver.put(label, false);
			}
		}

		revalidate();
		repaint();
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

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Rectangle bounds = g.getClipBounds();

		int centerX = bounds.x + (bounds.width / 2);
		int centerY = bounds.y + (bounds.height / 2);

		int spacing = HANDLE_SPACING - HANDLE_ICON_SIZE;
		int half_icon = HANDLE_ICON_SIZE / 2;
		int one_and_a_half_icons = (int) (HANDLE_ICON_SIZE * 1.5);

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

		Color background = UIManager.getColor("Panel.background");

		g.setColor(background);
		// dockable handles background
		g.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);

		// root north background


		// root east background
		g.setColor(background);
		g.fillRect(rootEast.getX() - spacing, rootEast.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		// root south background
		g.setColor(background);
		g.fillRect(rootSouth.getX() - spacing, rootSouth.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		// root west background
		g.setColor(background);
		g.fillRect(rootWest.getX() - spacing, rootWest.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		Color border = UIManager.getColor("Component.borderColor");
		g.setColor(border);
		g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);

		// root north border


		// root east border
		g.setColor(border);
		g.drawRect(rootEast.getX() - spacing, rootEast.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		// root south border
		g.setColor(border);
		g.drawRect(rootSouth.getX() - spacing, rootSouth.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		// root west border
		g.setColor(border);
		g.drawRect(rootWest.getX() - spacing, rootWest.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

		Color outline = UIManager.getColor("Button.foreground");
		g.setColor(outline);

		Color hover = UIManager.getColor("Button.default.borderColor");

		Graphics2D g2 = (Graphics2D) g.create();
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				0, new float[]{3}, 0);
		g2.setStroke(dashed);

		// draw the root handles
		if (rootCenter.isVisible()) {
			bounds = rootCenter.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			if (rootMouseOver.get(rootCenter)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, bounds.width - 1, bounds.height - 1);
			}
		}

		if (rootEast.isVisible()) {
			bounds = rootEast.getBounds();
			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x + halfWidth, bounds.y, bounds.width - halfWidth, bounds.height);

			if (rootMouseOver.get(rootEast)) {
				g.setColor(hover);
				g.fillRect(bounds.x + halfWidth + 1, bounds.y + 1, halfWidth - 1, bounds.height - 1);
			}
		}

		if (rootWest.isVisible()) {
			bounds = rootWest.getBounds();
			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x, bounds.y, bounds.width - halfWidth, bounds.height);

			if (rootMouseOver.get(rootWest)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, halfWidth - 1, bounds.height - 1);
			}
		}

		if (rootNorth.isVisible()) {
			g.setColor(background);
			g.fillRect(rootNorth.getX() - spacing, rootNorth.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

			g.setColor(border);
			g.drawRect(rootNorth.getX() - spacing, rootNorth.getY() - spacing, HANDLE_ICON_SIZE + (spacing * 2), HANDLE_ICON_SIZE + (spacing * 2));

			bounds = rootNorth.getBounds();

			g.setColor(outline);

			int halfWidth = bounds.width / 2;

			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height - halfWidth);

			if (rootMouseOver.get(rootNorth)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, bounds.width - 1, halfWidth - 1);
			}
		}

		if (rootSouth.isVisible()) {
			bounds = rootSouth.getBounds();
			g.setColor(outline);
			int halfWidth = bounds.width / 2;
			g.drawRect(bounds.x, bounds.y + halfWidth, bounds.width, bounds.height - halfWidth);

			if (rootMouseOver.get(rootSouth)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + halfWidth + 1, bounds.width - 1, halfWidth - 1);
			}
		}

		// draw the dockable handles
		if (dockableCenter.isVisible()) {
			bounds = dockableCenter.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			if (dockableMouseOver.get(dockableCenter)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, bounds.width - 1, bounds.height - 1);
			}
		}

		if (dockableEast.isVisible()) {
			bounds = dockableEast.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x + halfWidth, bounds.y, bounds.x + halfWidth, bounds.y + bounds.height);

			if (dockableMouseOver.get(dockableEast)) {
				g.setColor(hover);
				g.fillRect(bounds.x + halfWidth + 1, bounds.y + 1, halfWidth - 1, bounds.height - 1);
			}
		}

		if (dockableWest.isVisible()) {
			bounds = dockableWest.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x + halfWidth, bounds.y, bounds.x + halfWidth, bounds.y + bounds.height);

			if (dockableMouseOver.get(dockableWest)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, halfWidth - 1, bounds.height - 1);
			}
		}

		if (dockableNorth.isVisible()) {
			bounds = dockableNorth.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x, bounds.y + halfWidth, bounds.x + bounds.width, bounds.y + halfWidth);

			if (dockableMouseOver.get(dockableNorth)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + 1, bounds.width - 1, halfWidth - 1);
			}
		}

		if (dockableSouth.isVisible()) {
			bounds = dockableSouth.getBounds();
			g.setColor(outline);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int halfWidth = bounds.width / 2;

			g2.drawLine(bounds.x, bounds.y + halfWidth, bounds.x + bounds.width, bounds.y + halfWidth);

			if (dockableMouseOver.get(dockableSouth)) {
				g.setColor(hover);
				g.fillRect(bounds.x + 1, bounds.y + halfWidth + 1, bounds.width - 1, halfWidth - 1);
			}
		}

		g2.dispose();
	}
}
