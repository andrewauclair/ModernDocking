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
import javax.swing.JPanel;

/**
 * Represents a dockable that has failed to load from a layout file.
 * <p>
 * These are stripped out after restoring from the layout.
 */
public class FailedDockable extends JPanel implements Dockable {
	/**
	 * The docking instance this failed dockable belongs to
	 */
	private final DockingAPI docking;
	/**
	 * Persistent ID that no dockable has been registered for
	 */
	private final String persistentID;

	/**
	 * Create a new FailedDockable
	 *
	 * @param docking The docking instance this failed dockable belongs to
	 * @param persistentID Persistent ID that has failed to load
	 */
	public FailedDockable(DockingAPI docking, String persistentID) {
		this.docking = docking;
		this.persistentID = persistentID;

		docking.registerDockable(this);
	}

	/**
	 * Deregister this dockable
	 */
	public void destroy() {
		docking.deregisterDockable(this);
	}

	@Override
	public String getPersistentID() {
		return persistentID;
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public String getTabText() {
		return "Failed";
	}

	@Override
	public boolean isFloatingAllowed() {
		return false;
	}

	@Override
	public boolean isClosable() {
		return false;
	}
}
