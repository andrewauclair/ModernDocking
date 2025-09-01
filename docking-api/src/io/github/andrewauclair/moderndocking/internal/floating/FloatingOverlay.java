/*
Copyright (c) 2024 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.internal.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.CustomTabbedPane;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Utility for displaying the overlay highlight over the target frame while floating a dockable
 */
public class FloatingOverlay {
    // determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
    private static final double REGION_SENSITIVITY = 0.35;

    /**
     * Is this overlay visible?
     */
    private boolean visible = false;

    /**
     * the top left location where the overlay starts
     */
    private Point location = new Point(0, 0);
    /**
     * the total size of the overlay, used for drawing
     */
    private Dimension size = new Dimension(0, 0);
    private Rectangle targetTab = null;

    private final DockingAPI docking;
    private final JFrame utilFrame;

    private Point prevLocation = location;
    private Dimension prevSize = size;

    /**
     * Create a new overlay, attached to a utility frame
     *
     * @param docking The docking instance this overlay belongs to
     * @param utilFrame The utility frame this overlay is tied to
     */
    public FloatingOverlay(DockingAPI docking, JFrame utilFrame) {
        this.docking = docking;
        this.utilFrame = utilFrame;
    }

    /**
     * Check if the overlay needs to be redrawn
     *
     * @return True if the overlay requires a redraw
     */
    public boolean requiresRedraw() {
        return !location.equals(prevLocation) ||
                !size.equals(prevSize);
    }

    /**
     * Overlay has been redrawn. Clear the flag
     */
    public void clearRedraw() {
        prevLocation = location;
        prevSize = size;
    }

    /**
     * Update the overlay for a new region in the root panel
     *
     * @param rootPanel The root panel that we're showing root handles in
     * @param region The region of the handle the mouse is over
     */
    public void updateForRoot(InternalRootDockingPanel rootPanel, DockingRegion region) {
        setVisible(true);

        targetTab = null;

        Point point = rootPanel.getLocation();
        Dimension size = rootPanel.getSize();

        point = SwingUtilities.convertPoint(rootPanel.getParent(), point, utilFrame);

        final double DROP_SIZE = 4;

        prevLocation = new Point(this.location);
        prevSize = new Dimension(this.size);

        switch (region) {
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

    /**
     * Update the overlay for a new target dockable
     *
     * @param targetDockable The new target dockable
     * @param floatingDockable The dockable being floated
     * @param mousePosOnScreen The position of the mouse on screen
     * @param region The region of the dockable the overlay is over
     */
    public void updateForDockable(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen, DockingRegion region) {
        setVisible(true);

        prevLocation = new Point(this.location);
        prevSize = new Dimension(this.size);

        targetTab = null;

        if (region == null) {
            region = getRegion(targetDockable, floatingDockable, mousePosOnScreen);
        }

        JComponent component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();

        Point point = component.getLocation();
        Dimension size = component.getSize();

        point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

        final double DROP_SIZE = 2;

        switch (region) {
            case WEST: {
                size.width /= (int) DROP_SIZE;
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

    /**
     * Update the overlay for dispay over a tab
     *
     * @param tabbedPane The tabbed pane the mouse is over
     * @param mousePosOnScreen The mouse position on screen
     */
    public void updateForTab(CustomTabbedPane tabbedPane, Point mousePosOnScreen) {
        setVisible(true);

        prevLocation = new Point(this.location);
        prevSize = new Dimension(this.size);

        Component componentAt = tabbedPane.getComponentAt(0);

        location = componentAt.getLocation();
        SwingUtilities.convertPointToScreen(location, tabbedPane);
        SwingUtilities.convertPointFromScreen(location, utilFrame);

        size = componentAt.getSize();

        int targetTabIndex = tabbedPane.getTargetTabIndex(mousePosOnScreen, true);

        if (targetTabIndex != -1) {
            targetTab = tabbedPane.getBoundsAt(targetTabIndex);

            Point p = new Point(targetTab.x, targetTab.y);
            SwingUtilities.convertPointToScreen(p, tabbedPane);
            SwingUtilities.convertPointFromScreen(p, utilFrame);

            targetTab.x = p.x;
            targetTab.y = p.y;

            targetTab.width /= 2;
        }
        else {
            targetTab = tabbedPane.getBoundsAt(tabbedPane.getTabCount() - 1);

            Point tabPoint = new Point(tabbedPane.getX(), tabbedPane.getY());
            SwingUtilities.convertPointToScreen(tabPoint, tabbedPane.getParent());

            Point boundsPoint = new Point(targetTab.x, targetTab.y);
            SwingUtilities.convertPointToScreen(boundsPoint, tabbedPane);

            int widthToAdd = targetTab.width;

            if (boundsPoint.x + (targetTab.width * 2) >= tabPoint.x + tabbedPane.getWidth()) {
                targetTab.width = Math.abs((tabPoint.x + tabbedPane.getWidth()) - (boundsPoint.x + targetTab.width));
            }

            SwingUtilities.convertPointFromScreen(boundsPoint, utilFrame);

            targetTab.x = boundsPoint.x + widthToAdd;
            targetTab.y = boundsPoint.y;
        }
    }

    /**
     * Set the visibility of the overlay
     *
     * @param visible New visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Check which region the overlay is over
     *
     * @param targetDockable The target dockable
     * @param floatingDockable The dockable that is being floated
     * @param mousePosOnScreen The mouse position on screen
     *
     * @return The region the overlay is over, or null
     */
    public DockingRegion getRegion(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen) {
        JComponent component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();

        Point framePoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

        Point point = (component).getLocation();
        Dimension size = component.getSize();

        point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

        double horizontalPct = (framePoint.x - point.x) / (double) size.width;
        double verticalPct = (framePoint.y - point.y) / (double) size.height;

        double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
        double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

        if (horizontalEdgeDist < verticalEdgeDist) {
            if (horizontalPct < REGION_SENSITIVITY && isRegionAllowed(targetDockable, DockingRegion.WEST) && isRegionAllowed(floatingDockable, DockingRegion.WEST)) {
                return DockingRegion.WEST;
            }
            else if (horizontalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(targetDockable, DockingRegion.EAST) && isRegionAllowed(floatingDockable, DockingRegion.EAST)) {
                return DockingRegion.EAST;
            }
        }
        else {
            if (verticalPct < REGION_SENSITIVITY && isRegionAllowed(targetDockable, DockingRegion.NORTH) && isRegionAllowed(floatingDockable, DockingRegion.NORTH)) {
                return DockingRegion.NORTH;
            }
            else if (verticalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(targetDockable, DockingRegion.SOUTH) && isRegionAllowed(floatingDockable, DockingRegion.SOUTH)) {
                return DockingRegion.SOUTH;
            }
        }
        return DockingRegion.CENTER;
    }

    /**
     * Is the mouse currently over a tab? Renders a different overlay
     * @return Over tab
     */
    public boolean isOverTab() {
        return targetTab != null;
    }

    /**
     * Paint the overlay
     *
     * @param g Graphics instance
     */
    public void paint(Graphics g) {
        if (!visible) {
            return;
        }
        g.setColor(DockingSettings.getOverlayBackground());
        g.fillRect(location.x, location.y, size.width, size.height);

        if (targetTab != null) {
            g.fillRect(targetTab.x, targetTab.y, targetTab.width, targetTab.height);
        }
    }

    // check if the floating dockable is allowed to dock to this region
    private boolean isRegionAllowed(Dockable dockable, DockingRegion region) {
        if (dockable == null) {
            return true;
        }
        if (dockable.getStyle() == DockableStyle.BOTH) {
            return true;
        }
        if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
            return dockable.getStyle() == DockableStyle.HORIZONTAL;
        }
        return dockable.getStyle() == DockableStyle.VERTICAL;
    }

    /**
     * Check if this overlay is visible
     *
     * @return Is visible
     */
    public boolean isVisible() {
        return visible;
    }
}