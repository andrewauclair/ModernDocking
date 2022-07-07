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
import modern_docking.DockingRegion;
import modern_docking.internal.DockingInternal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Special JCheckBoxMenuItem that handles updating the checkbox for us based on the docking state of the dockable
public class DockableMenuItem extends JCheckBoxMenuItem implements ActionListener {
	private final String persistentID;
	private final JFrame frame;

	/**
	 *
	 * @param persistentID The dockable this menu item refers to
	 * @param text The display text for this menu item
	 * @param frame The frame to display this dockable on by default
	 */
	public DockableMenuItem(String persistentID, String text, JFrame frame) {
		super(text);

		this.persistentID = persistentID;
		this.frame = frame;

		addActionListener(this);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		// update the menu item, it's about to be displayed
		Dockable dockable = DockingInternal.getDockable(persistentID);
		setSelected(Docking.isDocked(dockable));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Dockable dockable = DockingInternal.getDockable(persistentID);

		// if dockable is already docked then bring it to the front
		// else, dock it
		if (Docking.isDocked(dockable)) {
			Docking.bringToFront(dockable);
		}
		else {
			Docking.dock(dockable, frame, DockingRegion.SOUTH);
		}

		// set this menu item to the state of the dockable, should be docked at this point
		setSelected(Docking.isDocked(dockable));
	}
}
