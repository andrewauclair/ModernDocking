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
package ModernDocking.layouts;

import javax.swing.*;
import java.awt.Dialog.ModalityType;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * layout of a single frame
 */
public class WindowLayout {
	private boolean isMainFrame;
	private Point location;
	private final boolean hasSizeAndLocationInformation;
	private Dimension size;
	private final int state;
	private final ModalityType modalityType;
	private final DockingLayoutNode rootNode;
	private String maximizedDockable = null;

	private final int windowHashCode;

	private final List<String> westUnpinnedToolbarIDs = new ArrayList<>();
	private final List<String> eastUnpinnedToolbarIDs = new ArrayList<>();
	private final List<String> southUnpinnedToolbarIDs = new ArrayList<>();

	/**
	 * Create a new WindowLayout from an existing root node
	 *
	 * @param isMainFrame Flag indicating if this is the main frame of the application
	 * @param location The location of the window on screen
	 * @param size The width and height of the window
	 * @param state State of the window (maximized, minimized)
	 * @param rootNode Root of the window
	 */
	public WindowLayout(boolean isMainFrame, Point location, Dimension size, int state, DockingLayoutNode rootNode) {
		this.isMainFrame = isMainFrame;
		this.location = location;
		this.size = size;
		this.state = state;
		this.rootNode = rootNode;
		this.modalityType = ModalityType.MODELESS;
		this.windowHashCode = 0;

		hasSizeAndLocationInformation = true;
	}

	/**
	 * Create a WindowLayout from a root node. All other info is defaulted
	 *
	 * @param rootNode Root of window
	 */
	public WindowLayout(DockingLayoutNode rootNode) {
		this.rootNode = rootNode;
		this.state = Frame.NORMAL;
		this.windowHashCode = 0;

		hasSizeAndLocationInformation = false;

		location = new Point();
		size = new Dimension();
		modalityType = ModalityType.MODELESS;
	}

	/**
	 * Create a new WindowLayout for the given window and its root. Assumed to not be the main frame
	 *
	 * @param window The window for this layout
	 * @param rootNode The root for the window
	 */
	public WindowLayout(Window window, DockingLayoutNode rootNode) {
		this.rootNode = rootNode;
		this.location = window.getLocation();
		this.size = window.getSize();
		this.windowHashCode = window.hashCode();

		if (window instanceof JFrame) {
			this.state = ((JFrame) window).getExtendedState();
			this.modalityType = ModalityType.MODELESS;
		}
		else {
			this.state = Frame.NORMAL;
			this.modalityType = ((JDialog) window).getModalityType();
		}

		hasSizeAndLocationInformation = true;
	}

	/**
	 * Check if the contained window is the applications main frame
	 *
	 * @return True if contained window is the main frame of the application
	 */
	public boolean isMainFrame() {
		return isMainFrame;
	}

	/**
	 * Get the location on screen of this window
	 *
	 * @return Screen location
	 */
	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * Get the size of this window
	 *
	 * @return Size (width and height) of the contained window
	 */
	public Dimension getSize() {
		return size;
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public int getState() {
		return state;
	}

	public ModalityType getModalityType() {
		return modalityType;
	}

	public DockingLayoutNode getRootNode() {
		return rootNode;
	}

	public void setMaximizedDockable(String persistentID) {
		maximizedDockable = persistentID;
	}

	/**
	 * Get the persistent ID of the dockable which is currently maximized
	 *
	 * @return Name of maximized dockable. Empty string if none.
	 */
	public String getMaximizedDockable() {
		return maximizedDockable;
	}

	/**
	 * Set a list of all the dockables on the west toolbar
	 *
	 * @param ids List of unpinned dockable IDs on the west toolbar
	 */
	public void setWestUnpinnedToolbarIDs(List<String> ids) {
		westUnpinnedToolbarIDs.clear();
		westUnpinnedToolbarIDs.addAll(ids);
	}

	/**
	 * Get a list of all the dockables on the west toolbar
	 *
	 * @return List of unpinned dockable IDs on the west toolbar
	 */
	public List<String> getWestUnpinnedToolbarIDs() {
		return westUnpinnedToolbarIDs;
	}

	/**
	 * Set a list of all the dockables on the east toolbar
	 *
	 * @param ids List of unpinned dockable IDs on the east toolbar
	 */
	public void setEastUnpinnedToolbarIDs(List<String> ids) {
		eastUnpinnedToolbarIDs.clear();
		eastUnpinnedToolbarIDs.addAll(ids);
	}

	/**
	 * Get a list of all the dockables on the east toolbar
	 *
	 * @return List of unpinned dockable IDs on the east toolbar
	 */
	public List<String> getEastUnpinnedToolbarIDs() {
		return eastUnpinnedToolbarIDs;
	}

	/**
	 * Set a list of all the dockables on the south toolbar
	 *
	 * @param ids List of unpinned dockable IDs on the south toolbar
	 */
	public void setSouthUnpinnedToolbarIDs(List<String> ids) {
		southUnpinnedToolbarIDs.clear();
		southUnpinnedToolbarIDs.addAll(ids);
	}

	/**
	 * Get a list of all the dockables on the south toolbar
	 *
	 * @return List of unpinned dockable IDs on the south toolbar
	 */
	public List<String> getSouthUnpinnedToolbarIDs() {
		return southUnpinnedToolbarIDs;
	}

	public boolean hasSizeAndLocationInformation() {
		return hasSizeAndLocationInformation;
	}

	public int getWindowHashCode() {
		return windowHashCode;
	}
}
