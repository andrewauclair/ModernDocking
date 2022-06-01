package floating;

import docking.Dockable;

import javax.swing.*;
import java.awt.*;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandlesFrame extends JFrame {
	private Dockable target;

	public DockingHandlesFrame() {
		setUndecorated(true);

		add(new JLabel("Test"));

		setBackground(new Color(0, 0, 0, 0));

		setSize(600, 600);
	}

	public void setTarget(Dockable dockable) {
		target = dockable;
	}
}
