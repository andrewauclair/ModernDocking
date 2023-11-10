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
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DockedSimplePanel;
import ModernDocking.internal.DockingInternal;
import ModernDocking.internal.DockingPanel;
import ModernDocking.ui.DockingSettings;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;

/**
 * displays the overlay highlight of where the panel will be docked
 */
public class DockingOverlay {
	// determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
	private static final double REGION_SENSITIVITY = 0.35;

	// the target root for this overlay, always the same
	private final RootDockingPanelAPI targetRoot;

	// the dockable that is currently floating in its own undecoarted frame
	private JPanel floating;

	// the target dockable that the mouse is currently over, could be null
	private Dockable targetDockable;

	// the region on the dockable that is being docked to, this comes from the handles? I think
	private DockingRegion dockableRegion;

	// the region on the root that is being docked to, this comes from the handles? I think
	private DockingRegion rootRegion;
	private ToolbarLocation pinToolbarLocation;

	// the top left location where the overlay starts
	private Point location = new Point(0, 0);
	// the total size of the overlay, used for drawing
	private Dimension size;

	private final DockingAPI docking;
	// the utility frame that this overlay belongs to
	private final JFrame utilFrame;

	// whether to draw this overlay, different from swing visibility because we're manually painting
	private boolean visible = false;

	// override for the visible flag, sometimes internally we don't want to draw but we might be active
	private boolean visibleOverride = false;

	public boolean overTab = false;
	public Rectangle targetTab;
	public boolean beforeTab = true;

	/**
	 * Construct a new overlay for a utility frame and root panel
	 *
	 * @param utilFrame The utility frame this overlay covers
	 * @param root The root of the frame under the utility frame
	 */
	public DockingOverlay(DockingAPI docking, JFrame utilFrame, RootDockingPanelAPI root) {
		this.docking = docking;
		this.utilFrame = utilFrame;

		targetRoot = root;
		size = utilFrame.getSize();
	}

	/**
	 * Set this overlay active. Sets the overlay to visible.
	 *
	 * @param active Should the overlay be displayed?
	 */
	public void setActive(boolean active) {
		visible = active;

		floating = null;
		targetDockable = null;
		dockableRegion = null;
		rootRegion = null;
		size = new Dimension(0, 0);
	}

	/**
	 * Set a reference to the dockable currently being floated
	 *
	 * @param dockable Current floating dockable
	 */
	public void setFloating(JPanel dockable) {
		floating = dockable;
	}

	/**
	 * Set the target dockable which is currently under the mouse position.
	 *
	 * @param dockable Target dockable under mouse
	 */
	public void setTargetDockable(Dockable dockable) {
		targetDockable = dockable;
	}

	// check if the floating dockable is allowed to dock to this region
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

	public void update(Point screenPos) {
		// the last region that we calculated. used for painting
		DockingRegion lastSelectedRegion;

		if (targetRoot != null && rootRegion != null) {
			Point point = targetRoot.getLocation();
			Dimension size = targetRoot.getSize();

			point = SwingUtilities.convertPoint(targetRoot.getParent(), point, utilFrame);

			lastSelectedRegion = rootRegion;

			final double DROP_SIZE = 4;

			switch (rootRegion) {
				case WEST: {
					size = new Dimension((int) (size.width / DROP_SIZE), size.height);
					break;
				}
				case NORTH: {
					size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
					break;
				}
				case EAST: {
					point.x += size.width - (size.width / DROP_SIZE);
					size = new Dimension((int) (size.width / DROP_SIZE), size.height);
					break;
				}
				case SOUTH: {
					point.y += size.height - (size.height / DROP_SIZE);
					size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
					break;
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null && dockableRegion != null) {
			JComponent component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();

			Point point = component.getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			lastSelectedRegion = dockableRegion;

			final double DROP_SIZE = 2;

			switch (dockableRegion) {
				case WEST: {
					size = new Dimension((int) (size.width / DROP_SIZE), size.height);
					break;
				}
				case NORTH: {
					size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
					break;
				}
				case EAST: {
					point.x += size.width / DROP_SIZE;
					size = new Dimension((int) (size.width / DROP_SIZE), size.height);
					break;
				}
				case SOUTH: {
					point.y += size.height / DROP_SIZE;
					size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
					break;
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null) {
			JComponent component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();

			Point framePoint = new Point(screenPos);
			SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			double horizontalPct = (framePoint.x - point.x) / (double) size.width;
			double verticalPct = (framePoint.y - point.y) / (double) size.height;

			double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
			double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

			if (horizontalEdgeDist < verticalEdgeDist) {
				if (horizontalPct < REGION_SENSITIVITY && isRegionAllowed(DockingRegion.WEST)) {
					lastSelectedRegion = DockingRegion.WEST;
					size = new Dimension(size.width / 2, size.height);
				}
				else if (horizontalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(DockingRegion.EAST)) {
					lastSelectedRegion = DockingRegion.EAST;
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
			}
			else {
				if (verticalPct < REGION_SENSITIVITY && isRegionAllowed(DockingRegion.NORTH)) {
					lastSelectedRegion = DockingRegion.NORTH;
					size = new Dimension(size.width, size.height / 2);
				}
				else if (verticalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(DockingRegion.SOUTH)) {
					lastSelectedRegion = DockingRegion.SOUTH;
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetRoot != null) {
			JComponent component = targetRoot;

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			lastSelectedRegion = DockingRegion.CENTER;

			this.location = point;
			this.size = size;
		}

		utilFrame.revalidate();
		utilFrame.repaint();
	}

	/**
	 * get the region that we're currently displaying an overlay for
	 *
	 * @param screenPos Screen position to find the region for
	 * @return Region of the given screen position
	 */
	public DockingRegion getRegion(Point screenPos) {
		// if we're moused over a root handle, use the region of the handle
		if (rootRegion != null) {
			return rootRegion;
		}

		// if we're moused over a dockable handle, use the region of the handle
		if (dockableRegion != null) {
			return dockableRegion;
		}

		// force the region to always be the center if the root is empty
		if (targetRoot.getPanel() == null) {
			return DockingRegion.CENTER;
		}

		// use the target dockable if we have one, otherwise use the root
		JComponent component;
		if (targetDockable != null) {
			component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();
		}
		else {
			component = targetRoot;
		}

		// find the mouse position over the component
		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component.getParent());

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		// calculate a percentage along the horizontal axis and vertical axis. we need to determine if we're in the center or one of the other 4 regions
		double horizontalPct = (framePoint.x - point.x) / (double) size.width;
		double verticalPct = (framePoint.y - point.y) / (double) size.height;

		// find out if we are in a horizontal region (NORTH / SOUTH) or a vertical region (WEST / EAST)
		double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
		double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

		// if we're close to the sides than we are to the bottom or bottom, then we might be in the WEST or EAST region
		if (horizontalEdgeDist < verticalEdgeDist) {
			// horizontal percentage is less than our sensitivity for the edge, we're in the WEST region
			if (horizontalPct < REGION_SENSITIVITY && isRegionAllowed(DockingRegion.WEST)) {
				return DockingRegion.WEST;
			}
			// horizontal percentage is greater than our sensitivity for the edge, we're in the EAST region
			else if (horizontalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(DockingRegion.EAST)) {
				return DockingRegion.EAST;
			}
			// we didn't exceed the sensitivity in the WEST or EAST regions. we're in the CENTER region
		}
		else {
			// vertical percentage is less than our sensitivity for the edge, we're in the NORTH region
			if (verticalPct < REGION_SENSITIVITY && isRegionAllowed(DockingRegion.NORTH)) {
				return DockingRegion.NORTH;
			}
			// vertical percentage is greater than our sensitivity for the edge, we're in the SOUTH region
			else if (verticalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(DockingRegion.SOUTH)) {
				return DockingRegion.SOUTH;
			}
			// we didn't exceed the sensitivity in the NORTH or SOUTH regions. we're in the CENTER region
		}
		return DockingRegion.CENTER;
	}

	public ToolbarLocation getToolbarLocation() {
		return pinToolbarLocation;
	}

	/**
	 * Check if the floating dockable is targeting a root docking handle
	 *
	 * @return True if the currently selected docking handle target or area is on the root panel
	 */
	public boolean isDockingToRoot() {
		// force the region to always be the center if the root is empty
		if (targetRoot.getPanel() == null) {
			return true;
		}
		return rootRegion != null;
	}

	/**
	 * Checks if we're docking to another dockable
	 *
	 * @return True if the target dockable or dockable region are not null
	 */
	public boolean isDockingToDockable() {
		return dockableRegion != null || targetDockable != null;
	}

	public boolean isDockingToPin() {
		return pinToolbarLocation != null;
	}

	// set a region from the handles if we're moused over a root handle
	public void setTargetRootRegion(DockingRegion region) {
		rootRegion = region;

		// we should only be visible if we're docking to a root or dockable. otherwise the overlay should be hidden.
		visibleOverride = !isDockingToRoot() && !isDockingToDockable();
	}

	// set a region from the handles if we're moused over a dockable handle
	public void setTargetDockableRegion(DockingRegion region) {
		if (overTab) { return;}
		dockableRegion = region;

		// we should only be visible if we're docking to a root or dockable. otherwise the overlay should be hidden.
		visibleOverride = !isDockingToRoot() && !isDockingToDockable();
	}

	public void setTargetPinRegion(ToolbarLocation region) {
		pinToolbarLocation = region;

		// we should only be visible if we're docking to a root or dockable. otherwise the overlay should be hidden.
		visibleOverride = !isDockingToRoot() && !isDockingToDockable();
	}

	/**
	 * Paint the docking overlay if visible
	 *
	 * @param g Graphics to use for painting
	 */
	public void paint(Graphics g) {
		if (!isDockingToRoot() && !isDockingToDockable()) {
			return;
		}

		if (isDockingToPin()) {
			return;
		}

		if (visible) {
			g.setColor(DockingSettings.getOverlayBackground());
			g.fillRect(location.x, location.y, size.width, size.height);

			if (overTab) {
				g.fillRect(targetTab.x, targetTab.y, targetTab.width, targetTab.height);
			}
		}
	}
}
