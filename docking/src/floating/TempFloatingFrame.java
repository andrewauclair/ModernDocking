package floating;

import docking.Dockable;

import javax.swing.*;
import java.awt.*;

// this is a frame used temporarily when floating a panel
public class TempFloatingFrame extends JFrame {
	public TempFloatingFrame(Dockable dockable, JComponent dragSrc, Point mouseDragPos) {
		setLayout(new BorderLayout());

		setSize(((JComponent) dockable).getSize());

		Point newPoint = new Point(mouseDragPos);
		SwingUtilities.convertPointToScreen(newPoint, dragSrc);

		newPoint.x -= mouseDragPos.x;
		newPoint.y -= mouseDragPos.y;

		setLocation(newPoint);

		add((JComponent) dockable, BorderLayout.CENTER);

		setUndecorated(true);

		setVisible(true);
	}
}
