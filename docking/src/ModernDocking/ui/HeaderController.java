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
package ModernDocking.ui;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.event.DockingListener;
import ModernDocking.event.MaximizeListener;
import ModernDocking.internal.DockingListeners;

/**
 * Controller for the header of dockables. Responsible for controlling the state of all buttons on the header.
 */
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

	/**
	 * Pin the dockable
	 */
	public void pinDockable() {
		Docking.pinDockable(dockable);
	}

	/**
	 * Set the dockable to unpinned
	 */
	public void unpinDockable() {
		Docking.unpinDockable(dockable);
	}

	/**
	 * Launch the dockable in a new window
	 */
	public void newWindow() {
		Docking.newWindow(dockable);
	}

	/**
	 * Minimize the dockable
	 */
	public void minimize() {
		Docking.minimize(dockable);
	}

	/**
	 * Maximize the dockable
	 */
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
	public void onDocked(String persistentID) {
		ui.update();
	}

	@Override
	public void onUndocked(String persistentID) {
		ui.update();
	}

	@Override
	public void onUnpinned(String persistentID) {
		ui.update();
	}

	@Override
	public void onShown(String persistentID) {
	}

	@Override
	public void onHidden(String persistentID) {
	}
}
