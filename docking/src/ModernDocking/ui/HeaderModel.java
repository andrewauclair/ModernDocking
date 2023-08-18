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

import javax.swing.*;

/**
 * Model for the header of a dockable. Provides wrapper access to functions in the dockable
 */
public class HeaderModel {
	/**
	 * Dockable for this model
	 */
	public final Dockable dockable;

	/**
	 * Create a new model for the given dockable
	 *
	 * @param dockable Dockable for the model
	 */
	public HeaderModel(Dockable dockable) {
		this.dockable = dockable;
	}

	/**
	 * Get the title text of dockable
	 *
	 * @return Title text
	 */
	public String titleText() {
		return dockable.getTabText();
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
	public boolean isPinnedAllowed() {
		return dockable.getAllowPinning();
	}

	// TODO can this be removed?
	public boolean isPinned() {
		return false;
	}

	public boolean isUnpinnedAllowed() {
		return dockable.getAllowPinning();
	}

	public boolean isUnpinned() {
		return Docking.isUnpinned(dockable);
	}

	/**
	 * helper function to determine if the header min/max option should be enabled
	 *
	 * @return Is min/max allowed for this dockable
	 */
	public boolean isMaximizeAllowed() {
		return dockable.getAllowMinMax();
	}

	public boolean isMaximized() {
		return Docking.isMaximized(dockable);
	}

	public boolean isCloseAllowed() {
		return dockable.getCanBeClosed();
	}

	/**
	 *
	 * @return True if there are more options to add to the context menu
	 */
	public boolean hasMoreOptions() {
		return dockable.getHasMoreOptions();
	}

	public boolean isFloatingAllowed() {
		return dockable.getFloatingAllowed();
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
