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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloatListener extends DragSourceAdapter implements DragSourceListener, DragSourceMotionListener {
	// current floating dockable
	private final DockableWrapper floatingDockable;

	private final DragSource dragSource = new DragSource();
	// dummy transferable, we don't actually transfer anything
	private final Transferable transferable = new StringSelection("");

	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final Map<JFrame, DockingUtilsFrame> utilFrames = new HashMap<>();

	private DockingUtilsFrame activeUtilsFrame = null;

	private static JFrame frameToDispose = null;

//	private final List<JFrame> framesBroughtToFront = new ArrayList<>();
	private JFrame currentTopFrame = null;
	private JFrame currentTargetFrame = null;
	private JFrame originalFrame;

	private RootDockState rootState;

	public FloatListener(DockableWrapper dockable) {
		this.floatingDockable = dockable;

		if (this.floatingDockable.getDockable().dragSource() != null) {
			dragSource.addDragSourceMotionListener(FloatListener.this);

			dragSource.createDefaultDragGestureRecognizer(this.floatingDockable.getDockable().dragSource(), DnDConstants.ACTION_MOVE, dge -> {
				if (!Docking.isUnpinned(floatingDockable.getDockable())) {
					dragSource.startDrag(dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), transferable, FloatListener.this);
					mouseDragged(dge.getDragOrigin());
				}
			});
		}
	}

	public static void reset() {
		// used when creating a new Docking instance, mostly to hack the tests
		utilFrames.clear();
		frameToDispose = null;
	}

	public void removeListeners() {
		if (floatingDockable.getDockable().dragSource() != null) {
			dragSource.removeDragSourceListener(this);
			dragSource.removeDragSourceMotionListener(this);
		}

		floatingDockable.removedListeners();
	}

	public static void registerDockingFrame(JFrame frame, RootDockingPanel root) {
		utilFrames.put(frame, new DockingUtilsFrame(frame, root));
	}

	private void updateFramePosition(Point mousePos) {
		// update the frames position to our mouse position
		Point framePos = new Point(mousePos.x - dragOffset.x, mousePos.y - dragOffset.y);
		floatingFrame.setLocation(framePos);

		// find the frame at our current position
		JFrame frame = Docking.findRootAtScreenPos(mousePos);

		// findRootAtScreenPos has a tendency to find the last added frame at the position. meaning it ignores Z order. override it here because we know better.
		if (currentTopFrame != null && currentTopFrame.getBounds().contains(mousePos)) {
			frame = currentTopFrame;
		}

		// change overlays and bring frames to front if we move over a new frame
		if (frame != currentTargetFrame) {
			currentTargetFrame = frame;

			changeFrameOverlays(frame);

			if (frame != null && frame != currentTopFrame) {
				currentTopFrame = frame;
				if (activeUtilsFrame != null) {
					currentTopFrame.toFront();
					activeUtilsFrame.toFront();
				}
			}
		}

		Dockable dockable = Docking.findDockableAtScreenPos(mousePos, currentTopFrame);

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setFloating(floatingDockable.getDockable());
			activeUtilsFrame.setTargetDockable(dockable);
			activeUtilsFrame.update(mousePos);
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

	public void mouseDragged(Point point) {
		dragOffset = point;

		// force the drag offset to be inset from the edge slightly
		dragOffset.y = Math.max(5, dragOffset.y);
		dragOffset.x = Math.max(5, dragOffset.x);

		currentTargetFrame = null;
		originalFrame = Docking.findFrameForDockable(floatingDockable.getDockable());
		rootState = Docking.getRootState(originalFrame);

		RootDockingPanel currentRoot = Docking.rootForFrame(originalFrame);

		floatingFrame = new TempFloatingFrame(floatingDockable.getDockable(), floatingDockable.getDockable().dragSource(), point);

		floatingDockable.getParent().undock(floatingDockable.getDockable());

		Docking.removeIllegalFloats(originalFrame);

		if (originalFrame != null && currentRoot != null && currentRoot.getPanel() == null && Docking.canDisposeFrame(originalFrame)) {
			frameToDispose = originalFrame;
			frameToDispose.setVisible(false);
		}

		// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
		Point mousePos = new Point(point);
		SwingUtilities.convertPointToScreen(mousePos, floatingDockable.getDockable().dragSource());

		JFrame frame = Docking.findRootAtScreenPos(mousePos);

		if (frame != frameToDispose) {
			currentTopFrame = frame;
			currentTargetFrame = frame;
			activeUtilsFrame = utilFrames.get(frame);
		}

		SwingUtilities.invokeLater(() -> {
			if (activeUtilsFrame != null) {
				activeUtilsFrame.setFloating(floatingDockable.getDockable());
				activeUtilsFrame.update(mousePos);
				activeUtilsFrame.setActive(true);
				activeUtilsFrame.toFront();
			}
		});

		AppState.setPaused(true);
	}

	private void dropFloatingPanel() {
		AppState.setPaused(false);

		Point mousePos = MouseInfo.getPointerInfo().getLocation();

		Point point = MouseInfo.getPointerInfo().getLocation();
		JFrame frame = Docking.findRootAtScreenPos(point);
		RootDockingPanel root = frame == null ? null : Docking.rootForFrame(frame);

		DockingPanel dockingPanel = Docking.findDockingPanelAtScreenPos(point);
		Dockable dockableAtPos = Docking.findDockableAtScreenPos(point);


		DockingRegion region = activeUtilsFrame != null ? activeUtilsFrame.getRegion(mousePos) : DockingRegion.CENTER;

		if (root != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToRoot()) {
			Docking.dock(floatingDockable.getDockable(), frame, region);
		}
		else if (frame != null && dockingPanel != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToDockable()) {
			Docking.dock(floatingDockable.getDockable(), dockableAtPos, region);
		}
		else if (root != null && region != DockingRegion.CENTER && activeUtilsFrame == null) {
			Docking.dock(floatingDockable.getDockable(), frame, region);
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

		floatingFrame.dispose();
		floatingFrame = null;

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;
		}

//		framesBroughtToFront.clear();
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		dropFloatingPanel();
	}

	@Override
	public void dragMouseMoved(DragSourceDragEvent dsde) {
		updateFramePosition(dsde.getLocation());
	}
}
