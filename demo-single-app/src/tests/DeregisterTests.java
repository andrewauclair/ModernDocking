/*
Copyright (c) 2023 Andrew Auclair

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
package tests;

import ModernDocking.app.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.ui.DefaultDockingPanel;
import ModernDocking.ext.ui.DockingUI;

import javax.swing.*;
import java.awt.*;

public class DeregisterTests extends JFrame {
    public DeregisterTests() {
        setSize(300, 200);

        Docking.initialize(this);
        DockingUI.initialize();

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DefaultDockingPanel one = new DefaultDockingPanel("one", "One");
        DefaultDockingPanel two = new DefaultDockingPanel("two", "Two");

        Docking.registerDockable(one);
        Docking.registerDockable(two);

        JButton openOneInNewWindow = new JButton("Open in new window");
        openOneInNewWindow.addActionListener(e -> Docking.newWindow("one"));
        one.add(openOneInNewWindow);

        JButton openTwoInNewWindow = new JButton("Open in new window");
        openTwoInNewWindow.addActionListener(e -> Docking.newWindow(two));
        two.add(openTwoInNewWindow);

        Docking.dock(one, this);
        Docking.dock(two, one, DockingRegion.WEST);

        JMenuItem deregisterOne = new JMenuItem("Deregister One");
        deregisterOne.addActionListener(e -> Docking.deregisterDockable(one));

        JMenuItem deregisterDockables = new JMenuItem("Deregister All Dockables");
        deregisterDockables.addActionListener(e -> Docking.deregisterAllDockables());

        JMenuItem registerOne = new JMenuItem("Register One");
        registerOne.addActionListener(e -> {
            Docking.registerDockable(one);
            if (Docking.isDocked(two)) {
                Docking.dock(one, two, DockingRegion.EAST);
            }
            else {
                Docking.dock(one, this);
            }
        });

        JMenuItem registerDockables = new JMenuItem("Register Dockables");
        registerDockables.addActionListener(e -> {
            Docking.registerDockable(one);
            Docking.registerDockable(two);

            Docking.dock(one, this);
            Docking.dock(two, one, DockingRegion.WEST);
        });

        JMenu testing = new JMenu("Testing");
        testing.add(deregisterOne);
        testing.add(deregisterDockables);
        testing.add(registerOne);
        testing.add(registerDockables);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(testing);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DeregisterTests().setVisible(true));
    }
}
