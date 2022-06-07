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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloatListener extends MouseAdapter implements WindowListener {
	private final DockableWrapper dockable;

	private boolean mouseDragging = false;
	private boolean floating = false;

	private Timer floatMouseTimer = null;

	private Point mousePressedLocation = new Point(0, 0);
	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final Map<JFrame, DockingHandlesFrame> dockingHandles = new HashMap<>();
	private static final Map<JFrame, DockingOverlayFrame> dockingOverlays = new HashMap<>();

	private DockingHandlesFrame activeDockingHandles = null;
	private DockingOverlayFrame activeDockingOverlay = null;

	private static JFrame frameToDispose = null;

	private final List<JFrame> framesBroughtToFront = new ArrayList<>();

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

	public static void registerDockingFrame(JFrame frame, RootDockingPanel root) {
		dockingHandles.put(frame, new DockingHandlesFrame(frame, root));
		dockingOverlays.put(frame, new DockingOverlayFrame(frame, root));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressedLocation = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	private static int timerCount = 0;

	private JFrame currentTargetFrame = null;

	private void createTimer() {
		// create a timer if we're floating and don't have a timer already
		if (floating && floatMouseTimer == null) {
			final int DELAY_WINDOW_POSITIONING = 30;
			final int RATE_TO_UPDATE_OVERLAYS = 6;

			// update the position of the floating frame every 30ms
			floatMouseTimer = new Timer(DELAY_WINDOW_POSITIONING, e1 -> {
				long startTime = System.nanoTime();

				timerCount = (timerCount + 1) % RATE_TO_UPDATE_OVERLAYS;

				if (floating) {
					Point mousePos = MouseInfo.getPointerInfo().getLocation();

					Point framePos = new Point(mousePos);
					framePos.x -= dragOffset.x;
					framePos.y -= dragOffset.y;

					floatingFrame.setLocation(framePos);

					JFrame frame = Docking.findRootAtScreenPos(mousePos);

					// update overlays every DELAY_WINDOW_POSITIONING * RATE_TO_UPDATE_OVERLAYS ms
					if (timerCount == 0 || frame != currentTargetFrame) {
						currentTargetFrame = frame;

						if (frame != null && !framesBroughtToFront.contains(frame)) {
							frame.toFront();
							floatingFrame.toFront();

							if (activeDockingOverlay != null) {
								activeDockingOverlay.toFront();
							}
							if (activeDockingHandles != null) {
								activeDockingHandles.toFront();
							}

							framesBroughtToFront.add(frame);
						}
						Dockable dockable = Docking.findDockableAtScreenPos(mousePos);

						if (frame == null && activeDockingOverlay != null) {
							activeDockingOverlay.setActive(false);
							activeDockingOverlay = null;
						}
						else if (frame != null && activeDockingOverlay == null) {
							activeDockingOverlay = dockingOverlays.get(frame);
							activeDockingOverlay.setActive(true);
						}

						if (activeDockingOverlay != null && frame != activeDockingOverlay.getFrame()) {
							activeDockingOverlay.setActive(false);
							activeDockingOverlay = dockingOverlays.get(frame);
							activeDockingOverlay.setFloating(this.dockable.getDockable());
							activeDockingOverlay.setActive(true);
							activeDockingOverlay.toFront();
						}

						if (activeDockingOverlay != null) {
							activeDockingOverlay.setTargetDockable(dockable);
							activeDockingOverlay.setTargetRootRegion(activeDockingHandles == null ? null : activeDockingHandles.getRootRegion());
							activeDockingOverlay.setTargetDockableRegion(activeDockingHandles == null ? null : activeDockingHandles.getDockableRegion());
//							activeDockingOverlay.update(mousePos);
						}

						if (frame == null && activeDockingHandles != null) {
							activeDockingHandles.setActive(false);
							activeDockingHandles = null;
						}
						else if (frame != null && activeDockingHandles == null) {
							activeDockingHandles = dockingHandles.get(frame);
							activeDockingHandles.setActive(true);
						}

						if (activeDockingHandles != null && frame != activeDockingHandles.getFrame()) {
							activeDockingHandles.setActive(false);
							activeDockingHandles = dockingHandles.get(frame);
							activeDockingHandles.setFloating(this.dockable.getDockable());
							activeDockingHandles.setActive(true);
							activeDockingHandles.toFront();
						}

						if (activeDockingHandles != null) {
							activeDockingHandles.setTarget(dockable);
//							activeDockingHandles.update(mousePos);
						}
					}

					if (activeDockingOverlay != null) {
						activeDockingOverlay.update(mousePos);
					}
					if (activeDockingHandles != null) {
						activeDockingHandles.update(mousePos);
					}
				}

				long endTime = System.nanoTime();

				long duration = (endTime - startTime) / 1_000_000;

				if (duration > 20) {
					System.out.println("Timer update took: " + duration + "ms");
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

			dockable.getParent().undock(dockable.getDockable());

			// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
			Point mousePos = new Point(mousePressedLocation);
			SwingUtilities.convertPointToScreen(mousePos, dockable.getDockable().dragSource());

			JFrame frame = Docking.findRootAtScreenPos(mousePos);
			RootDockingPanel root = Docking.rootForFrame(frame);

			activeDockingHandles = dockingHandles.get(frame);
			activeDockingHandles.setActive(true);
			activeDockingHandles.setFloating(dockable.getDockable());

			activeDockingOverlay = dockingOverlays.get(frame);
			activeDockingOverlay.setActive(true);
			activeDockingOverlay.setFloating(dockable.getDockable());

			// TODO This doesn't seem to work if the frame to dispose ends up behind the target immediately
			if (frame != null && root != null && root.getPanel() == null && Docking.canDisposeFrame(frame)) {
				frameToDispose = frame;
				frameToDispose.setVisible(false);
			}

			dockable.getParent().revalidate();
			dockable.getParent().repaint();

			floatingFrame.addWindowListener(this);

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

			if (root != null && activeDockingOverlay.isDockingToRoot()) {
				root.dock(dockable.getDockable(), activeDockingOverlay.getRegion(mousePos));
			}
			else if (frame != null && dockingPanel != null) {
				dockingPanel.dock(dockable.getDockable(), activeDockingOverlay.getRegion(mousePos));
			}
			else if (root != null && frame != null) {
				root.dock(dockable.getDockable(), activeDockingOverlay.getRegion(mousePos));
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

			if (activeDockingHandles != null) {
				activeDockingHandles.setActive(false);
				activeDockingHandles = null;
			}

			if (activeDockingOverlay != null) {
				activeDockingOverlay.setActive(false);
				activeDockingOverlay = null;
			}

			framesBroughtToFront.clear();
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
