/*
Copyright (c) 2022 Andrew Auclair

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
package floating;

import docking.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class FloatListener extends MouseAdapter implements WindowListener {
	private final DockableWrapper dockable;

	private boolean mouseDragging = false;
	private boolean floating = false;

	private Timer floatMouseTimer = null;

	private Point mousePressedLocation = new Point(0, 0);
	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final DockingHandlesFrame dockingHandles = new DockingHandlesFrame();
	private static final DockingOverlayFrame dockingOverlay = new DockingOverlayFrame();

	private static JFrame frameToDispose = null;

	private static final List<JFrame> framesBroughtToFront = new ArrayList<>();

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
		mousePressedLocation = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("mouseReleased");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
//		createTimer();
	}

	private void createTimer() {
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

					if (frame != null && !framesBroughtToFront.contains(frame)) {
						frame.toFront();
						floatingFrame.toFront();
						dockingHandles.toFront();
						dockingOverlay.toFront();

						framesBroughtToFront.add(frame);
					}
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

			dockable.getParent().undock(dockable.getDockable());

			// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
			Point mousePos = new Point(mousePressedLocation);
			SwingUtilities.convertPointToScreen(mousePos, dockable.getDockable().dragSource());

			JFrame frame = Docking.findRootAtScreenPos(mousePos);
			RootDockingPanel root = Docking.rootForFrame(frame);

			if (frame != null && root != null && root.getPanel() == null && Docking.canDisposeFrame(frame)) {
				frameToDispose = frame;
				frameToDispose.setVisible(false);
			}

			dockable.getParent().revalidate();
			dockable.getParent().repaint();

			floatingFrame.addWindowListener(this);
			dockingHandles.addWindowListener(this);
			dockingOverlay.addWindowListener(this);

			floating = true;

			createTimer();
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
			RootDockingPanel root = Docking.rootForFrame(frame);

			DockingPanel dockingPanel = Docking.findDockingPanelAtScreenPos(point);

			// Docking will add new listeners, we must remove ours here
			removeListeners();

			if (root != null && dockingOverlay.isDockingToRoot()) {
				root.getPanel().dock(dockable.getDockable(), dockingOverlay.getRegion(mousePos));
			}
			else if (frame != null && dockingPanel != null) {
				dockingPanel.dock(dockable.getDockable(), dockingOverlay.getRegion(mousePos));
			}
			else {
				new FloatingFrame(dockable.getDockable(), floatingFrame);
			}

			if (frameToDispose != null) {
				frameToDispose.dispose();
				frameToDispose = null;
			}

			floatingFrame.dispose();
			floatingFrame = null;

			dockingHandles.dockingComplete();
			dockingOverlay.dockingComplete();

			framesBroughtToFront.clear();

			SwingUtilities.invokeLater(() -> {
				dockingHandles.setVisible(false);
				dockingOverlay.setVisible(false);
			});
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// window was deactivated and another app has taken focus, stop floating the panel, drop it where it is
		if (e.getOppositeWindow() == null) {
			dropFloatingPanel();
		}
	}
}
