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

import ModernDocking.internal.CustomTabbedPane;
import ModernDocking.ui.DockingSettings;

import javax.swing.*;
import java.awt.*;

public class DockingTabOverlay extends DockingOverlayInterface {
    private final JFrame parent;
    private final CustomTabbedPane tabbedPane;
    // the top left location where the overlay starts
    private Point location = new Point(0, 0);
    // the total size of the overlay, used for drawing
    private Dimension size;

    private Rectangle targetTab;

    public DockingTabOverlay(JFrame parent, CustomTabbedPane tabbedPane, Point mousePosOnScreen) {
        this.parent = parent;
        this.tabbedPane = tabbedPane;
        updateOverlay(mousePosOnScreen);
    }

    public void updateOverlay(Point mousePosOnScreen) {
        Component componentAt = tabbedPane.getComponentAt(0);

        location = componentAt.getLocation();
        SwingUtilities.convertPointToScreen(location, tabbedPane);
        SwingUtilities.convertPointFromScreen(location, parent);

        size = componentAt.getSize();

        int targetTabIndex = tabbedPane.getTargetTabIndex(mousePosOnScreen, true);

        if (targetTabIndex != -1) {
            targetTab = tabbedPane.getBoundsAt(targetTabIndex);

            Point p = new Point(targetTab.x, targetTab.y);
            SwingUtilities.convertPointToScreen(p, tabbedPane);
            SwingUtilities.convertPointFromScreen(p, parent);

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

            SwingUtilities.convertPointFromScreen(boundsPoint, parent);

            targetTab.x = boundsPoint.x + widthToAdd;
            targetTab.y = boundsPoint.y;
        }
    }

    /**
     * Paint the docking overlay if visible
     *
     * @param g Graphics to use for painting
     */
    public void paint(Graphics g) {
        g.setColor(DockingSettings.getOverlayBackground());
        g.fillRect(location.x, location.y, size.width, size.height);
        g.fillRect(targetTab.x, targetTab.y, targetTab.width, targetTab.height);
    }
}
