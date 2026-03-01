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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import io.github.andrewauclair.moderndocking.internal.util.SlideBorder;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Special JPanel used to contain a dockable within a docking toolbar
 */
public class DockedAutoHidePanel extends JPanel implements ComponentListener, MouseMotionListener {
    private final DockingAPI docking;
    /**
	 * The root that this auto hide panel belongs to
	 */
	private final RootDockingPanelAPI root;
	/**
	 * The toolbar that contains the dockable in this auto hide panel
	 */
	private final DockableToolbar toolbar;
	private final SlideBorder slideBorder;

	/**
	 * Flag indicating if the panel has been configured. Configuration doesn't occur until the panel is setVisible(true)
	 */
	private boolean configured = false;

	/**
	 * Create a new DockedAutoHidePanel to contain a dockable on a docking toolbar
	 *
	 * @param docking The docking instance
	 * @param dockable The dockable contained on this panel
	 * @param root The root panel of the Window
	 * @param toolbar The toolbar this panel is in
	 */
	public DockedAutoHidePanel(DockingAPI docking, Dockable dockable, RootDockingPanelAPI root, DockableToolbar toolbar) {
        this.docking = docking;
        this.root = root;
		this.toolbar = toolbar;

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;

		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
		DockedSimplePanel panel = new DockedSimplePanel(docking, wrapper, "", wrapper.getDisplayPanel(), false);
		slideBorder = new SlideBorder(toolbar.getDockedLocation());

		if (toolbar.getDockedLocation() == ToolbarLocation.SOUTH) {
			System.out.println("Create south toolbar");
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			add(slideBorder, gbc);

			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy++;

			add(panel, gbc);
		}
		else if (toolbar.getDockedLocation() == ToolbarLocation.EAST) {
			System.out.println("Create east toolbar");
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.VERTICAL;

			add(slideBorder, gbc);

			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx++;

			add(panel, gbc);
		}
		else { // west
			System.out.println("Create west toolbar");
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;

			add(panel, gbc);

			gbc.weightx = 0.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.gridx++;

			add(slideBorder, gbc);
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();

		slideBorder.addMouseMotionListener(this);

		root.addComponentListener(this);
		addComponentListener(this);
	}

	@Override
	public void removeNotify() {
		slideBorder.removeMouseMotionListener(this);

		root.removeComponentListener(this);
		removeComponentListener(this);

		super.removeNotify();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		setLocationAndSize(0);

		if (!configured) {
			configured = true;
		}
	}

	public int getSlidePosition() {
		if (toolbar.getDockedLocation() == ToolbarLocation.SOUTH) {
			return getHeight();
		}
		return getWidth();
	}

	public void setSlidePosition(int position) {
		System.out.println("setSlidePosition, position = " + position + ", location: " + toolbar.getDockedLocation());
		if (toolbar.getDockedLocation() == ToolbarLocation.SOUTH) {
			setSize(getWidth(), position);
		}
		else {
			setSize(position, getHeight());
		}
	}

	private void setLocationAndSize(int widthDifference) {
		Point toolbarLocation = toolbar.getLocation();
		SwingUtilities.convertPointToScreen(toolbarLocation, toolbar.getParent());

		Dimension toolbarSize = toolbar.getSize();

		// this panel will be in a layered pane without a layout manager
		// we must configure the size and position ourselves
		if (toolbar.isVertical()) {
			int width = (int) (root.getWidth() / 4.0); // default width, 1/4 of root width
			int height = toolbarSize.height; // match height of toolbar

			if (configured)
			{
				System.out.println("Already configured. width: " + getWidth() + ", location: " + toolbar.getDockedLocation());
				width = getWidth() + widthDifference;
			}
			else {
				System.out.println("First time configuring " + toolbar.getDockedLocation());
			}

			if (root.isVisible()) {

			}

			System.out.println("width = " + width + ", location: " + toolbar.getDockedLocation());

			Point location = new Point(toolbarLocation.x + toolbarSize.width, toolbarLocation.y);
			Dimension size = new Dimension(width, height);

			if (toolbar.getDockedLocation() == ToolbarLocation.EAST) {
				location.x = toolbarLocation.x - width;
			}

			SwingUtilities.convertPointFromScreen(location, getParent());

			setLocation(location);
			setSize(size);
		}
		else {
			int width = toolbarSize.width; // match width of toolbar
			int height = (int) (root.getHeight() / 4.0); // default height, 1/4 of root height

			if (configured)
			{
				System.out.println("Already configured. height: " + getHeight() + ", location: " + toolbar.getDockedLocation());
				height = getHeight() + widthDifference;
			}
			else {
				System.out.println("First time configuring " + toolbar.getDockedLocation());
			}


			System.out.println("height = " + height + ", location: " + toolbar.getDockedLocation());

			Point location = new Point(toolbarLocation.x, toolbarLocation.y - height);
			Dimension size = new Dimension(width, height);

			SwingUtilities.convertPointFromScreen(location, getParent());

			setLocation(location);
			setSize(size);
		}

		revalidate();
		repaint();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// component has resized, update the location and size of the auto hide panel
		if (e.getComponent() == root) {
			setLocationAndSize(0);
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
		if (e.getComponent() == root) {
			int width = getWidth();
			int height = getHeight();

			if (toolbar.isVertical()) {
				width = Math.max(100, width); // minimum width is 100
				width = Math.min(width, root.getWidth() - 100); // max width is root - 100
			}
			else {
				height = Math.max(100, height); // minimum height is 100
				height = Math.min(height, root.getHeight() - 100); // max height is root - 100

			}

			setSize(width, height);
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// dragging the divider, update the size and location of the auto hide panel
		if (toolbar.getDockedLocation() == ToolbarLocation.SOUTH) {
			setLocationAndSize(-e.getY());
		}
		else if (toolbar.getDockedLocation() == ToolbarLocation.WEST) {
			setLocationAndSize(e.getX());
		}
		else { // east
			setLocationAndSize(-e.getX());
		}

		docking.getAppState().persist();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
