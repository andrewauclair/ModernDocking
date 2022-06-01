package floating;

import docking.Dockable;
import docking.Docking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// displays the overlay highlight of where the panel will be docked
public class DockingOverlayFrame extends JFrame implements MouseMotionListener, MouseListener {
	private Dockable target;
	private Dockable floating;

	public DockingOverlayFrame() {
		setUndecorated(true);

		setBackground(new Color(0, 0, 100, 30));

		setSize(1, 1);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	public void setTarget(Dockable dockable) {
		target = dockable;

		setVisible(target != null);
	}

	public void update(Point position) {
		if (target == null) {
			return;
		}
		JComponent component = (JComponent) target;

		Point point = (component).getLocation();

		SwingUtilities.convertPointToScreen(point, component);

		setLocation(point);
		setSize((component).getSize());
	}

	// we don't want to use the mouse events in this overlay frame because that would break the app
	// pass them off to the component that we really need them in, the drag source
	private void dispatchEvent(MouseEvent e) {
		if (floating != null) {
			floating.dragSource().dispatchEvent(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		dispatchEvent(e);
	}
}
