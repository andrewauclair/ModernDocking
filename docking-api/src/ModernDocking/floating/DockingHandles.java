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
package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.DockableStyle;
import ModernDocking.DockingRegion;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.*;
import ModernDocking.ui.DockingSettings;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static ModernDocking.floating.DockingHandle.HANDLE_ICON_SIZE;

/**
 * handles displaying the handles for docking overlaid on the application
 * only displayed over the currently hit docking panel
 */
public class DockingHandles {
	private final DockingHandle rootCenter = new DockingHandle(DockingRegion.CENTER, true);
	private final DockingHandle rootWest = new DockingHandle(DockingRegion.WEST, true);
	private final DockingHandle rootNorth = new DockingHandle(DockingRegion.NORTH, true);
	private final DockingHandle rootEast = new DockingHandle(DockingRegion.EAST, true);
	private final DockingHandle rootSouth = new DockingHandle(DockingRegion.SOUTH, true);

	private final DockingHandle pinWest = new DockingHandle(DockingRegion.WEST);
	private final DockingHandle pinEast = new DockingHandle(DockingRegion.EAST);
	private final DockingHandle pinSouth = new DockingHandle(DockingRegion.SOUTH);

	private final DockingHandle dockableCenter = new DockingHandle(DockingRegion.CENTER, false);
	private final DockingHandle dockableWest = new DockingHandle(DockingRegion.WEST, false);
	private final DockingHandle dockableNorth = new DockingHandle(DockingRegion.NORTH, false);
	private final DockingHandle dockableEast = new DockingHandle(DockingRegion.EAST, false);
	private final DockingHandle dockableSouth = new DockingHandle(DockingRegion.SOUTH, false);

	private final Map<DockingHandle, Boolean> mouseOver = new HashMap<>();

	private DockingRegion rootRegion = null;
	private DockingRegion dockableRegion = null;
	private ToolbarLocation pinRegion = null;

	private final JFrame utilFrame;
	private final RootDockingPanelAPI targetRoot;

	// the dockable that we're currently trying to dock and is floating in a TempFloatingFrame
	private JPanel floating;
	// the dockable that the mouse is currently over, can be null
	private Dockable targetDockable = null;

	public boolean overTab = false;

	/**
	 * Create a new instance of the DockingHandles
	 *
	 * @param utilFrame The utility frame to draw the handles on
	 * @param root The root panel of the window we're drawing over
	 */
	public DockingHandles(JFrame utilFrame, RootDockingPanelAPI root) {
		this.utilFrame = utilFrame;

		this.targetRoot = root;

		setupHandle(rootCenter);
		setupHandle(rootWest);
		setupHandle(rootNorth);
		setupHandle(rootEast);
		setupHandle(rootSouth);

		setupHandle(pinWest);
		setupHandle(pinEast);
		setupHandle(pinSouth);

		setupHandle(dockableCenter);
		setupHandle(dockableWest);
		setupHandle(dockableNorth);
		setupHandle(dockableEast);
		setupHandle(dockableSouth);
	}

	/**
	 * Get the current region that we are moused over
	 *
	 * @return The current region, possibly null
	 */
	public DockingRegion getDockableRegion() {
		return dockableRegion;
	}

	/**
	 * Get the current root region that we are moused over
	 *
	 * @return The current root region, possibly null
	 */
	public DockingRegion getRootRegion() {
		return rootRegion;
	}

	/**
	 * Get the current pin region that we are moused over
	 *
	 * @return The current pin region, possibly null
	 */
	public ToolbarLocation getPinningRegion() {
		return pinRegion;
	}

	/**
	 * Set this docking handle active
	 *
	 * @param active Active state of handles
	 */
	public void setActive(boolean active) {
		utilFrame.setVisible(active);
	}

	/**
	 * Set the floating dockable
	 *
	 * @param dockable Dockable that is floating
	 */
	public void setFloating(JPanel dockable) {
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

		pinWest.setVisible(targetRoot != null && isPinningRegionAllowed(DockingRegion.WEST));
		pinEast.setVisible(targetRoot != null && isPinningRegionAllowed(DockingRegion.EAST));
		pinSouth.setVisible(targetRoot != null && isPinningRegionAllowed(DockingRegion.SOUTH));

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

			setLocation(pinWest, location.x - (size.width / 2) + rootHandleSpacing(pinWest), location.y - (size.height / 3));
			setLocation(pinEast, location.x + (size.width / 2) - rootHandleSpacing(pinEast), location.y - (size.height / 3));
			setLocation(pinSouth, location.x - (size.width / 3), location.y + (size.height / 2) - rootHandleSpacing(pinSouth));
		}
	}

	/**
	 * Retrieve the spacing for the handle
	 *
	 * @param handle The handle label
	 * @return width
	 */
	private int handleSpacing(JLabel handle) {
		return handle.getWidth() + 8;
	}

	/**
	 * Retrieve the spacing for the root handle
	 *
	 * @param handle The handle label
	 * @return width
	 */
	private int rootHandleSpacing(JLabel handle) {
		return handle.getWidth() + 16;
	}

	/**
	 * set the specific Dockable target which we'll show a basic handle in the center of
	 *
	 * @param dockable target dockable
	 */
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
		if (floating instanceof DockedSimplePanel) {
			DockedSimplePanel panel = (DockedSimplePanel) this.floating;
			Dockable floating = panel.getWrapper().getDockable();

			if (floating.getStyle() == DockableStyle.BOTH) {
				return true;
			}
			if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
				return floating.getStyle() == DockableStyle.HORIZONTAL;
			}
			return floating.getStyle() == DockableStyle.VERTICAL;
		}
		return true;
	}

	private boolean isPinningRegionAllowed(DockingRegion region) {
		if (floating instanceof DockedTabbedPanel) {
			return false;
		}
		Dockable floating = ((DisplayPanel) this.floating).getWrapper().getDockable();

		if (!floating.isPinningAllowed()) {
			return false;
		}

		if (floating.getPinningStyle() == DockableStyle.BOTH) {
			return true;
		}
		if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
			return floating.getPinningStyle() == DockableStyle.HORIZONTAL;
		}
		return floating.getPinningStyle() == DockableStyle.VERTICAL;
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

			// if this dockable is wrapped in a JScrollPane we need to set the handle to the center of the JScrollPane
			// not to the center of the dockable (which will more than likely be at a different location)
			if (targetDockable.isWrappableInScrollpane()) {
				Component parent = ((Component) targetDockable).getParent();

				while (parent != null && !(parent instanceof JScrollPane)) {
					parent = parent.getParent();
				}

				if (parent != null) {
					JScrollPane display = (JScrollPane) parent;

					location = display.getLocation();
					size = display.getSize();
				}
			}

			location.x += size.width / 2;
			location.y += size.height / 2;

			location.y -= (int) (DockingHandle.HANDLE_ICON_SIZE * (1.75/2));

			SwingUtilities.convertPointToScreen(location, ((Component) targetDockable).getParent());
			SwingUtilities.convertPointFromScreen(location, utilFrame);

			setLocation(dockableCenter, location.x, location.y);
			setLocation(dockableWest, location.x - handleSpacing(dockableWest), location.y);
			setLocation(dockableNorth, location.x, location.y - handleSpacing(dockableNorth));
			setLocation(dockableEast, location.x + handleSpacing(dockableEast), location.y);
			setLocation(dockableSouth, location.x, location.y + handleSpacing(dockableSouth));

		}
	}

	/**
	 * update the positions of the handles
	 *
	 * @param screenPos New mouse position
	 */
	public void update(Point screenPos) {
		JComponent component = targetRoot;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component.getParent());

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		SwingUtilities.convertPointToScreen(point, component.getParent());

//		utilFrame.setLocation(point);
//		utilFrame.setSize(size);

		setRootHandleLocations();
		setDockableHandleLocations();

		// check if the root handles happen to be under the dockable handles
		if (Math.abs(dockableEast.getX() - rootEast.getX()) < dockableEast.getWidth() &&
			Math.abs(dockableEast.getY() - rootEast.getY()) < dockableEast.getHeight()) {
			// need to move root east, move it up 1/4 of screen
			Point location = targetRoot.getLocation();
			Dimension rootSize = targetRoot.getSize();
			location.x += rootSize.width / 2;
			location.y += (rootSize.height / 2) + (rootSize.height / 4);

			SwingUtilities.convertPointToScreen(location, targetRoot.getParent());
			SwingUtilities.convertPointFromScreen(location, utilFrame);

//			setLocation(rootCenter, location.x, location.y);
//			setLocation(rootWest, location.x - (size.width / 2) + rootHandleSpacing(rootWest), location.y);
//			setLocation(rootNorth, location.x, location.y - (size.height / 2) + rootHandleSpacing(rootNorth));
			setLocation(rootEast, location.x + (rootSize.width / 2) - rootHandleSpacing(rootEast), location.y);
//			setLocation(rootSouth, location.x, location.y + (size.height / 2) - rootHandleSpacing(rootSouth));

		}

		framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

		rootRegion = null;
		dockableRegion = null;
		pinRegion = null;

		for (DockingHandle handle : mouseOver.keySet()) {
			boolean over = handle.isVisible() && handle.getBounds().contains(framePoint);

			mouseOver.put(handle, over);

			if (over) {
				if (handle.isRoot()) {
					rootRegion = handle.getRegion();
				}
				else if (handle.isPin()) {
					switch (handle.getRegion()) {
						case WEST:
							pinRegion = ToolbarLocation.WEST;
							break;
						case EAST:
							pinRegion = ToolbarLocation.EAST;
							break;
						case SOUTH:
							pinRegion = ToolbarLocation.SOUTH;
							break;
					}
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

	/**
	 * Paint the handles
	 *
	 * @param g Graphics instance to use
	 */
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

		Color background = DockingSettings.getHandleBackground();//DockingProperties.getHandlesBackground();
		Color border = DockingSettings.getHandleForeground();//DockingProperties.getHandlesBackgroundBorder();

		Graphics2D g2 = (Graphics2D) g.create();
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
		g2.setStroke(dashed);

		// draw root handles
		paintHandle(g, g2, rootCenter);
		paintHandle(g, g2, rootEast);
		paintHandle(g, g2, rootWest);
		paintHandle(g, g2, rootNorth);
		paintHandle(g, g2, rootSouth);

		paintHandle(g, g2, pinWest);
		paintHandle(g, g2, pinEast);
		paintHandle(g, g2, pinSouth);

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
