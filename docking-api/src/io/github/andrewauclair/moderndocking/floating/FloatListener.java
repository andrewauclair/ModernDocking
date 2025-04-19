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
package io.github.andrewauclair.moderndocking.floating;

import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DisplayPanel;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.InvalidDnDOperationException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Base class for float listeners. Listen for drags that might be dockable floating events
 */
public abstract class FloatListener extends DragSourceAdapter implements DragSourceMotionListener, DragSourceListener {
	private final DockingAPI docking;
	private final JPanel panel;
	private final JComponent dragComponent;

	// our drag source to support dragging the dockables
	private final DragSource dragSource = new DragSource();

	private Point dragComponentDragOffset = new Point();

	protected Window originalWindow;
	private WindowLayout originalWindowLayout;
	private JFrame floatingFrame;

	private Window currentUtilWindow;
	private FloatUtilsFrame currentUtilFrame;
	private DragGestureRecognizer alternateDragGesture;

	/**
	 * Create a new listener for a specific panel
	 *
	 * @param docking The docking instance
	 * @param panel The panel to listen for drags on
	 */
	public FloatListener(DockingAPI docking, DisplayPanel panel) {
		this(docking, panel, (JComponent) panel.getWrapper().getHeaderUI());
	}

	/**
	 * Create a new listener for a specific panel
	 *
	 * @param docking The docking instance
	 * @param panel The panel to listen for drags on
	 * @param dragComponent The component to add the drag listeners to
	 */
	public FloatListener(DockingAPI docking, DisplayPanel panel, JComponent dragComponent) {
		this(docking, (JPanel) panel, dragComponent);
	}

	/**
	 * Create a new listener for a tabbed panel
	 *
	 * @param docking The docking instance
	 * @param tabs The tabbed panel to listen for drags on
	 * @param dragComponent The component to add drag listeners to
	 */
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

	/**
	 * Add an alternate drag source, used for dragging from tabs
	 *
	 * @param dragComponent Drag component to add
	 * @param listener The drag gesture listener to add
	 */
	public void addAlternateDragSource(JComponent dragComponent, DragGestureListener listener) {
		alternateDragGesture = dragSource.createDefaultDragGestureRecognizer(dragComponent, DnDConstants.ACTION_MOVE, listener);
	}

	/**
	 * Remove the alternate drag source
	 *
	 * @param listener The listener to remove
	 */
	public void removeAlternateDragSource(DragGestureListener listener) {
		alternateDragGesture.removeDragGestureListener(listener);
		alternateDragGesture = null;
	}

	/**
	 * Get the panel that we're listening to
	 *
	 * @return Dockable panel
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Check if this listener is interested in the drag
	 *
	 * @param dragGestureEvent The drag event that is in progress
	 * @return True if this listener wishes to handle the drag
	 */
	protected abstract boolean allowDrag(DragGestureEvent dragGestureEvent);

	/**
	 * Start a new drag
	 *
	 * @param dragGestureEvent The drag gesture
	 */
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

		currentUtilWindow = null;

		Floating.setFloating(true);

		dragStarted(dragGestureEvent.getDragOrigin());

		Point mouseOnScreen = new Point(dragGestureEvent.getDragOrigin());
		SwingUtilities.convertPointToScreen(mouseOnScreen, dragGestureEvent.getComponent());

		updateFramePosition(mouseOnScreen);
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

		InternalRootDockingPanel currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

		// hide the original window if it is not the main window of the app
		if (currentRoot.isEmpty() && docking.getMainWindow() != originalWindow) {
			originalWindow.setVisible(false);
		}
	}

	/**
	 * Remove our drag source motion listener
	 */
	public void removeListeners() {
		dragSource.removeDragSourceMotionListener(this);
	}

	@Override
	public void dragMouseMoved(DragSourceDragEvent event) {
		if (!Floating.isFloating()) {
			return;
		}
		updateFramePosition(event.getLocation());
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

		if (frame != currentUtilWindow) {
			if (currentUtilFrame != null) {
				currentUtilFrame.deactivate();
			}

			currentUtilWindow = frame;

			currentUtilFrame = Floating.frameForWindow(currentUtilWindow);

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

		InternalRootDockingPanel currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

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

	/**
	 * Get the original window that the dockable started in at the start of this floating event
	 *
	 * @return The original window of the dockable
	 */
	protected abstract Window getOriginalWindow();

	/**
	 * Undock the dockable now that a drag has started
	 */
	protected abstract void undock();

	/**
	 * Create a new floating frame to contain a dockable
	 *
	 * @return The new frame
	 */
	protected abstract JFrame createFloatingFrame();

	/**
	 * Drop the floating panel
	 *
	 * @param utilsFrame The utils frame that provides the docking handles and overlay
	 * @param floatingFrame The floating frame that contains the floating dockable
	 * @param mousePosOnScreen The position of the mouse on screen
	 *
	 * @return Did we successfully dock the floating dockable?
	 */
	protected abstract boolean dropPanel(FloatUtilsFrame utilsFrame, JFrame floatingFrame, Point mousePosOnScreen);
}