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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.exception.DockableRegistrationFailureException;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal utilities for the library
 */
public class DockingInternal {
	private static final Map<String, DockableWrapper> dockables = new HashMap<>();

	/**
	 * register a dockable with the framework
	 *
	 * @param dockable The dockable to register
	 */
	public static void registerDockable(Dockable dockable) {
		if (dockables.containsKey(dockable.getPersistentID())) {
			throw new DockableRegistrationFailureException("Registration for Dockable failed. Persistent ID " + dockable.getPersistentID() + " already exists.");
		}
		if (dockable.getTabText() == null) {
			throw new RuntimeException("Dockable '" + dockable.getPersistentID() + "' should not return 'null' for tabText()");
		}
		dockables.put(dockable.getPersistentID(), new DockableWrapper(dockable));
	}

	/**
	 * Dockables must be deregistered so it can be properly disposed
	 *
	 * @param dockable The dockable to deregister
	 */
	public static void deregisterDockable(Dockable dockable) {
		getWrapper(dockable).removedListeners();
		dockables.remove(dockable.getPersistentID());
	}

	// internal function to get the dockable wrapper
	public static DockableWrapper getWrapper(Dockable dockable) {
		if (dockables.containsKey(dockable.getPersistentID())) {
			return dockables.get(dockable.getPersistentID());
		}
		throw new DockableRegistrationFailureException("Dockable with Persistent ID " + dockable.getPersistentID() + " has not been registered.");
	}

	/**
	 * Find a dockable with the given persistent ID
	 * @param persistentID persistent ID to search for
	 * @return found dockable
	 * @throws DockableRegistrationFailureException if the dockable has not been registered
	 */
	public static Dockable getDockable(String persistentID) {
		if (dockables.containsKey(persistentID)) {
			return dockables.get(persistentID).getDockable();
		}
		throw new DockableRegistrationFailureException("Dockable with Persistent ID " + persistentID + " has not been registered.");
	}

	public static void fireDockedEventForFrame(Window window) {
		// everything has been restored, go through the list of dockables and fire docked events for the ones that are docked
		List<DockableWrapper> wrappers = dockables.values().stream()
				.filter(wrapper -> wrapper.getWindow() == window)
				.collect(Collectors.toList());

		for (DockableWrapper wrapper : wrappers) {
			DockingListeners.fireDockedEvent(wrapper.getDockable());
		}
	}

	/**
	 * everything has been restored, go through the list of dockables and fire docked events for the ones that are docked
	 */
	public static void fireDockedEventForAll() {
		for (DockableWrapper wrapper : dockables.values()) {
			if (Docking.isDocked(wrapper.getDockable())) {
				DockingListeners.fireDockedEvent(wrapper.getDockable());
			}
		}
	}

	/**
	 * Force a UI update on all dockables when changing look and feel. This ensures that any dockables not part of a free (i.e. not docked)
	 * are properly updated with the new look and feel
	 */
	public static void updateLAF() {
		for (DockableWrapper wrapper : dockables.values()) {
			SwingUtilities.updateComponentTreeUI(wrapper.getDisplayPanel());
		}
	}
}
