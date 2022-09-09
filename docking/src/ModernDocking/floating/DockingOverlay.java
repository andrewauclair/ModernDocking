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

import ModernDocking.*;
import ModernDocking.internal.DockingColors;
import ModernDocking.internal.DockingInternal;

import javax.swing.*;
import java.awt.*;

// displays the overlay highlight of where the panel will be docked
public class DockingOverlay {
	// determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
	private static final double REGION_SENSITIVITY = 0.35;

	// the target root for this overlay, always the same
	private final RootDockingPanel targetRoot;

	// the dockable that is currently floating in its own undecoarted frame
	private Dockable floating;

	// the target dockable that the mouse is currently over, could be null
	private Dockable targetDockable;

	// the region on the dockable that is being docked to, this comes from the handles? I think
	private DockingRegion dockableRegion;

	// the region on the root that is being docked to, this comes from the handles? I think
	private DockingRegion rootRegion;

	// the last region that we calculated. used for painting
	private DockingRegion lastSelectedRegion;

	// the top left location where the overlay starts
	private Point location = new Point(0, 0);
	// the total size of the overlay, used for drawing
	private Dimension size;

	// the utility frame that this overlay belongs to
	private final JFrame utilFrame;

	// whether to draw this overlay, different from swing visiblity because we're manually painting
	private boolean visible = false;

	public DockingOverlay(JFrame utilFrame, RootDockingPanel root) {
		this.utilFrame = utilFrame;

		targetRoot = root;
		size = utilFrame.getSize();
	}

	public void setActive(boolean active) {
		visible = active;
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	public void setTargetDockable(Dockable dockable) {
		targetDockable = dockable;
	}

	// check if the floating dockable is allowed to dock to this region
	private boolean isRegionAllowed(DockingRegion region) {
		return floating.disallowedRegions() == null || !floating.disallowedRegions().contains(region);
	}

	public void update(Point screenPos) {
		if (targetRoot != null && rootRegion != null) {
			Point point = targetRoot.getLocation();
			Dimension size = targetRoot.getSize();

			point = SwingUtilities.convertPoint(targetRoot.getParent(), point, utilFrame);

			lastSelectedRegion = rootRegion;

			switch (rootRegion) {
				case WEST: {
					size = new Dimension(size.width / 2, size.height);
					break;
				}
				case NORTH: {
					size = new Dimension(size.width, size.height / 2);
					break;
				}
				case EAST: {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
					break;
				}
				case SOUTH: {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
					break;
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null && dockableRegion != null) {
			JComponent component = DockingInternal.getWrapper(targetDockable).getDisplayPanel();

			Point point = component.getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			lastSelectedRegion = dockableRegion;

			switch (dockableRegion) {
				case WEST: {
					size = new Dimension(size.width / 2, size.height);
					break;
				}
				case NORTH: {
					size = new Dimension(size.width, size.height / 2);
					break;
				}
				case EAST: {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
					break;
				}
				case SOUTH: {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
					break;
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null) {
			JComponent component = DockingInternal.getWrapper(targetDockable).getDisplayPanel();

			Point framePoint = new Point(screenPos);
			SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			double horizontalPct = (framePoint.x - point.x) / (double) size.width;
			double verticalPct = (framePoint.y - point.y) / (double) size.height;

			double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
			double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

			boolean westAllowed = isRegionAllowed(DockingRegion.WEST);
			boolean eastAllowed = isRegionAllowed(DockingRegion.EAST);

			if (horizontalEdgeDist < verticalEdgeDist) {// && (westAllowed || eastAllowed)) {
				if (horizontalPct < REGION_SENSITIVITY) {// && westAllowed) {
					lastSelectedRegion = DockingRegion.WEST;
					size = new Dimension(size.width / 2, size.height);
				}
				else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {// && eastAllowed) {
					lastSelectedRegion = DockingRegion.EAST;
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
			}
			else {
				if (verticalPct < REGION_SENSITIVITY) {// && isRegionAllowed(DockingRegion.NORTH)) {
					lastSelectedRegion = DockingRegion.NORTH;
					size = new Dimension(size.width, size.height / 2);
				}
				else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {// && isRegionAllowed(DockingRegion.SOUTH)) {
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

	// get the region that we're currently displaying an overlay for
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
			component = DockingInternal.getWrapper(targetDockable).getDisplayPanel();
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
			if (horizontalPct < REGION_SENSITIVITY) {
				return DockingRegion.WEST;
			}
			// horizontal percentage is greater than our sensitivity for the edge, we're in the EAST region
			else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.EAST;
			}
			// we didn't exceed the sensitivity in the WEST or EAST regions. we're in the CENTER region
		}
		else {
			// vertical percentage is less than our sensitivity for the edge, we're in the NORTH region
			if (verticalPct < REGION_SENSITIVITY) {
				return DockingRegion.NORTH;
			}
			// vertical percentage is greater than our sensitivity for the edge, we're in the SOUTH region
			else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.SOUTH;
			}
			// we didn't exceed the sensitivity in the NORTH or SOUTH regions. we're in the CENTER region
		}
		return DockingRegion.CENTER;
	}

	public boolean isDockingToRoot() {
		// force the region to always be the center if the root is empty
		if (targetRoot.getPanel() == null) {
			return true;
		}
		return rootRegion != null;
	}

	public boolean isDockingToDockable() {
		return dockableRegion != null || targetDockable != null;
	}

	// set a region from the handles if we're moused over a root handle
	public void setTargetRootRegion(DockingRegion region) {
		rootRegion = region;
	}

	// set a region from the handles if we're moused over a dockable handle
	public void setTargetDockableRegion(DockingRegion region) {
		dockableRegion = region;
	}

	public void paint(Graphics g) {
		if (visible && isRegionAllowed(lastSelectedRegion)) {
			g.setColor(DockingColors.getDockingOverlay());
			g.fillRect(location.x, location.y, size.width, size.height);
			g.setColor(DockingColors.getDockingOverlayBorder());
			g.fillRect(location.x, location.y, size.width, size.height);
		}
	}
}
