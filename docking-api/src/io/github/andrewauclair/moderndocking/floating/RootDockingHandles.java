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
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import static io.github.andrewauclair.moderndocking.floating.DockingHandle.HANDLE_ICON_SIZE;

/**
 * Collection of the handles for the root of a window
 */
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
    private final InternalRootDockingPanel rootPanel;

    private DockingRegion mouseOverRegion = null;
    private DockingRegion mouseOverPin = null;

    /**
     * Create a new instance of the root docking handles
     *
     * @param frame The frame this root docking handle belongs to
     * @param rootPanel The root panel for the frame
     */
    public RootDockingHandles(JFrame frame, InternalRootDockingPanel rootPanel) {
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

    /**
     * Update the handle positions within the frame
     */
    public void updateHandlePositions() {
        setRootHandleLocations();
    }

    /**
     * Change the dockable that is being floated
     *
     * @param dockable New floating dockable or null
     */
    public void setFloatingDockable(Dockable dockable) {
        if (dockable == null) {
            pinWest.setVisible(false);
            pinEast.setVisible(false);
            pinSouth.setVisible(false);
            return;
        }

        rootCenter.setVisible(rootPanel.isEmpty());

        pinWest.setVisible(dockable.isAutoHideAllowed() && (dockable.getAutoHideStyle() == DockableStyle.BOTH || dockable.getAutoHideStyle() == DockableStyle.VERTICAL));
        pinEast.setVisible(dockable.isAutoHideAllowed() && (dockable.getAutoHideStyle() == DockableStyle.BOTH || dockable.getAutoHideStyle() == DockableStyle.VERTICAL));
        pinSouth.setVisible(dockable.isAutoHideAllowed() && (dockable.getAutoHideStyle() == DockableStyle.BOTH || dockable.getAutoHideStyle() == DockableStyle.HORIZONTAL));
    }

    /**
     * The mouse has moved on screen, and we need to check its position against all the root docking handles
     *
     * @param mousePosOnScreen New mouse position on screen
     */
    public void mouseMoved(Point mousePosOnScreen) {
        Point framePoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(framePoint, frame);

        rootCenter.mouseMoved(framePoint);
        rootWest.mouseMoved(framePoint);
        rootNorth.mouseMoved(framePoint);
        rootEast.mouseMoved(framePoint);
        rootSouth.mouseMoved(framePoint);

        mouseOverRegion = null;
        if (rootCenter.isMouseOver()) mouseOverRegion = DockingRegion.CENTER;
        if (rootWest.isMouseOver()) mouseOverRegion = DockingRegion.WEST;
        if (rootNorth.isMouseOver()) mouseOverRegion = DockingRegion.NORTH;
        if (rootEast.isMouseOver()) mouseOverRegion = DockingRegion.EAST;
        if (rootSouth.isMouseOver()) mouseOverRegion = DockingRegion.SOUTH;

        pinWest.mouseMoved(framePoint);
        pinEast.mouseMoved(framePoint);
        pinSouth.mouseMoved(framePoint);

        mouseOverPin = null;
        if (pinWest.isMouseOver()) mouseOverPin = DockingRegion.WEST;
        if (pinEast.isMouseOver()) mouseOverPin = DockingRegion.EAST;
        if (pinSouth.isMouseOver()) mouseOverPin = DockingRegion.SOUTH;
    }

    private void setupHandle(JFrame frame, DockingHandle label) {
        label.setVisible(true);
        frame.add(label);
    }

    private void setRootHandleLocations() {
        Point location = rootPanel.getRootPanel().getLocation();
        Dimension size = rootPanel.getRootPanel().getSize();
        location.x += size.width / 2;
        location.y += size.height / 2;

        SwingUtilities.convertPointToScreen(location, rootPanel.getRootPanel().getParent());
        SwingUtilities.convertPointFromScreen(location, frame);

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

    private void setLocation(Component component, int x, int y) {
        component.setLocation(x - (HANDLE_ICON_SIZE / 2), y - (HANDLE_ICON_SIZE / 2));
    }

    /**
     * Paint the handles
     *
     * @param g2 Graphics2D instance to use
     */
    public void paint(Graphics2D g2) {
        // draw root handles
        rootCenter.paintHandle(g2);
        rootEast.paintHandle(g2);
        rootWest.paintHandle(g2);
        rootNorth.paintHandle(g2);
        rootSouth.paintHandle(g2);

        pinWest.paintHandle(g2);
        pinEast.paintHandle(g2);
        pinSouth.paintHandle(g2);
    }

    /**
     * Check if the mouse is over a root handle
     *
     * @return Is the mouse over a root handle?
     */
    public boolean isOverHandle() {
        return rootCenter.isMouseOver() ||
                rootEast.isMouseOver() ||
                rootWest.isMouseOver() ||
                rootNorth.isMouseOver() ||
                rootSouth.isMouseOver();
    }

    /**
     * Check if the mouse is over an auto-hide handle
     *
     * @return Is the mouse over an auto-hide handle?
     */
    public boolean isOverPinHandle() {
        return pinEast.isMouseOver() ||
                pinSouth.isMouseOver() ||
                pinWest.isMouseOver();
    }

    /**
     * Get the root region that the mouse is over
     *
     * @return Root region or null
     */
    public DockingRegion getRegion() {
        if (rootCenter.isMouseOver()) {
            return DockingRegion.CENTER;
        }
        if (rootEast.isMouseOver()) {
            return DockingRegion.EAST;
        }
        if (rootWest.isMouseOver()) {
            return DockingRegion.WEST;
        }
        if (rootNorth.isMouseOver()) {
            return DockingRegion.NORTH;
        }
        if (rootSouth.isMouseOver()) {
            return DockingRegion.SOUTH;
        }
        return null;
    }

    /**
     * Get the auto-hide region that the mouse is over
     *
     * @return Auto-hide region or null
     */
    public ToolbarLocation getPinRegion() {
        if (pinEast.isMouseOver()) {
            return ToolbarLocation.EAST;
        }
        else if (pinWest.isMouseOver()) {
            return ToolbarLocation.WEST;
        }
        else if (pinSouth.isMouseOver()) {
            return ToolbarLocation.SOUTH;
        }
        return null;
    }
}