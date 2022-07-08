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
package modern_docking.ui;

import modern_docking.Dockable;
import modern_docking.Docking;
import modern_docking.internal.DockingListeners;
import modern_docking.event.DockingListener;
import modern_docking.event.MaximizeListener;

public class HeaderController implements MaximizeListener, DockingListener {
	private final Dockable dockable;
	private final HeaderModel model;

	private DockingHeaderUI ui;

	public HeaderController(Dockable dockable, HeaderModel model) {
		this.dockable = dockable;
		this.model = model;

		DockingListeners.addMaximizeListener(this);
		DockingListeners.addDockingListener(this);
	}

	public void setUI(DockingHeaderUI ui) {
		this.ui = ui;
	}

	public void removeListeners() {
		DockingListeners.removeMaximizeListener(this);
		DockingListeners.removeDockingListener(this);
	}

	public void pinDockable() {
		Docking.pinDockable(dockable);
	}

	public void unpinDockable() {
		Docking.unpinDockable(dockable);
	}

	public void newWindow() {
		Docking.newWindow(dockable);
	}

	public void minimize() {
		Docking.minimize(dockable);
	}

	public void maximize() {
		Docking.maximize(dockable);
	}

	public void close() {
		Docking.undock(dockable);
	}

	@Override
	public void maximized(Dockable dockable, boolean maximized) {
		ui.update();
	}

	@Override
	public void docked(String persistentID) {
		ui.update();
	}

	@Override
	public void undocked(String persistentID) {
		ui.update();
	}

	@Override
	public void unpinned(String persistentID) {
		ui.update();
	}
}
