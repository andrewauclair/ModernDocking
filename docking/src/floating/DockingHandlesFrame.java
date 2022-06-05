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
import java.util.HashMap;
import java.util.Map;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandlesFrame extends JFrame {
	private static final double ROOT_HANDLE_EDGE_DISTANCE = 0.85;
	private static final int HANDLE_ICON_SIZE = 32;

	private static final Color HANDLE_COLOR_NOT_SELECTED = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 30);
	private static final Color HANDLE_COLOR_SELECTED = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 50);

	private Dockable targetDockable;
	private RootDockingPanel targetRoot;

	// TODO turn these into icons
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

	public DockingHandlesFrame() {
		setLayout(null);

		setUndecorated(true);

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

	private void setupRootLabel(JLabel label, DockingRegion region) {
		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);

		rootRegions.put(label, region);

		add(label);
	}

	private void setupDockableLabel(JLabel label, DockingRegion region) {
		label.setVisible(false);

		label.setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		label.setOpaque(true);
		label.setBackground(HANDLE_COLOR_NOT_SELECTED);

		dockableRegions.put(label, region);

		add(label);
	}

	public void dockingComplete() {
		targetDockable = null;
		targetRoot = null;
		rootRegion = null;
		dockableRegion = null;

		for (JLabel label : rootRegions.keySet()) {
			label.setVisible(false);
		}

		for (JLabel label : dockableRegions.keySet()) {
			label.setVisible(false);
		}
	}

	// set the root of the target frame. Allows the user to always dock to the outer edges of the frame
	public void setRoot(JFrame frame, RootDockingPanel root) {
		targetRoot = root;

		rootWest.setVisible(targetRoot != null);
		rootNorth.setVisible(targetRoot != null);
		rootEast.setVisible(targetRoot != null);
		rootSouth.setVisible(targetRoot != null);

		if (targetRoot != null && targetDockable != null) {
			Point location = ((Component) targetDockable).getLocation();
			Rectangle bounds = targetRoot.getBounds();
			location.x += bounds.width / 2;
			location.y += bounds.height / 2;

			setLocation(rootWest, (int) (location.x - (bounds.width / 2 * ROOT_HANDLE_EDGE_DISTANCE)), location.y);
			setLocation(rootNorth, location.x, (int) (location.y - (bounds.height / 2 * ROOT_HANDLE_EDGE_DISTANCE)));
			setLocation(rootEast, (int) (location.x + (bounds.width / 2 * ROOT_HANDLE_EDGE_DISTANCE)), location.y);
			setLocation(rootSouth, location.x, (int) (location.y + (bounds.height / 2 * ROOT_HANDLE_EDGE_DISTANCE)));
		}
	}

	// set the specific Dockable target which we'll show a basic handle in the center of
	public void setTarget(Dockable dockable) {
		targetDockable = dockable;

		dockableCenter.setVisible(targetDockable != null);
		dockableWest.setVisible(targetDockable != null);
		dockableNorth.setVisible(targetDockable != null);
		dockableEast.setVisible(targetDockable != null);
		dockableSouth.setVisible(targetDockable != null);

		if (targetDockable != null) {
			Point location = ((Component) targetDockable).getLocation();
			Rectangle bounds = ((Component) targetDockable).getBounds();
			location.x += bounds.width / 2;
			location.y += bounds.height / 2;

			SwingUtilities.convertPointToScreen(location, (Component) targetDockable);

			SwingUtilities.convertPointFromScreen(location, this);
			setLocation(dockableCenter, location.x, location.y);
			setLocation(dockableWest, location.x - HANDLE_ICON_SIZE, location.y);
			setLocation(dockableNorth, location.x, location.y - HANDLE_ICON_SIZE);
			setLocation(dockableEast, location.x + HANDLE_ICON_SIZE, location.y);
			setLocation(dockableSouth, location.x, location.y + HANDLE_ICON_SIZE);
		}
	}

	public void update(Point screenPos) {
		if (targetRoot == null) {
			return;
		}

		JComponent component = targetRoot;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component);

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		SwingUtilities.convertPointToScreen(point, component);

		setLocation(point);
		setSize(size);

		framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, this);

		rootRegion = null;

		for (JLabel label : rootRegions.keySet()) {
			if (label.getBounds().contains(framePoint)) {
				rootRegion = rootRegions.get(label);
				label.setBackground(HANDLE_COLOR_SELECTED);
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
			}
		}

		dockableRegion = null;

		for (JLabel label : dockableRegions.keySet()) {
			if (label.getBounds().contains(framePoint)) {
				dockableRegion = dockableRegions.get(label);
				label.setBackground(HANDLE_COLOR_SELECTED);
			}
			else {
				label.setBackground(HANDLE_COLOR_NOT_SELECTED);
			}
		}

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
}
