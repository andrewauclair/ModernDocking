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
package docking;

import floating.FloatListener;

// internal wrapper around the Dockable implemented by the application.
// lets us provide access to the dockable and its parent in the hierarchy
public class DockableWrapper {
	private DockingPanel parent = null;
	private final Dockable dockable;

	private FloatListener floatListener;

	private boolean maximized = false;

	public DockableWrapper(Dockable dockable) {
		this.dockable = dockable;
		floatListener = new FloatListener(this);
	}

	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	public Dockable getDockable() {
		return dockable;
	}

	public void removedListeners() {
		if (floatListener != null) {
			floatListener.removeListeners();
			floatListener = null;
		}
	}

	public DockingPanel getParent() {
		return parent;
	}

	public boolean isMaximized() {
		return maximized;
	}

	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}
}
