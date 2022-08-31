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

public class HeaderModel {
	private final Dockable dockable;

	public HeaderModel(Dockable dockable) {
		this.dockable = dockable;
	}

	public void update() {

	}

	public String titleText() {
		return dockable.tabText();
	}

	public boolean isPinnedAllowed() {
		return dockable.allowPinning();
	}

	public boolean isPinned() {
		return false;
	}

	public boolean isUnpinnedAllowed() {
		return dockable.allowPinning();
	}

	public boolean isUnpinned() {
		return Docking.isUnpinned(dockable);
	}

	public boolean isMaximizeAllowed() {
		return dockable.allowMinMax();
	}

	public boolean isMaximized() {
		return Docking.isMaximized(dockable);
	}

	public boolean isCloseAllowed() {
		return dockable.allowClose();
	}

	public boolean hasMoreOptions() {
		return dockable.hasMoreOptions();
	}

	public boolean isFloatingAllowed() {
		return dockable.floatingAllowed();
	}

	public void addMoreOptions(JPopupMenu menu) {
		dockable.addMoreOptions(menu);
	}
}
