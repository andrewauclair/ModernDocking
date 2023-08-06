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

import ModernDocking.DockingRegion;
import ModernDocking.internal.DockingProperties;

import javax.swing.*;
import java.awt.*;

/**
 * Special label used to draw the docking handles on an overlay
 */
public class DockingHandle extends JLabel {
	/**
	 * The size to draw the docking handle, in pixels
	 */
	public static final int HANDLE_ICON_SIZE = 32;

	/**
	 * The region that this docking handle display is drawing
	 */
	private final DockingRegion region;
	/**
	 * Flag indicating whether this handle display is for the root handles
	 */
	private final boolean isRoot;

	/**
	 * Create a new DockingHandle
	 *
	 * @param region The region of this DockingHandle
	 * @param isRoot Is this the root docking handle?
	 */
	public DockingHandle(DockingRegion region, boolean isRoot) {
		this.region = region;
		this.isRoot = isRoot;

		// set the bounds (we're not in a layout manager) and make sure this handle isn't visible
		setBounds(0, 0, HANDLE_ICON_SIZE, HANDLE_ICON_SIZE);
		setVisible(false);
	}

	/**
	 * Get the region for this handle
	 *
	 * @return Region this DockingHandle represents
	 */
	public DockingRegion getRegion() {
		return region;
	}

	/**
	 * Check if this DockingHandle is for the root of the panel
	 *
	 * @return Whether this is the root docking handle
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * Paint the handle
	 *
	 * @param g used to do the main paint operations
	 * @param g2 used to draw the dashed lines on top
	 * @param mouseOver is the mouse over this handle?
	 */
	public void paintHandle(Graphics g, Graphics2D g2, boolean mouseOver) {
		Rectangle bounds = getBounds();

		Color background = DockingProperties.getHandlesBackground();
		Color hover = DockingProperties.getHandlesFill();
		Color outline = DockingProperties.getHandlesOutline();

		// each root handle has its own background. we have to draw them here.
		// the dockables all share one big root that is drawn in DockingHandles
		if (isRoot) {
			g.setColor(background);
			drawBackground(g);
		}

		if (mouseOver) {
			g.setColor(hover);
			fillMouseOverRegion(g);
		}

		// only draw the dashed line if the region isn't center and these are not root handles
		if (region != DockingRegion.CENTER && !isRoot) {
			drawDashedLine(g2);
		}

		// draw the outline over the mouse over
		g.setColor(outline);

		System.out.println(bounds);

		if (isRoot && region != DockingRegion.CENTER) {
			drawRootOutline(g);
		}
		else {
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	private void drawBackground(Graphics g) {
		int spacing = 8;

		int x = getX() - spacing;
		int y = getY() - spacing;
		int width = getWidth() + (spacing * 2);
		int height = getHeight() + (spacing * 2);

		g.fillRect(x, y, width, height);

		Color border = DockingProperties.getHandlesBackgroundBorder();

		g.setColor(border);
		g.drawRect(x, y, width, height);
	}

	private void drawRootOutline(Graphics g) {
		Rectangle bounds = getBounds();

		boolean north = region == DockingRegion.NORTH;
		boolean south = region == DockingRegion.SOUTH;
		boolean east = region == DockingRegion.EAST;

		int halfWidth = bounds.width / 2;

		int x = east ? bounds.x + halfWidth : bounds.x;
		int y = south ? bounds.y + halfWidth : bounds.y;
		int width = north || south ? bounds.width : halfWidth;
		int height = north || south ? halfWidth : bounds.height;

		if (region == DockingRegion.CENTER) {
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		else {
			g.drawRect(x, y, width, height);
		}
	}

	private void fillMouseOverRegion(Graphics g) {
		Rectangle bounds = getBounds();

		boolean north = region == DockingRegion.NORTH;
		boolean south = region == DockingRegion.SOUTH;
		boolean east = region == DockingRegion.EAST;

		int halfWidth = bounds.width / 2;

		int x = east ? bounds.x + halfWidth : bounds.x;
		int y = south ? bounds.y + halfWidth : bounds.y;
		int width = north || south ? bounds.width : halfWidth;
		int height = north || south ? halfWidth : bounds.height;

		if (region == DockingRegion.CENTER) {
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		else {
			g.fillRect(x, y, width, height);
		}
	}

	private void drawDashedLine(Graphics2D g2) {
		Rectangle bounds = getBounds();

		boolean north = region == DockingRegion.NORTH;
		boolean south = region == DockingRegion.SOUTH;

		int halfWidth = bounds.width / 2;

		int x = north || south ? bounds.x : bounds.x + halfWidth;
		int y = north || south ? bounds.y + halfWidth : bounds.y;
		int x2 = north || south ? bounds.x + bounds.width : bounds.x + halfWidth;
		int y2 = north || south ? bounds.y + halfWidth : bounds.y + bounds.height;

		if (region == DockingRegion.CENTER) {
			g2.drawLine(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		else {
			g2.drawLine(x, y, x2, y2);
		}
	}
}
