package docking.listener;

import docking.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FloatListener extends MouseAdapter implements AWTEventListener {
	private final DockableWrapper dockable;

	private boolean mouseDown = false;
	private boolean mouseDragging = false;
	private boolean floating = false;

	private Timer floatMouseTimer = null;

	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	public FloatListener(DockableWrapper dockable) {
		this.dockable = dockable;

		// TODO need to remove these somewhere I think
		this.dockable.getDockable().dragSource().addMouseListener(this);
		this.dockable.getDockable().dragSource().addMouseMotionListener(this);

		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
	}

	private void removeListeners() {
		dockable.getDockable().dragSource().removeMouseListener(this);
		dockable.getDockable().dragSource().removeMouseMotionListener(this);
		Toolkit.getDefaultToolkit().removeAWTEventListener(this);

		dockable.removedListeners();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
		mouseDragging = false;
		floating = false;

//		dropFloatingPanel();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// create a timer if we're floating and don't have a timer already
		if (floating && floatMouseTimer == null) {
			// update the position of the floating frame every 30ms
			floatMouseTimer = new Timer(30, e1 -> {
				if (floating) {
					Point point = MouseInfo.getPointerInfo().getLocation();
					point.x -= dragOffset.x;
					point.y -= dragOffset.y;

					floatingFrame.setLocation(point);
				}
			});
			floatMouseTimer.start();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!mouseDragging) {
			dragOffset = e.getPoint();

			floatingFrame = new TempFloatingFrame(dockable.getDockable(), dockable.getDockable().dragSource(), e.getPoint());

			Docking.undock(dockable.getDockable());

			floating = true;
		}
		else {
			Point newPoint = e.getPoint();
			SwingUtilities.convertPointToScreen(newPoint, dockable.getDockable().dragSource());
			floatingFrame.setLocation(newPoint);
		}
		mouseDragging = true;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dropFloatingPanel();
	}

	private void dropFloatingPanel() {
		if (floatMouseTimer != null) {
			floatMouseTimer.stop();
			floatMouseTimer = null;
		}

		if (floating) {
			floating = false;
			mouseDragging = false;
			mouseDown = false;

			Point point = MouseInfo.getPointerInfo().getLocation();
			JFrame frame = Docking.findRootAtScreenPos(point);

			// Docking will add new listeners, we must remove ours here
			removeListeners();

			if (frame != null) {
				// TODO figure out a region, center 65% and outer 35%
				Docking.dock(frame, dockable.getDockable());
			}
			else {
				new FloatingFrame(dockable.getDockable(), floatingFrame);
			}

			floatingFrame.dispose();
			floatingFrame = null;
		}
	}

	@Override
	public void eventDispatched(AWTEvent event) {
		if (event.getID() == MouseEvent.MOUSE_RELEASED) {
			System.out.println("mouse released");
			dropFloatingPanel();
		}
	}
}
