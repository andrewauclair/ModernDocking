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
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import static io.github.andrewauclair.moderndocking.internal.floating.DockingHandle.HANDLE_ICON_SIZE;

/**
 * Class for handling all of the docking handle operations while floating a dockable
 */
public class DockableHandles {
    private final DockingHandle dockableCenter = new DockingHandle(DockingRegion.CENTER, false);
    private final DockingHandle dockableWest = new DockingHandle(DockingRegion.WEST, false);
    private final DockingHandle dockableNorth = new DockingHandle(DockingRegion.NORTH, false);
    private final DockingHandle dockableEast = new DockingHandle(DockingRegion.EAST, false);
    private final DockingHandle dockableSouth = new DockingHandle(DockingRegion.SOUTH, false);
    private final JFrame frame;
    private final Dockable targetDockable;
    private final Dockable floatingDockable;

    /**
     * Create a new instance for the frame and target dockable
     *
     * @param frame The frame to display these handles on
     * @param targetDockable The target dockable the mouse is over
     */
    public DockableHandles(JFrame frame, Dockable targetDockable) {
        this.frame = frame;
        this.targetDockable = targetDockable;
        this.floatingDockable = null;

        setupHandle(frame, dockableCenter);
        setupHandle(frame, dockableWest);
        setupHandle(frame, dockableNorth);
        setupHandle(frame, dockableEast);
        setupHandle(frame, dockableSouth);

        setDockableHandleLocations();
    }

    /**
     * Create a new instance for the frame and target dockable
     *
     * @param frame The frame to display these handles on
     * @param targetDockable The target dockable the mouse is over
     * @param floatingDockable The dockable is floating
     */
    public DockableHandles(JFrame frame, Dockable targetDockable, Dockable floatingDockable) {
        this.frame = frame;
        this.targetDockable = targetDockable;
        this.floatingDockable = floatingDockable;

        setupHandle(frame, dockableCenter);
        setupHandle(frame, dockableWest);
        setupHandle(frame, dockableNorth);
        setupHandle(frame, dockableEast);
        setupHandle(frame, dockableSouth);

        setDockableHandleLocations();
    }

    /**
     * The mouse has moved. Update all the handles for mouse over events
     *
     * @param mousePosOnScreen The new mouse position on screen
     */
    public void mouseMoved(Point mousePosOnScreen) {
        Point framePoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(framePoint, frame);

        dockableCenter.mouseMoved(framePoint);
        dockableWest.mouseMoved(framePoint);
        dockableNorth.mouseMoved(framePoint);
        dockableEast.mouseMoved(framePoint);
        dockableSouth.mouseMoved(framePoint);
    }

    private void setupHandle(JFrame frame, DockingHandle label) {
        label.setVisible(false);
        frame.add(label);
    }

    private void setDockableHandleLocations() {
        dockableCenter.setVisible(true);
        dockableWest.setVisible(isRegionAllowed(targetDockable, DockingRegion.WEST));
        dockableNorth.setVisible(isRegionAllowed(targetDockable, DockingRegion.NORTH));
        dockableEast.setVisible(isRegionAllowed(targetDockable, DockingRegion.EAST));
        dockableSouth.setVisible(isRegionAllowed(targetDockable, DockingRegion.SOUTH));

        if (floatingDockable != null) {
            if (!isRegionAllowed(floatingDockable, DockingRegion.WEST)) {
                dockableWest.setVisible(false);
            }
            if (!isRegionAllowed(floatingDockable, DockingRegion.NORTH)) {
                dockableNorth.setVisible(false);
            }
            if (!isRegionAllowed(floatingDockable, DockingRegion.EAST)) {
                dockableEast.setVisible(false);
            }
            if (!isRegionAllowed(floatingDockable, DockingRegion.SOUTH)) {
                dockableSouth.setVisible(false);
            }
        }

        if (((Component) targetDockable).getParent() != null) {
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

            location.y -= (int) (HANDLE_ICON_SIZE * (1.75/2));

            SwingUtilities.convertPointToScreen(location, ((Component) targetDockable).getParent());
            SwingUtilities.convertPointFromScreen(location, frame);

            setLocation(dockableCenter, location.x, location.y);
            setLocation(dockableWest, location.x - handleSpacing(dockableWest), location.y);
            setLocation(dockableNorth, location.x, location.y - handleSpacing(dockableNorth));
            setLocation(dockableEast, location.x + handleSpacing(dockableEast), location.y);
            setLocation(dockableSouth, location.x, location.y + handleSpacing(dockableSouth));
        }
    }

    private void setLocation(Component component, int x, int y) {
        component.setLocation(x - (HANDLE_ICON_SIZE / 2), y - (HANDLE_ICON_SIZE / 2));
    }

    private boolean isRegionAllowed(Dockable dockable, DockingRegion region) {
        if (dockable.getStyle() == DockableStyle.BOTH) {
            return true;
        }
        if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
            return dockable.getStyle() == DockableStyle.HORIZONTAL;
        }
        return dockable.getStyle() == DockableStyle.VERTICAL;
    }

    /**
     * Paint the handles
     *
     * @param g2 The graphics instance
     */
    public void paint(Graphics2D g2) {
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

        // draw the dockable handles background over the root handles in case they overlap
        // fill the dockable handles background
        g2.setColor(background);
        g2.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);

        // draw the dockable handles border
        g2.setColor(border);
        g2.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);

        // draw the docking handles over the docking handles background
        dockableCenter.paintHandle(g2);
        dockableEast.paintHandle(g2);
        dockableWest.paintHandle(g2);
        dockableNorth.paintHandle(g2);
        dockableSouth.paintHandle(g2);
    }

    private int handleSpacing(JLabel handle) {
        return handle.getWidth() + 8;
    }

    /**
     * Find the region of the docking handle the mouse is over
     *
     * @return Region the mouse is over or null
     */
    public DockingRegion getRegion() {
        if (dockableCenter.isMouseOver()) {
            return DockingRegion.CENTER;
        }
        if (dockableEast.isMouseOver()) {
            return DockingRegion.EAST;
        }
        if (dockableWest.isMouseOver()) {
            return DockingRegion.WEST;
        }
        if (dockableNorth.isMouseOver()) {
            return DockingRegion.NORTH;
        }
        if (dockableSouth.isMouseOver()) {
            return DockingRegion.SOUTH;
        }
        return null;
    }
}