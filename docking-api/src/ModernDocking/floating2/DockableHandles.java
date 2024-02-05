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
package ModernDocking.floating2;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.ui.DockingSettings;

import javax.swing.*;
import java.awt.*;

import static ModernDocking.floating.DockingHandle.HANDLE_ICON_SIZE;

public class DockableHandles {
    private final DockingHandle dockableCenter = new DockingHandle(DockingRegion.CENTER, false);
    private final DockingHandle dockableWest = new DockingHandle(DockingRegion.WEST, false);
    private final DockingHandle dockableNorth = new DockingHandle(DockingRegion.NORTH, false);
    private final DockingHandle dockableEast = new DockingHandle(DockingRegion.EAST, false);
    private final DockingHandle dockableSouth = new DockingHandle(DockingRegion.SOUTH, false);
    private final JFrame frame;
    private final Dockable dockable;

    public DockableHandles(JFrame frame, Dockable dockable) {
        this.frame = frame;
        this.dockable = dockable;

        setupHandle(frame, dockableCenter);
        setupHandle(frame, dockableWest);
        setupHandle(frame, dockableNorth);
        setupHandle(frame, dockableEast);
        setupHandle(frame, dockableSouth);

        setDockableHandleLocations();
    }

    public void mouseMoved(Point mousePosOnScreen) {
        Point framePoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(framePoint, frame);

        dockableCenter.mouseMoved(framePoint);
        dockableWest.mouseMoved(framePoint);
        dockableNorth.mouseMoved(framePoint);
        dockableEast.mouseMoved(framePoint);
        dockableSouth.mouseMoved(framePoint);

        frame.revalidate();
        frame.repaint();
    }

    private void setupHandle(JFrame frame, DockingHandle label) {
        label.setVisible(false);
        frame.add(label);
    }

    private void setDockableHandleLocations() {
        dockableCenter.setVisible(true);
        dockableWest.setVisible(isRegionAllowed(DockingRegion.WEST));
        dockableNorth.setVisible(isRegionAllowed(DockingRegion.NORTH));
        dockableEast.setVisible(isRegionAllowed(DockingRegion.EAST));
        dockableSouth.setVisible(isRegionAllowed(DockingRegion.SOUTH));

        if (((Component) dockable).getParent() != null) {
            Point location = ((Component) dockable).getLocation();
            Dimension size = ((Component) dockable).getSize();

            // if this dockable is wrapped in a JScrollPane we need to set the handle to the center of the JScrollPane
            // not to the center of the dockable (which will more than likely be at a different location)
            if (dockable.isWrappableInScrollpane()) {
                Component parent = ((Component) dockable).getParent();

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

            SwingUtilities.convertPointToScreen(location, ((Component) dockable).getParent());
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

    private boolean isRegionAllowed(DockingRegion region) {
//        if (floating instanceof DockedSimplePanel) {
//            DockedSimplePanel panel = (DockedSimplePanel) this.floating;
//            Dockable floating = panel.getWrapper().getDockable();
//
//            if (floating.getStyle() == DockableStyle.BOTH) {
//                return true;
//            }
//            if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
//                return floating.getStyle() == DockableStyle.HORIZONTAL;
//            }
//            return floating.getStyle() == DockableStyle.VERTICAL;
//        }
        return true;
    }

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

        // draw the dockable handles background over the root handles in case they overlap
        // fill the dockable handles background
        g.setColor(background);
        g.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);

        // draw the dockable handles border
        g.setColor(border);
        g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);

        // draw the docking handles over the docking handles background
        dockableCenter.paintHandle(g, g2);
        dockableEast.paintHandle(g, g2);
        dockableWest.paintHandle(g, g2);
        dockableNorth.paintHandle(g, g2);
        dockableSouth.paintHandle(g, g2);

        g2.dispose();
    }

    private int handleSpacing(JLabel handle) {
        return handle.getWidth() + 8;
    }
}
