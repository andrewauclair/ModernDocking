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
import java.util.HashMap;
import java.util.Map;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandlesFrame extends JFrame implements MouseMotionListener, MouseListener {
	private static final double ROOT_HANDLE_EDGE_DISTANCE = 0.85;
	private static final int HANDLE_ICON_SIZE = 32;

	private static final Color HANDLE_COLOR_NOT_SELECTED = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 30);
	private static final Color HANDLE_COLOR_SELECTED = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 50);

	private final JFrame frame;

	private Dockable floating;
	private Dockable targetDockable;
	private final RootDockingPanel targetRoot;

	// TODO turn these into icons
	private final JLabel rootCenter = new JLabel("RC", SwingConstants.CENTER);
	private final JLabel rootWest = new JLabel("RW", SwingConstants.CENTER);
	private final JLabel rootNorth = new JLabel("RN", SwingConstants.CENTER);
	private final JLabel rootEast = new JLabel("RE", SwingConstants.CENTER);
	private final JLabel rootSouth = new JLabel("RS", SwingConstants.CENTER);

	private final JLabel dockableCenter = new JLabel("DC", SwingConstants.CENTER);
	private final JLabel dockableWest = new JLabel("DW", SwingConstants.CENTER);
	private final JLabel dockableNorth = new JLabel("DN", SwingConstants.CENTER);
	private final JLabel dockableEast = new JLabel("DE", SwingConstants.CENTER);
	private final JLabel dockableSouth = new JLabel("DS", SwingConstants.CENTER);

	private final Map<JLabel, DockingRegion> rootRegions = new HashMap<>();
	private final Map<JLabel, DockingRegion> dockableRegions = new HashMap<>();

	private DockingRegion rootRegion = null;
	private DockingRegion dockableRegion = null;

	public DockingHandlesFrame(JFrame frame, RootDockingPanel root) {
		setLayout(null);
		setType(Type.UTILITY);

		this.frame = frame;
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

		setBackground(new Color(0, 0, 0, 0));
	}

	public void setActive(boolean active) {
		setVisible(active);
	}

	private void setupRootLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);

		rootRegions.put(label, region);

		add(label);
	}

	private void setupDockableLabel(JLabel label, DockingRegion region) {
		label.addMouseListener(this);
		label.addMouseMotionListener(this);

		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);

		dockableRegions.put(label, region);

		add(label);
	}

	private void setRootHandleLocations() {
		rootCenter.setVisible(targetRoot != null && targetRoot.getPanel() == null);
		rootWest.setVisible(targetRoot != null && targetRoot.getPanel() != null);
		rootNorth.setVisible(targetRoot != null && targetRoot.getPanel() != null);
		rootEast.setVisible(targetRoot != null && targetRoot.getPanel() != null);
		rootSouth.setVisible(targetRoot != null && targetRoot.getPanel() != null);

		if (targetRoot != null) {
			Point location = targetRoot.getLocation();
			Dimension size = targetRoot.getSize();
			location.x += size.width / 2;
			location.y += size.height / 2;

			SwingUtilities.convertPointToScreen(location, targetRoot.getParent());
			SwingUtilities.convertPointFromScreen(location, this);

			setLocation(rootCenter, location.x, location.y);
			setLocation(rootWest, (int) (location.x - (size.width / 2 * ROOT_HANDLE_EDGE_DISTANCE)), location.y);
			setLocation(rootNorth, location.x, (int) (location.y - (size.height / 2 * ROOT_HANDLE_EDGE_DISTANCE)));
			setLocation(rootEast, (int) (location.x + (size.width / 2 * ROOT_HANDLE_EDGE_DISTANCE)), location.y);
			setLocation(rootSouth, location.x, (int) (location.y + (size.height / 2 * ROOT_HANDLE_EDGE_DISTANCE)));
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

	private void setDockableHandleLocations() {
		dockableCenter.setVisible(targetDockable != null);
		dockableWest.setVisible(targetDockable != null);
		dockableNorth.setVisible(targetDockable != null);
		dockableEast.setVisible(targetDockable != null);
		dockableSouth.setVisible(targetDockable != null);

		if (targetDockable != null) {
			Point location = ((Component) targetDockable).getLocation();
			Dimension size = ((Component) targetDockable).getSize();
			location.x += size.width / 2;
			location.y += size.height / 2;

			SwingUtilities.convertPointToScreen(location, ((Component) targetDockable).getParent());

			SwingUtilities.convertPointFromScreen(location, this);
			setLocation(dockableCenter, location.x, location.y);
			setLocation(dockableWest, location.x - HANDLE_ICON_SIZE, location.y);
			setLocation(dockableNorth, location.x, location.y - HANDLE_ICON_SIZE);
			setLocation(dockableEast, location.x + HANDLE_ICON_SIZE, location.y);
			setLocation(dockableSouth, location.x, location.y + HANDLE_ICON_SIZE);
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
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
			}
		}

		dockableRegion = null;

		for (JLabel label : dockableRegions.keySet()) {
			if (label.isVisible() && label.getBounds().contains(framePoint)) {
				dockableRegion = dockableRegions.get(label);
				label.setBackground(HANDLE_COLOR_SELECTED);
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
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
}
