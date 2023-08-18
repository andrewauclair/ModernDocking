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
package basic;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;

public class FloatingSample extends JFrame {
    private static class DockingPanel extends JPanel implements Dockable {
        private final String id;

        public DockingPanel(String id) {
            this.id = id;

            JButton floatPanel = new JButton("Float This Panel");

            floatPanel.addActionListener(e -> {
                Point location = getLocationOnScreen();
                location.x += 250;
                Docking.newWindow(this, location, new Dimension(150, 150));
            });

            add(floatPanel);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public String getTabText() {
            return id;
        }

        @Override
        public boolean getFloatingAllowed() {
            return true;
        }

        @Override
        public boolean getCanBeClosed() {
            return false;
        }
    };

    public FloatingSample() {
        setSize(300, 200);

        Docking.initialize(this);

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DockingPanel alpha = new DockingPanel("Alpha");
        DockingPanel bravo = new DockingPanel("Bravo");

        Docking.registerDockable(alpha);
        Docking.registerDockable(bravo);

        Docking.dock(alpha, this);
        Docking.dock(bravo, alpha, DockingRegion.WEST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FloatingSample().setVisible(true));
    }
}
