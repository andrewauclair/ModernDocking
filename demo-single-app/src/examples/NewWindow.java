package examples;

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.ui.DefaultDockingPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
