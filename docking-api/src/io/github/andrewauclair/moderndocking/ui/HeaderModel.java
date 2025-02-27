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

import javax.swing.*;

/**
 * Model for the header of a dockable. Provides wrapper access to functions in the dockable
 */
public class HeaderModel {
	/**
	 * Dockable for this model
	 */
	public final Dockable dockable;

	private final DockingAPI docking;

	/**
	 * Create a new model for the given dockable
	 *
	 * @param dockable Dockable for the model
	 */
	public HeaderModel(Dockable dockable, DockingAPI docking) {
		this.dockable = dockable;
		this.docking = docking;
	}

	/**
	 * Get the title text of dockable
	 *
	 * @return Title text
	 */
	public String titleText() {
		return dockable.getTitleText();
	}

	/**
	 * Get the icon to display for the dockable
	 *
	 * @return Dockable icon
	 */
	public Icon icon() {
		return dockable.getIcon();
	}

	/**
	 * Check if pinning is allowed for this dockable
	 *
	 * @return Is pinning allowed?
	 */
	public boolean isAutoHideAllowed() {
		return dockable.isAutoHideAllowed();
	}

	public boolean isAutoHideEnabled() {
		return docking.isHidden(dockable);
	}

	/**
	 * helper function to determine if the header min/max option should be enabled
	 *
	 * @return Is min/max allowed for this dockable
	 */
	public boolean isMaximizeAllowed() {
		return dockable.isMinMaxAllowed();
	}

	public boolean isMaximized() {
		return docking.isMaximized(dockable);
	}

	public boolean isCloseAllowed() {
		return dockable.isClosable();
	}

	/**
	 *
	 * @return True if there are more options to add to the context menu
	 */
	public boolean hasMoreOptions() {
		return dockable.getHasMoreOptions();
	}

	public boolean isFloatingAllowed() {
		return dockable.isFloatingAllowed();
	}

	/**
	 * Add the extra options to the context menu
	 *
	 * @param menu Menu to add options to
	 */
	public void addMoreOptions(JPopupMenu menu) {
		dockable.addMoreOptions(menu);
	}
}
