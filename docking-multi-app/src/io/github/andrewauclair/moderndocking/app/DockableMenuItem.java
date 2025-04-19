/*
Copyright (c) 2022-2025 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.app;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;

/**
 * Special JCheckBoxMenuItem that handles updating the checkbox for us based on the docking state of the dockable
 */
public class DockableMenuItem extends JCheckBoxMenuItem implements ActionListener {
	private static final Logger logger = Logger.getLogger(DockableMenuItem.class.getPackageName());

	/**
	 * Persistent ID provider. Used when the persistent ID isn't known at compile time.
	 * Used when this menu item is added to a menu (using addNotify)
	 */
	private final Supplier<String> persistentIDProvider;
	/**
	 * The persistent ID of the dockable which will be displayed when this dockable is clicked
	 */
	private final String persistentID;
	private final DockingAPI docking;

	/**
	 * Create a new DockableMenuItem
	 *
	 * @param docking The docking API instance to use for this menu item
	 * @param dockable The dockable to dock when this menu item is selected
	 */
	public DockableMenuItem(DockingAPI docking, Dockable dockable) {
		this(docking, dockable.getPersistentID(), dockable.getTabText());
	}

	/**
	 *
	 * @param docking The docking API instance to use for this menu item
	 * @param persistentID The dockable this menu item refers to
	 * @param text The display text for this menu item
	 */
	public DockableMenuItem(DockingAPI docking, String persistentID, String text) {
		super(text);

		this.docking = docking;

		this.persistentIDProvider = null;
		this.persistentID = persistentID;

		addActionListener(this);
	}

	/**
	 *
	 * @param docking The docking API instance to use for this menu item
	 * @param persistentIDProvider Provides the persistentID that will be displayed
	 * @param text The display text for this menu item
	 */
	public DockableMenuItem(DockingAPI docking, Supplier<String> persistentIDProvider, String text) {
		super(text);

		this.docking = docking;

		this.persistentIDProvider = persistentIDProvider;
		this.persistentID = "";

		addActionListener(this);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		// update the menu item, it's about to be displayed
		DockingInternal internal = DockingInternal.get(docking);

		String id = persistentIDProvider != null ? persistentIDProvider.get() : persistentID;

		if (internal.hasDockable(id)) {
			Dockable dockable = internal.getDockable(id);
			setSelected(docking.isDocked(dockable));
		}
		else {
			setVisible(false);
			logger.log(Level.INFO, "Hiding DockableMenuItem for \"" + getText() + ".\" No dockable with persistentID '" + id + "' registered.");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DockingInternal internal = DockingInternal.get(docking);

		String id = persistentIDProvider != null ? persistentIDProvider.get() : persistentID;

		if (internal.hasDockable(id)) {
			Dockable dockable = internal.getDockable(id);

			docking.display(dockable);

			// set this menu item to the state of the dockable, should be docked at this point
			setSelected(docking.isDocked(dockable));
		}
		else {
			logger.log(Level.SEVERE, "DockableMenuItem for \"" + getText() + "\" action failed. No dockable with persistentID '" + id + "' registered.");
		}
	}
}
