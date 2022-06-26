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
import persist.AppState;
import persist.RootDockState;

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
	// current floating dockable
	private final DockableWrapper floatingDockable;

	private boolean mouseDragging = false;
	private boolean floating = false;

	private Timer floatMouseTimer = null;

	private Point mousePressedLocation = new Point(0, 0);
	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final Map<JFrame, DockingUtilsFrame> utilFrames = new HashMap<>();

	private DockingUtilsFrame activeUtilsFrame = null;

	private static JFrame frameToDispose = null;

	private final List<JFrame> framesBroughtToFront = new ArrayList<>();

	private static int timerCount = 0;

	private JFrame currentTargetFrame = null;
	private JFrame originalFrame;

	private RootDockState rootState;

	public FloatListener(DockableWrapper dockable) {
		this.floatingDockable = dockable;

		if (this.floatingDockable.getDockable().dragSource() != null) {
			this.floatingDockable.getDockable().dragSource().addMouseListener(this);
			this.floatingDockable.getDockable().dragSource().addMouseMotionListener(this);
		}
	}

	public static void reset() {
		// used when creating a new Docking instance, mostly to hack the tests
//		dockingHandles.clear();
//		dockingOverlays.clear();
		utilFrames.clear();
		frameToDispose = null;
		timerCount = 0;
	}

	public void removeListeners() {
		if (floatingDockable.getDockable().dragSource() != null) {
			floatingDockable.getDockable().dragSource().removeMouseListener(this);
			floatingDockable.getDockable().dragSource().removeMouseMotionListener(this);
		}

		floatingDockable.removedListeners();
	}

	public static void registerDockingFrame(JFrame frame, RootDockingPanel root) {
		utilFrames.put(frame, new DockingUtilsFrame(frame, root));
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

					if (frame != currentTargetFrame) {
						changeFrameOverlays(frame);
					}

					// update overlays every DELAY_WINDOW_POSITIONING * RATE_TO_UPDATE_OVERLAYS ms
					if (timerCount == 0 || frame != currentTargetFrame) {
						currentTargetFrame = frame;

						if (frame != null && !framesBroughtToFront.contains(frame)) {
							if (activeUtilsFrame != null) {
								activeUtilsFrame.toFront();
							}

							framesBroughtToFront.add(frame);
						}
						Dockable dockable = Docking.findDockableAtScreenPos(mousePos);

						if (activeUtilsFrame != null) {
							activeUtilsFrame.setTargetDockable(dockable);
						}
					}

					if (activeUtilsFrame != null) {
						activeUtilsFrame.update(mousePos);
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

	private void changeFrameOverlays(JFrame newFrame) {
		if (activeUtilsFrame != null) {
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;

		}
		if (newFrame != null) {
			activeUtilsFrame = utilFrames.get(newFrame);

			if (activeUtilsFrame != null) {
				Point mousePos = MouseInfo.getPointerInfo().getLocation();
				activeUtilsFrame.setFloating(floatingDockable.getDockable());
				activeUtilsFrame.update(mousePos);
				activeUtilsFrame.setActive(true);
				activeUtilsFrame.toFront();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!mouseDragging) {
			dragOffset = e.getPoint();

			originalFrame = Docking.findFrameForDockable(floatingDockable.getDockable());
			rootState = Docking.getRootState(originalFrame);

			RootDockingPanel currentRoot = Docking.rootForFrame(originalFrame);

			floatingFrame = new TempFloatingFrame(floatingDockable.getDockable(), floatingDockable.getDockable().dragSource(), e.getPoint());

			floatingDockable.getParent().undock(floatingDockable.getDockable());

			Docking.removeIllegalFloats(originalFrame);

			if (originalFrame != null && currentRoot != null && currentRoot.getPanel() == null && Docking.canDisposeFrame(originalFrame)) {
				frameToDispose = originalFrame;
				frameToDispose.setVisible(false);
			}

			// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
			Point mousePos = new Point(mousePressedLocation);
			SwingUtilities.convertPointToScreen(mousePos, floatingDockable.getDockable().dragSource());

			JFrame frame = Docking.findRootAtScreenPos(mousePos);

			if (frame != frameToDispose) {
				activeUtilsFrame = utilFrames.get(frame);
			}

			SwingUtilities.invokeLater(() -> {
			if (activeUtilsFrame != null) {
				activeUtilsFrame.setFloating(floatingDockable.getDockable());
				activeUtilsFrame.update(mousePos);
				activeUtilsFrame.setActive(true);
			}
			});

			floatingFrame.addWindowListener(this);

			floating = true;
			AppState.setPaused(true);

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
			AppState.setPaused(false);

			floating = false;
			mouseDragging = false;

			Point mousePos = MouseInfo.getPointerInfo().getLocation();

			Point point = MouseInfo.getPointerInfo().getLocation();
			JFrame frame = Docking.findRootAtScreenPos(point);
			RootDockingPanel root = frame == null ? null : Docking.rootForFrame(frame);

			DockingPanel dockingPanel = Docking.findDockingPanelAtScreenPos(point);

			DockingRegion region = activeUtilsFrame != null ? activeUtilsFrame.getRegion(mousePos) : DockingRegion.CENTER;

			if (root != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToRoot()) {
				root.dock(floatingDockable.getDockable(), region, 0.5);
			}
			else if (frame != null && dockingPanel != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToDockable()) {
				dockingPanel.dock(floatingDockable.getDockable(), region, 0.5);
			}
			else if (root != null && region != DockingRegion.CENTER && activeUtilsFrame == null) {
				root.dock(floatingDockable.getDockable(), region, 0.5);
			}
			else if (!floatingDockable.getDockable().floatingAllowed()) {
				Docking.restoreState(originalFrame, rootState);
			}
			else {
				new FloatingFrame(floatingDockable.getDockable(), floatingFrame);
			}

			AppState.persist();

			originalFrame = null;

			if (frameToDispose != null) {
				Docking.deregisterDockingPanel(frameToDispose);
				frameToDispose.dispose();
				frameToDispose = null;
			}

			floatingFrame.removeWindowListener(this);
			floatingFrame.dispose();
			floatingFrame = null;

			if (activeUtilsFrame != null) {
				activeUtilsFrame.setActive(false);
				activeUtilsFrame = null;
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
