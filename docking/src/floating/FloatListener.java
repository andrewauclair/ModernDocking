package floating;

import docking.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FloatListener extends MouseAdapter {
	private final DockableWrapper dockable;

	private boolean mouseDragging = false;
	private boolean floating = false;

	private Timer floatMouseTimer = null;

	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final DockingHandlesFrame dockingHandles = new DockingHandlesFrame();
	private static final DockingOverlayFrame dockingOverlay = new DockingOverlayFrame();

	public FloatListener(DockableWrapper dockable) {
		this.dockable = dockable;

		this.dockable.getDockable().dragSource().addMouseListener(this);
		this.dockable.getDockable().dragSource().addMouseMotionListener(this);
	}

	private void removeListeners() {
		dockable.getDockable().dragSource().removeMouseListener(this);
		dockable.getDockable().dragSource().removeMouseMotionListener(this);

		dockable.removedListeners();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// create a timer if we're floating and don't have a timer already
		if (floating && floatMouseTimer == null) {
			// update the position of the floating frame every 30ms
			floatMouseTimer = new Timer(30, e1 -> {
				if (floating) {
					Point mousePos = MouseInfo.getPointerInfo().getLocation();

					Point framePos = MouseInfo.getPointerInfo().getLocation();
					framePos.x -= dragOffset.x;
					framePos.y -= dragOffset.y;

					floatingFrame.setLocation(framePos);

					JFrame frame = Docking.findRootAtScreenPos(mousePos);
					RootDockingPanel root = Docking.rootForFrame(frame);

					Dockable dockable = Docking.findDockableAtScreenPos(mousePos);

					dockingHandles.setRoot(frame, root);
					dockingHandles.setTarget(dockable);
					dockingHandles.update(mousePos);

					dockingOverlay.setRoot(root);
					dockingOverlay.setTargetDockable(dockable);
					dockingOverlay.setTargetRootRegion(dockingHandles.getRootRegion());
					dockingOverlay.setTargetDockableRegion(dockingHandles.getDockableRegion());
					dockingOverlay.update(mousePos);
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

			dockingOverlay.setFloating(dockable.getDockable());

			dockingHandles.setVisible(true);
			dockingOverlay.setVisible(true);

			Docking.undock(dockable.getDockable());

			floating = true;
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

			Point mousePos = MouseInfo.getPointerInfo().getLocation();

			Point point = MouseInfo.getPointerInfo().getLocation();
			JFrame frame = Docking.findRootAtScreenPos(point);

			DockingPanel dockingPanel = Docking.findDockingPanelAtScreenPos(point);

			// Docking will add new listeners, we must remove ours here
			removeListeners();

			if (frame != null && dockingPanel != null) {
//				Docking.dock(frame, dockable.getDockable(), dockingOverlay.getRegion(mousePos));

				dockingPanel.dock(dockable.getDockable(), dockingOverlay.getRegion(mousePos));
			}
			else {
				new FloatingFrame(dockable.getDockable(), floatingFrame);
			}

			floatingFrame.dispose();
			floatingFrame = null;

			dockingHandles.dockingComplete();
			dockingOverlay.dockingComplete();

			SwingUtilities.invokeLater(() -> {
				dockingHandles.setVisible(false);
				dockingOverlay.setVisible(false);
			});
		}
	}
}
