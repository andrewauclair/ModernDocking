package examples;

import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;
import ModernDocking.ui.DefaultDockingPanel;
import docking.ui.DockingUI;

import javax.swing.*;
import java.awt.*;

public class NewWindow extends JFrame {
    public NewWindow() {
        setSize(300, 200);

        DockingUI.initialize();

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DefaultDockingPanel one = new DefaultDockingPanel("one", "One");
        DefaultDockingPanel two = new DefaultDockingPanel("two", "Two");

        JButton openOneInNewWindow = new JButton("Open in new window");
        openOneInNewWindow.addActionListener(e -> Docking.newWindow("one"));
        one.add(openOneInNewWindow);

        JButton openTwoInNewWindow = new JButton("Open in new window");
        openTwoInNewWindow.addActionListener(e -> Docking.newWindow(two));
        two.add(openTwoInNewWindow);

        Docking.dock(one, this);
        Docking.dock(two, one, DockingRegion.WEST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NewWindow().setVisible(true));
    }
}
