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
package io.github.andrewauclair.moderndocking.ui;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import io.github.andrewauclair.moderndocking.event.MaximizeListener;
import io.github.andrewauclair.moderndocking.internal.DockingListeners;

/**
 * Controller for the header of dockables. Responsible for controlling the state of all buttons on the header.
 */
public class HeaderController implements MaximizeListener, DockingListener {
	/**
	 * The dockable this header controller references
	 */
	private final Dockable dockable;
	/**
	 * The docking instance the dockable belongs to
	 */
	private final DockingAPI docking;

	/**
	 * The header model controlled
	 */
	private final HeaderModel model;

	/**
	 * The header UI
	 */
	private DockingHeaderUI ui;

	/**
	 * Create a new header controller for a dockable
	 *
	 * @param dockable The dockable this header controller uses
	 * @param docking The docking instance the dockable belongs to
	 * @param model The header model for the dockable
	 */
	public HeaderController(Dockable dockable, DockingAPI docking, HeaderModel model) {
		this.dockable = dockable;
		this.docking = docking;
		this.model = model;

		DockingListeners.addMaximizeListener(this);
		DockingListeners.addDockingListener(this);
	}

	/**
	 * Change the header UI we're controlling
	 *
	 * @param ui New header UI
	 */
	public void setUI(DockingHeaderUI ui) {
		this.ui = ui;
	}

	/**
	 * Remove the docking listeners that we've added
	 */
	public void removeListeners() {
		DockingListeners.removeMaximizeListener(this);
		DockingListeners.removeDockingListener(this);
	}

	/**
	 * Flip the auto-hide flag
	 */
	public void toggleAutoHide() {
		if (model.isAutoHideEnabled()) {
			docking.autoShowDockable(dockable);
		}
		else {
			docking.autoHideDockable(dockable);
		}
	}

	/**
	 * Launch the dockable in a new window
	 */
	public void newWindow() {
		docking.newWindow(dockable);
	}

	/**
	 * Minimize the dockable
	 */
	public void minimize() {
		docking.minimize(dockable);
	}

	/**
	 * Maximize the dockable
	 */
	public void maximize() {
		docking.maximize(dockable);
	}

	/**
	 * Close the dockable
	 */
	public void close() {
		if (dockable.requestClose()) {
			docking.undock(dockable);
		}
	}

	@Override
	public void maximized(Dockable dockable, boolean maximized) {
		ui.update();
	}

	@Override
	public void dockingChange(DockingEvent e) {
		ui.update();
	}
}
