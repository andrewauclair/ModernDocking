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

import ModernDocking.Dockable;
import ModernDocking.DockingInstance;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * utility frame that is used to draw handles and overlay highlighting
 */
public class DockingUtilsFrame extends JFrame implements ComponentListener {
	/**
	 * Handles display for this utility frame
	 */
	private final DockingHandles handles;
	/**
	 * Overlay display for this utility frame
	 */
	private final DockingOverlay overlay;
	/**
	 * The window from the application that this utility frame is directly over
	 */
	private final Window referenceDockingWindow;

	/**
	 * create a new DockingUtilsFrame with a frame and its root panel
	 *
	 * @param referenceDockingWindow Window that this utility frame is tied to
	 * @param root The root of the tied window
	 */
	public DockingUtilsFrame(DockingInstance docking, Window referenceDockingWindow, RootDockingPanel root) {
		setLayout(null); // don't use a layout manager for this custom painted frame
		setUndecorated(true); // don't want to see a frame border
		setType(Type.UTILITY); // hide this frame from the task bar

		setBackground(new Color(0, 0, 0, 0)); // don't want a background for this frame
		getRootPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the root pane either. Workaround for a FlatLaf macOS issue.
		getContentPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the content frame either.

		try {
			if (getContentPane() instanceof JComponent) {
				((JComponent) getContentPane()).setOpaque(false);
			}
		}
		catch (IllegalComponentStateException e) {
			// TODO we need to handle platforms that don't support translucent display
			// this exception indicates that the platform doesn't support changing the opacity
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); // always moving a dockable when this frame is visible. use the moving cursor to indicate such

		// set location and size based on the reference docking frame
		setLocation(referenceDockingWindow.getLocation());
		setSize(referenceDockingWindow.getSize());

		// remember the reference docking frame and create the handles and over components
		this.referenceDockingWindow = referenceDockingWindow;
		handles = new DockingHandles(this, root);
		overlay = new DockingOverlay(docking, this, root);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		// listen for the reference frame to move and resize. this frame must match it exactly
		referenceDockingWindow.addComponentListener(this);
	}

	@Override
	public void removeNotify() {
		referenceDockingWindow.removeComponentListener(this);

		super.removeNotify();
	}

	/**
	 * set the current dockable that the mouse is over, can be null
	 *
	 * @param target Target dockable
	 */
	public void setTargetDockable(Dockable target) {
		handles.setTarget(target);
		overlay.setTargetDockable(target);

		overlay.setTargetRootRegion(handles.getRootRegion());
		overlay.setTargetDockableRegion(handles.getDockableRegion());
	}

	/**
	 * set the floating panel, doesn't change once the panel is first floated
	 *
	 * @param floating Floating dockable
	 */
	public void setFloating(Dockable floating) {
		handles.setFloating(floating);
		overlay.setFloating(floating);
	}

	/**
	 * update the overlay with the current mouse position
	 *
	 * @param screenPos New mouse position
	 */
	public void update(Point screenPos) {
		handles.update(screenPos);
		overlay.update(screenPos);
	}

	/**
	 * activate the overlays, sets them to visible
	 *
	 * @param active New active state
	 */
	public void setActive(boolean active) {
		handles.setActive(active);
		overlay.setActive(active);
	}

	/**
	 * get the current region from the overlay. this is either a root region or dockable region
	 *
	 * @param screenPos Screen position to find region of
	 * @return The root or dockable region at the screen position
	 */
	public DockingRegion getRegion(Point screenPos) {
		return overlay.getRegion(screenPos);
	}

	/**
	 * checks if docking to the root. This is only possible when the mouse is over a root docking handle
	 *
	 * @return Is docking to root
	 */
	public boolean isDockingToRoot() {
		return overlay.isDockingToRoot();
	}

	/**
	 * checks if docking to a dockable. Returns false if isDockingToRoot() is true.
	 *
	 * @return false if not over a frame
	 */
	public boolean isDockingToDockable() {
		return overlay.isDockingToDockable();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		setSize(referenceDockingWindow.getSize());
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		setLocation(referenceDockingWindow.getLocation());
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		// nothing to really paint, but this will give us a clean slate
		super.paint(g);

		// paint the handles and overlays. nothing is painted if they aren't visible
		handles.paint(g);
		overlay.paint(g);
	}
}
