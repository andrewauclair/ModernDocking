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
package ModernDocking.floating;

import ModernDocking.*;
import ModernDocking.internal.DockableWrapper;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.internal.DockingPanel;
import ModernDocking.internal.FloatingFrame;
import ModernDocking.persist.AppState;
import ModernDocking.persist.RootDockState;

import javax.swing.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener responsible for tracking dockables both when they are first dragged and while being dragged
 */
public class FloatListener extends DragSourceAdapter implements DragSourceListener, DragSourceMotionListener {
	/**
	 * Flag indicating if there is a dockable currently floating
	 */
	public static boolean isFloating = false;

	// current floating dockable
	private final DockableWrapper floatingDockable;
	private final DockingInstance docking;

	// our drag source to support dragging the dockables
	private final DragSource dragSource = new DragSource();
	// dummy transferable, we don't actually transfer anything
	private final Transferable transferable = new StringSelection("");

	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final Map<Window, DockingUtilsFrame> utilFrames = new HashMap<>();

	private DockingUtilsFrame activeUtilsFrame = null;

	private static Window windowToDispose = null;

	private Window currentTopWindow = null;
	private Window currentTargetWindow = null;
	private Window originalWindow;

	private RootDockState rootState;

	private ModalityType modalityType = ModalityType.MODELESS;

	public FloatListener(DockingInstance docking, DockableWrapper dockable, Component dragSource) {
		this.floatingDockable = dockable;
		this.docking = docking;

		if (dragSource != null) {
			this.dragSource.addDragSourceMotionListener(FloatListener.this);

			this.dragSource.createDefaultDragGestureRecognizer(dragSource, DnDConstants.ACTION_MOVE, dge -> {
				this.dragSource.startDrag(dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), transferable, FloatListener.this);
				mouseDragStarted(dge.getDragOrigin());

				if (originalWindow instanceof JDialog) {
					modalityType = ((JDialog) originalWindow).getModalityType();

					((JDialog) originalWindow).setModalityType(ModalityType.MODELESS);

					// Set all of these as invokeLater to force the order they happen in
					SwingUtilities.invokeLater(() -> {
						// check that the floating frame still exists since we invoked later and time might have passed
						if (floatingFrame != null) {
							floatingFrame.toFront();
						}
					});
					SwingUtilities.invokeLater(() -> {
						// check that the utils frame still exists since we invoked later and time might have passed
						if (activeUtilsFrame != null) {
							activeUtilsFrame.toFront();
						}
					});
				}
			});
		}
	}

	public void removeListeners() {
		dragSource.removeDragSourceMotionListener(this);

		floatingDockable.removedListeners();
	}

	public static void registerDockingWindow(DockingInstance docking, Window window, RootDockingPanel root) {
		utilFrames.put(window, new DockingUtilsFrame(docking, window, root));
	}

	public static void deregisterDockingWindow(Window window) {
		utilFrames.remove(window);
	}

	private void updateFramePosition(Point mousePosOnScreen) {
		// update the frames position to our mouse position
		Point framePos = new Point(mousePosOnScreen.x - dragOffset.x, mousePosOnScreen.y - dragOffset.y);
		floatingFrame.setLocation(framePos);

		// find the frame at our current position
		Window frame = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);

		// findRootAtScreenPos has a tendency to find the last added frame at the position. meaning it ignores Z order. override it here because we know better.
		if (currentTopWindow != null && currentTopWindow.getBounds().contains(mousePosOnScreen)) {
			frame = currentTopWindow;
		}

		boolean isModal = modalityType == ModalityType.TOOLKIT_MODAL || modalityType == ModalityType.APPLICATION_MODAL;

		// change overlays and bring frames to front if we move over a new frame
		if (frame != currentTargetWindow && !isModal) {
			currentTargetWindow = frame;
			currentTopWindow = frame;

			changeFrameOverlays(frame);
		}

		Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, currentTopWindow);

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setFloating(floatingDockable.getDockable());
			activeUtilsFrame.setTargetDockable(dockable);
			activeUtilsFrame.update(mousePosOnScreen);
		}
	}

	private void changeFrameOverlays(Window newWindow) {
		if (activeUtilsFrame != null) {
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;
		}

		if (newWindow != null) {
			activeUtilsFrame = utilFrames.get(newWindow);

			if (currentTopWindow != null && floatingFrame != null && activeUtilsFrame != null) {
				Point mousePos = MouseInfo.getPointerInfo().getLocation();
				activeUtilsFrame.setFloating(floatingDockable.getDockable());
				activeUtilsFrame.update(mousePos);
				activeUtilsFrame.setActive(true);

				// Set all of these as invokeLater to force the order they happen in
				SwingUtilities.invokeLater(() -> {
					// check that the current top frame still exists since we invoked later and time might have passed
					if (currentTopWindow != null) {
						currentTopWindow.toFront();
					}
				});
				SwingUtilities.invokeLater(() -> {
					// check that the floating frame still exists since we invoked later and time might have passed
					if (floatingFrame != null) {
						floatingFrame.toFront();
					}
				});
				SwingUtilities.invokeLater(() -> {
					// check that the utils frame still exists since we invoked later and time might have passed
					if (activeUtilsFrame != null) {
						activeUtilsFrame.toFront();
					}
				});
			}
		}
	}

	public void mouseDragStarted(Point point) {
		isFloating = true;

		dragOffset = point;

		// force the drag offset to be inset from the edge slightly
		dragOffset.y = Math.max(5, dragOffset.y);
		dragOffset.x = Math.max(5, dragOffset.x);

		currentTargetWindow = null;
		originalWindow = DockingComponentUtils.findWindowForDockable(docking, floatingDockable.getDockable());

		rootState = DockingState.getRootState(docking, originalWindow);

		RootDockingPanel currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

		floatingFrame = new TempFloatingFrame(docking, floatingDockable.getDockable(), (JComponent) floatingDockable.getHeaderUI());

		docking.undock(floatingDockable.getDockable());

		DockingComponentUtils.removeIllegalFloats(docking, originalWindow);

		if (originalWindow != null && currentRoot != null && currentRoot.getPanel() == null && docking.canDisposeWindow(originalWindow)) {
			windowToDispose = originalWindow;
			windowToDispose.setVisible(false);
		}

		// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
		Point mousePos = new Point(point);
		SwingUtilities.convertPointToScreen(mousePos, (Component) floatingDockable.getHeaderUI());

		if (originalWindow != windowToDispose) {
			currentTopWindow = originalWindow;
			currentTargetWindow = originalWindow;
			activeUtilsFrame = utilFrames.get(originalWindow);
		}

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setFloating(floatingDockable.getDockable());
			activeUtilsFrame.update(mousePos);
			activeUtilsFrame.setActive(true);
			activeUtilsFrame.toFront();
		}

		AppState.setPaused(true);
	}

	private void dropFloatingPanel() {
		AppState.setPaused(false);

		Point mousePos = MouseInfo.getPointerInfo().getLocation();

		Point point = MouseInfo.getPointerInfo().getLocation();

		RootDockingPanel root = currentTopWindow == null ? null : DockingComponentUtils.rootForWindow(docking, currentTopWindow);

		DockingPanel dockingPanel = DockingComponentUtils.findDockingPanelAtScreenPos(point, currentTopWindow);
		Dockable dockableAtPos = DockingComponentUtils.findDockableAtScreenPos(point, currentTopWindow);

		DockingRegion region = activeUtilsFrame != null ? activeUtilsFrame.getRegion(mousePos) : DockingRegion.CENTER;

		if (root != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToRoot()) {
			docking.dock(floatingDockable.getDockable(), currentTopWindow, region, 0.25);
		}
		else if (floatingDockable.getDockable().isLimitedToRoot() && floatingDockable.getRoot() != root) {
			DockingState.restoreState(docking, originalWindow, rootState);
		}
		else if (currentTopWindow != null && dockingPanel != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToDockable()) {
			docking.dock(floatingDockable.getDockable(), dockableAtPos, region);
		}
		else if (root != null && region != DockingRegion.CENTER && activeUtilsFrame == null) {
			docking.dock(floatingDockable.getDockable(), currentTopWindow, region);
		}
		else if (!floatingDockable.getDockable().isFloatingAllowed()) {
			DockingState.restoreState(docking, originalWindow, rootState);
		}
		else {
			new FloatingFrame(docking, floatingDockable.getDockable(), floatingFrame);
		}

		// auto persist the new layout to the file
		AppState.persist(docking);

		if (originalWindow instanceof JDialog) {
			((JDialog) originalWindow).setModalityType(modalityType);
		}

		originalWindow = null;

		// if we're disposing the frame we started dragging from, dispose of it now
		if (windowToDispose != null) {
			docking.deregisterDockingPanel(windowToDispose);
			windowToDispose.dispose();
			windowToDispose = null;
		}

		// dispose of the temp floating frame now that we're done with it
		floatingFrame.dispose();
		floatingFrame = null;

		// hide the overlay frame if one is active
		if (activeUtilsFrame != null) {
			activeUtilsFrame.setTargetDockable(null);
			activeUtilsFrame.setFloating(null);
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;
		}
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		dropFloatingPanel();

		isFloating = false;
	}

	@Override
	public void dragMouseMoved(DragSourceDragEvent dsde) {
		if (!isFloating) {
			return;
		}
		updateFramePosition(dsde.getLocation());
	}
}
