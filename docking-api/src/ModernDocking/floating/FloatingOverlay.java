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
package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.DockableStyle;
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.CustomTabbedPane;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.internal.DockingInternal;
import ModernDocking.ui.DockingSettings;

import javax.swing.*;
import java.awt.*;

public class FloatingOverlay {
    // determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
    private static final double REGION_SENSITIVITY = 0.35;

    private boolean visible;

    // the top left location where the overlay starts
    private Point location = new Point(0, 0);
    // the total size of the overlay, used for drawing
    private Dimension size;
    private Rectangle targetTab = null;

    private final DockingAPI docking;
    private final JFrame utilFrame;

    public FloatingOverlay(DockingAPI docking, JFrame utilFrame) {
        this.docking = docking;
        this.utilFrame = utilFrame;
    }

    public void updateForRoot(RootDockingPanelAPI rootPanel, DockingRegion region) {
        setVisible(true);

        targetTab = null;

        Point point = rootPanel.getLocation();
        Dimension size = rootPanel.getSize();

        point = SwingUtilities.convertPoint(rootPanel.getParent(), point, utilFrame);

        final double DROP_SIZE = 4;

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

    public void updateForDockable(Dockable dockable, Point mousePosOnScreen, DockingRegion region) {
        setVisible(true);

        targetTab = null;

        if (region == null) {
            JComponent component = DockingInternal.get(docking).getWrapper(dockable).getDisplayPanel();

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
                if (horizontalPct < REGION_SENSITIVITY && isRegionAllowed(dockable, DockingRegion.WEST)) {
                    size = new Dimension(size.width / 2, size.height);
                }
                else if (horizontalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(dockable, DockingRegion.EAST)) {
                    point.x += size.width / 2;
                    size = new Dimension(size.width / 2, size.height);
                }
            }
            else {
                if (verticalPct < REGION_SENSITIVITY && isRegionAllowed(dockable, DockingRegion.NORTH)) {
                    size = new Dimension(size.width, size.height / 2);
                }
                else if (verticalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(dockable, DockingRegion.SOUTH)) {
                    point.y += size.height / 2;
                    size = new Dimension(size.width, size.height / 2);
                }
            }

            this.location = point;
            this.size = size;

            return;
        }
        JComponent component = DockingInternal.get(docking).getWrapper(dockable).getDisplayPanel();

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
        if (dockable.getStyle() == DockableStyle.BOTH) {
            return true;
        }
        if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
            return dockable.getStyle() == DockableStyle.HORIZONTAL;
        }
        return dockable.getStyle() == DockableStyle.VERTICAL;
    }
}
