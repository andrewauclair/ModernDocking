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
package io.github.andrewauclair.moderndocking.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.CustomTabbedPane;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;

import javax.swing.*;
import java.awt.*;

public class FloatingOverlay {
    // determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
    private static final double REGION_SENSITIVITY = 0.35;

    private boolean visible = false;

    // the top left location where the overlay starts
    private Point location = new Point(0, 0);
    // the total size of the overlay, used for drawing
    private Dimension size = new Dimension(0, 0);
    private Rectangle targetTab = null;

    private final DockingAPI docking;
    private final JFrame utilFrame;

    private Point prevLocation = location;
    private Dimension prevSize = size;

    public FloatingOverlay(DockingAPI docking, JFrame utilFrame) {
        this.docking = docking;
        this.utilFrame = utilFrame;
    }

    public boolean requiresRedraw() {
        return !location.equals(prevLocation) ||
                !size.equals(prevSize);
    }

    public void clearRedraw() {
        prevLocation = location;
        prevSize = size;
    }

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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

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

    public boolean isOverTab() {
        return targetTab != null;
    }

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

    public boolean isVisible() {
        return visible;
    }
}