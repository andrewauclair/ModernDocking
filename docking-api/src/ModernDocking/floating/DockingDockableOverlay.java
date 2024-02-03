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
import ModernDocking.api.DockingAPI;
import ModernDocking.internal.CustomTabbedPane;
import ModernDocking.internal.DockingInternal;
import ModernDocking.ui.DockingSettings;

import javax.swing.*;
import java.awt.*;

import static ModernDocking.DockingRegion.*;

public class DockingDockableOverlay extends DockingOverlayInterface {
    private final DockingAPI docking;
    private final JFrame parent;

    // the top left location where the overlay starts
    private Point location = new Point(0, 0);
    // the total size of the overlay, used for drawing
    private Dimension size = new Dimension();

    public DockingDockableOverlay(DockingAPI docking, JFrame parent, Dockable targetDockable, Point mousePosOnScreen) {
        this.docking = docking;
        this.parent = parent;
        updateOverlay(targetDockable, mousePosOnScreen);
    }

    public void updateOverlay(Dockable targetDockable, Point mousePosOnScreen) {
        if (targetDockable == null) {
            System.out.println("reset");
            size = new Dimension();
            return;
        }
        JComponent component = DockingInternal.get(docking).getWrapper(targetDockable).getDisplayPanel();

        Point point = component.getLocation();
        Dimension size = component.getSize();

        point = SwingUtilities.convertPoint(component.getParent(), point, parent);

//        lastSelectedRegion = dockableRegion;

        final double DROP_SIZE = 2;

//        switch (dockableRegion) {
//            case WEST: {
//                size = new Dimension((int) (size.width / DROP_SIZE), size.height);
//                break;
//            }
//            case NORTH: {
//                size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
//                break;
//            }
//            case EAST: {
//                point.x += size.width / DROP_SIZE;
//                size = new Dimension((int) (size.width / DROP_SIZE), size.height);
//                break;
//            }
//            case SOUTH: {
//                point.y += size.height / DROP_SIZE;
//                size = new Dimension(size.width, (int) (size.height / DROP_SIZE));
//                break;
//            }
//        }

        this.location = point;
        this.size = size;
    }

    /**
     * Paint the docking overlay if visible
     *
     * @param g Graphics to use for painting
     */
    public void paint(Graphics g) {
        g.setColor(DockingSettings.getOverlayBackground());
        g.fillRect(location.x, location.y, size.width, size.height);
//        g.fillRect(targetTab.x, targetTab.y, targetTab.width, targetTab.height);
    }
}
