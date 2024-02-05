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
import ModernDocking.DockableStyle;
import ModernDocking.DockingRegion;
import ModernDocking.api.RootDockingPanelAPI;

import javax.swing.*;
import java.awt.*;

import static ModernDocking.floating.DockingHandle.HANDLE_ICON_SIZE;

public class RootDockingHandles {
    private final DockingHandle rootCenter = new DockingHandle(DockingRegion.CENTER, true);
    private final DockingHandle rootWest = new DockingHandle(DockingRegion.WEST, true);
    private final DockingHandle rootNorth = new DockingHandle(DockingRegion.NORTH, true);
    private final DockingHandle rootEast = new DockingHandle(DockingRegion.EAST, true);
    private final DockingHandle rootSouth = new DockingHandle(DockingRegion.SOUTH, true);

    private final DockingHandle pinWest = new DockingHandle(DockingRegion.WEST);
    private final DockingHandle pinEast = new DockingHandle(DockingRegion.EAST);
    private final DockingHandle pinSouth = new DockingHandle(DockingRegion.SOUTH);

    private final JFrame frame;
    private final RootDockingPanelAPI rootPanel;

    public RootDockingHandles(JFrame frame, RootDockingPanelAPI rootPanel) {
        this.frame = frame;
        this.rootPanel = rootPanel;
        setupHandle(frame, rootCenter);
        setupHandle(frame, rootWest);
        setupHandle(frame, rootNorth);
        setupHandle(frame, rootEast);
        setupHandle(frame, rootSouth);

        setupHandle(frame, pinWest);
        setupHandle(frame, pinEast);
        setupHandle(frame, pinSouth);

        // invoke later to wait for the root panel to have a parent
        SwingUtilities.invokeLater(this::setRootHandleLocations);
    }

    public void setVisible(boolean visible) {
//        rootEast.setVisible(true);
    }

    public void setTargetDockable(Dockable dockable) {
        if (dockable == null) {
            pinWest.setVisible(false);
            pinEast.setVisible(false);
            pinSouth.setVisible(false);
            return;
        }

        pinWest.setVisible(dockable.isPinningAllowed() && (dockable.getPinningStyle() == DockableStyle.BOTH || dockable.getPinningStyle() == DockableStyle.VERTICAL));
        pinEast.setVisible(dockable.isPinningAllowed() && (dockable.getPinningStyle() == DockableStyle.BOTH || dockable.getPinningStyle() == DockableStyle.VERTICAL));
        pinSouth.setVisible(dockable.isPinningAllowed() && (dockable.getPinningStyle() == DockableStyle.BOTH || dockable.getPinningStyle() == DockableStyle.HORIZONTAL));
    }

    public void mouseMoved(Point mousePosOnScreen) {
        Point framePoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(framePoint, frame);

        rootCenter.mouseMoved(framePoint);
        rootWest.mouseMoved(framePoint);
        rootNorth.mouseMoved(framePoint);
        rootEast.mouseMoved(framePoint);
        rootSouth.mouseMoved(framePoint);

        pinWest.mouseMoved(framePoint);
        pinEast.mouseMoved(framePoint);
        pinSouth.mouseMoved(framePoint);

        frame.revalidate();
        frame.repaint();
    }

    private void setupHandle(JFrame frame, DockingHandle label) {
        label.setVisible(false);
        frame.add(label);
    }

    private void setRootHandleLocations() {
        Point location = rootPanel.getLocation();
        Dimension size = rootPanel.getSize();
        location.x += size.width / 2;
        location.y += size.height / 2;

        SwingUtilities.convertPointToScreen(location, rootPanel.getParent());
        SwingUtilities.convertPointFromScreen(location, rootPanel);

        setLocation(rootCenter, location.x, location.y);
        setLocation(rootWest, location.x - (size.width / 2) + rootHandleSpacing(rootWest), location.y);
        setLocation(rootNorth, location.x, location.y - (size.height / 2) + rootHandleSpacing(rootNorth));
        setLocation(rootEast, location.x + (size.width / 2) - rootHandleSpacing(rootEast), location.y);
        setLocation(rootSouth, location.x, location.y + (size.height / 2) - rootHandleSpacing(rootSouth));

        setLocation(pinWest, location.x - (size.width / 2) + rootHandleSpacing(pinWest), location.y - (size.height / 3));
        setLocation(pinEast, location.x + (size.width / 2) - rootHandleSpacing(pinEast), location.y - (size.height / 3));
        setLocation(pinSouth, location.x - (size.width / 3), location.y + (size.height / 2) - rootHandleSpacing(pinSouth));
    }

    private int rootHandleSpacing(JLabel handle) {
        return handle.getWidth() + 16;
    }

    private boolean isPinningRegionAllowed(Dockable dockable, DockingRegion region) {
        return false;
//        if (floating instanceof DockedTabbedPanel) {
//            return false;
//        }
//        Dockable floating = ((DisplayPanel) this.floating).getWrapper().getDockable();
//
//        if (!floating.isPinningAllowed()) {
//            return false;
//        }
//
//        if (floating.getPinningStyle() == DockableStyle.BOTH) {
//            return true;
//        }
//        if (region == DockingRegion.NORTH || region == DockingRegion.SOUTH) {
//            return floating.getPinningStyle() == DockableStyle.HORIZONTAL;
//        }
//        return floating.getPinningStyle() == DockableStyle.VERTICAL;
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
        Graphics2D g2 = (Graphics2D) g.create();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        g2.setStroke(dashed);

        // draw root handles
        rootCenter.paintHandle(g, g2);
        rootEast.paintHandle(g, g2);
        rootWest.paintHandle(g, g2);
        rootNorth.paintHandle(g, g2);
        rootSouth.paintHandle(g, g2);

        pinWest.paintHandle(g, g2);
        pinEast.paintHandle(g, g2);
        pinSouth.paintHandle(g, g2);

        g2.dispose();
    }
}
