package floating;

import docking.Dockable;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;

// handles displaying the handles for docking overlaid on the application
// only displayed over the currently hit docking panel
public class DockingHandlesFrame extends JFrame {
	private Dockable targetDockable;
	private JFrame targetFrame;
	private RootDockingPanel targetRoot;

	// TODO turn these into icons
	private final JLabel rootWest = new JLabel("RW");
	private final JLabel rootNorth = new JLabel("RN");
	private final JLabel rootEast = new JLabel("RE");
	private final JLabel rootSouth = new JLabel("RS");

	private final JLabel dockableCenter = new JLabel("DC");
	private final JLabel dockableWest = new JLabel("DW");
	private final JLabel dockableNorth = new JLabel("DN");
	private final JLabel dockableEast = new JLabel("DE");
	private final JLabel dockableSouth = new JLabel("DS");

	public DockingHandlesFrame() {
		setLayout(null);

		setUndecorated(true);

		rootWest.setVisible(false);
		rootNorth.setVisible(false);
		rootEast.setVisible(false);
		rootSouth.setVisible(false);

		dockableCenter.setVisible(false);
		dockableWest.setVisible(false);
		dockableNorth.setVisible(false);
		dockableEast.setVisible(false);
		dockableSouth.setVisible(false);

		rootWest.setBounds(0, 0, 18, 18);
		rootNorth.setBounds(0, 0, 18, 18);
		rootEast.setBounds(0, 0, 18, 18);
		rootSouth.setBounds(0, 0, 18, 18);

		dockableCenter.setBounds(0, 0, 18, 18);
		dockableWest.setBounds(0, 0, 18, 18);
		dockableNorth.setBounds(0, 0, 18, 18);
		dockableEast.setBounds(0, 0, 18, 18);
		dockableSouth.setBounds(0, 0, 18, 18);

		add(rootWest);
		add(rootNorth);
		add(rootEast);
		add(rootSouth);
		add(dockableCenter);

		add(dockableWest);
		add(dockableNorth);
		add(dockableEast);
		add(dockableSouth);

		setBackground(new Color(255, 0, 0, 10));
	}

	// set the root of the target frame. Allows the user to always dock to the outer edges of the frame
	public void setRoot(JFrame frame, RootDockingPanel root) {
		targetFrame = frame;
		targetRoot = root;
	}

	// set the specific Dockable target which we'll show a basic handle in the center of
	public void setTarget(Dockable dockable) {
		targetDockable = dockable;

		dockableCenter.setVisible(targetDockable != null);
		dockableWest.setVisible(targetDockable != null);
		dockableNorth.setVisible(targetDockable != null);
		dockableEast.setVisible(targetDockable != null);
		dockableSouth.setVisible(targetDockable != null);

		if (targetDockable != null) {
			Point location = ((Component) dockable).getLocation();
			Rectangle bounds = ((Component) dockable).getBounds();
			location.x += bounds.width / 2;
			location.y += bounds.height / 2;

			SwingUtilities.convertPointToScreen(location, (Component) dockable);

			SwingUtilities.convertPointFromScreen(location, this);
			dockableCenter.setLocation(location);
			dockableWest.setLocation(location.x - 20, location.y);
			dockableNorth.setLocation(location.x, location.y - 20);
			dockableEast.setLocation(location.x + 20, location.y);
			dockableSouth.setLocation(location.x, location.y + 20);
		}
	}

	public void update(Point screenPos) {
		if (targetDockable == null) {
			return;
		}

		JComponent component = (JComponent) targetDockable;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component);

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		SwingUtilities.convertPointToScreen(point, component);

		setLocation(point);
		setSize(size);
	}
}
