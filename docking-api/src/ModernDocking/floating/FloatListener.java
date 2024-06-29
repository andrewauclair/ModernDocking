/*
Copyright (c) 2024 Andrew Auclair

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

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.internal.DockedTabbedPanel;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.layouts.WindowLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

public abstract class FloatListener extends DragSourceAdapter implements DragSourceMotionListener, DragSourceListener {
	private final DockingAPI docking;
	private final JPanel panel;
	private final JComponent dragComponent;

	// our drag source to support dragging the dockables
	private final DragSource dragSource = new DragSource();

	private Point dragComponentDragOffset = new Point();

	private Window originalWindow;
	private WindowLayout originalWindowLayout;
	private JFrame floatingFrame;

	private Window currentWindow;
	private FloatUtilsFrame currentUtilFrame;
	private DragGestureRecognizer alternateDragGesture;

	public FloatListener(DockingAPI docking, DisplayPanel panel) {
		this(docking, panel, (JComponent) panel.getWrapper().getHeaderUI());
	}

	public FloatListener(DockingAPI docking, DisplayPanel panel, JComponent dragComponent) {
		this(docking, (JPanel) panel, dragComponent);
	}

	public FloatListener(DockingAPI docking, DockedTabbedPanel tabs, JComponent dragComponent) {
		this(docking, (JPanel) tabs, dragComponent);
	}

	private FloatListener(DockingAPI docking, JPanel panel, JComponent dragComponent) {
		this.docking = docking;
		this.panel = panel;
		this.dragComponent = dragComponent;

		if (dragComponent != null) {
			dragSource.addDragSourceMotionListener(this);
			dragSource.createDefaultDragGestureRecognizer(dragComponent, DnDConstants.ACTION_MOVE, this::startDrag);
		}
	}

	public void addAlternateDragSource(JComponent dragComponent, DragGestureListener listener) {
		alternateDragGesture = dragSource.createDefaultDragGestureRecognizer(dragComponent, DnDConstants.ACTION_MOVE, listener);
	}

	public void removeAlternateDragSource(DragGestureListener listener) {
		alternateDragGesture.removeDragGestureListener(listener);
		alternateDragGesture = null;
	}

	public JPanel getPanel() {
		return panel;
	}

	protected abstract boolean allowDrag(DragGestureEvent dragGestureEvent);

	public void startDrag(DragGestureEvent dragGestureEvent) {
		// if there is already a floating panel, don't float this one
		if (Floating.isFloating()) {
			return;
		}

		if (!allowDrag(dragGestureEvent)) {
			return;
		}

		try {
			dragSource.startDrag(dragGestureEvent, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), new StringSelection(""), this);
		}
		catch (InvalidDnDOperationException ignored) {
			// someone beat us to it
			return;
		}

		currentWindow = null;

		Floating.setFloating(true);

		dragStarted(dragGestureEvent.getDragOrigin());

		Point mouseOnScreen = new Point(dragGestureEvent.getDragOrigin());
		SwingUtilities.convertPointToScreen(mouseOnScreen, dragGestureEvent.getComponent());

		SwingUtilities.invokeLater(() -> updateFramePosition(mouseOnScreen));
	}

	private void dragStarted(Point dragOrigin) {
		dragComponentDragOffset = new Point(dragOrigin);

		// force the drag offset to be inset from the edge slightly
		dragComponentDragOffset.y = Math.max(5, dragComponentDragOffset.y);
		dragComponentDragOffset.x = Math.max(5, dragComponentDragOffset.x);

		originalWindow = getOriginalWindow();

		originalWindowLayout = docking.getDockingState().getWindowLayout(originalWindow);

		floatingFrame = createFloatingFrame();

		undock();

		DockingComponentUtils.removeIllegalFloats(docking, originalWindow);

		RootDockingPanelAPI currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

		if (currentRoot.isEmpty()) {
			originalWindow.setVisible(false);
		}
	}

	public void removeListeners() {
		dragSource.removeDragSourceMotionListener(this);
	}

	@Override
	public void dragMouseMoved(DragSourceDragEvent event) {
		if (!Floating.isFloating()) {
			return;
		}
		SwingUtilities.invokeLater(() -> updateFramePosition(event.getLocation()));
	}

	private void updateFramePosition(Point mousePosOnScreen) {
		// update the frames position to our mouse position
		Point framePos = new Point(mousePosOnScreen.x - dragComponentDragOffset.x, mousePosOnScreen.y - dragComponentDragOffset.y);
		floatingFrame.setLocation(framePos);

		checkForFrameSwitch(mousePosOnScreen);
	}

	private void checkForFrameSwitch(Point mousePosOnScreen) {
		// find the frame at our current position
		Window frame = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);

		if (frame != currentWindow) {
			if (currentUtilFrame != null) {
				currentUtilFrame.deactivate();
			}

			currentWindow = frame;

			currentUtilFrame = Floating.frameForWindow(currentWindow);

			if (currentUtilFrame != null) {
				currentUtilFrame.activate(this, floatingFrame, dragSource, mousePosOnScreen);
			}
		}
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent event) {
		if (!Floating.isFloating()) {
			return;
		}
		dropFloatingPanel(event.getLocation());

		RootDockingPanelAPI currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

		if (currentRoot.isEmpty() && docking.canDisposeWindow(originalWindow)) {
			originalWindow.dispose();
		}

		Floating.setFloating(false);
	}

	private void dropFloatingPanel(Point mousePosOnScreen) {
		if (!Floating.isFloating()) {
			return;
		}

		boolean docked = dropPanel(currentUtilFrame, floatingFrame, mousePosOnScreen);

		if (currentUtilFrame != null) {
			currentUtilFrame.deactivate();
		}

		if (!docked) {
			docking.getDockingState().restoreWindowLayout(originalWindow, originalWindowLayout);
		}

		floatingFrame.dispose();
		floatingFrame = null;
	}

	protected abstract Window getOriginalWindow();

	protected abstract void undock();

	protected abstract JFrame createFloatingFrame();

	protected abstract boolean dropPanel(FloatUtilsFrame utilsFrame, JFrame floatingFrame, Point mousePosOnScreen);
}