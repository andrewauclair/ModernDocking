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
package modern_docking.layouts;

import javax.swing.*;
import java.awt.*;

// layout of a single frame
public class DockingLayout {
	private boolean isMainFrame;
	private final Point location;
	private final Dimension size;
	private final int state;
	private final DockingLayoutNode rootNode;
	private String maximizedDockable = null;

	public DockingLayout(boolean isMainFrame, Point location, Dimension size, int state, DockingLayoutNode rootNode) {
		this.isMainFrame = isMainFrame;
		this.location = location;
		this.size = size;
		this.state = state;
		this.rootNode = rootNode;
	}

	public DockingLayout(JFrame frame, DockingLayoutNode rootNode) {
		this.rootNode = rootNode;
		this.location = frame.getLocation();
		this.size = frame.getSize();
		this.state = frame.getExtendedState();
	}

	public boolean isMainFrame() {
		return isMainFrame;
	}

	public Point getLocation() {
		return location;
	}

	public Dimension getSize() {
		return size;
	}

	public int getState() {
		return state;
	}

	public DockingLayoutNode getRootNode() {
		return rootNode;
	}

	public void setMaximizedDockable(String persistentID) {
		maximizedDockable = persistentID;
	}

	public String getMaximizedDockable() {
		return maximizedDockable;
	}
}
