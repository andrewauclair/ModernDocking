package docking;

import javax.swing.*;
import java.awt.*;

public class FloatingFrame extends JFrame {
	public FloatingFrame(Dockable dockable, TempFloatingFrame floatingFrame) {
		setLayout(new BorderLayout());
		setSize(((JComponent) dockable).getSize());

		Point location = floatingFrame.getLocation();
		location.x -= Docking.frameBorderSize.width;
		location.y -= Docking.frameBorderSize.height;

		setLocation(location);

		RootDockingPanel root = new RootDockingPanel();
		add(root, BorderLayout.CENTER);

		Docking.registerDockingPanel(root, this);

		Docking.dock(this, dockable);

		setVisible(true);
	}
}
