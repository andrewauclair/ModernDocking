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

import docking.Dockable;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

// utility frame that is used to draw handles and overlay highlighting
public class DockingUtilsFrame extends JFrame implements ComponentListener {
	private final DockingHandles handles;
	private final DockingOverlay overlay;
	private final JFrame frame;

	// create a new DockingUtilsFrame with a frame and its root panel
	public DockingUtilsFrame(JFrame frame, RootDockingPanel root) {
		this.frame = frame;
		setType(Type.UTILITY);
		setUndecorated(true);
		setLayout(null);
		setLocation(frame.getLocation());
		setSize(frame.getSize());

		handles = new DockingHandles(this, root);
		overlay = new DockingOverlay(this, root);

		frame.addComponentListener(this);

		setBackground(new Color(0, 0, 0, 0));
	}

	// set the current dockable that the mouse is over, might be null
	public void setTargetDockable(Dockable target) {
		handles.setTarget(target);
		overlay.setTargetDockable(target);

		overlay.setTargetRootRegion(handles.getRootRegion());
		overlay.setTargetDockableRegion(handles.getDockableRegion());
	}

	// set the floating panel, doesn't change once the panel is first floated
	public void setFloating(Dockable floating) {
		handles.setFloating(floating);
		overlay.setFloating(floating);
	}

	// update the overlay with the current mouse position
	public void update(Point screenPos) {
		handles.update(screenPos);
		overlay.update(screenPos);
	}

	// activate the overlays, sets them to visible
	public void setActive(boolean active) {
		handles.setActive(active);
		overlay.setActive(active);
	}

	public DockingRegion getRegion(Point screenPos) {
		return overlay.getRegion(screenPos);
	}

	// checks if docking to the root. This is only possible when the mosue is over a root docking handle
	public boolean isDockingToRoot() {
		return overlay.isDockingToRoot();
	}

	// checks if docking to a dockable. Returns false if isDockingToRoot() is true.
	// Returns false if not over a frame
	public boolean isDockingToDockable() {
		return overlay.isDockingToDockable();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		setSize(frame.getSize());
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		setLocation(frame.getLocation());
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		handles.paint(g);
		overlay.paint(g);
	}
}
