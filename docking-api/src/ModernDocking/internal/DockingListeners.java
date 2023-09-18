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
import ModernDocking.event.DockingEvent;
import ModernDocking.event.DockingListener;
import ModernDocking.event.MaximizeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for docking and maximize listeners
 */
public class DockingListeners {
	private static final List<MaximizeListener> maximizeListeners = new ArrayList<>();
	private static final List<DockingListener> dockingListeners = new ArrayList<>();

	/**
	 * Add a new maximize listener. Will be called when a dockable is maximized
	 *
	 * @param listener Listener to add
	 */
	public static void addMaximizeListener(MaximizeListener listener) {
		if (!maximizeListeners.contains(listener)) {
			maximizeListeners.add(listener);
		}
	}

	/**
	 * Remove a previously added maximize listener. No-op if the listener isn't in the list
	 *
	 * @param listener Listener to remove
	 */
	public static void removeMaximizeListener(MaximizeListener listener) {
		maximizeListeners.remove(listener);
	}

	/**
	 * Fire a new maximize event
	 *
	 * @param dockable Dockable that has changed
	 * @param maximized New maximized state
	 */
	public static void fireMaximizeEvent(Dockable dockable, boolean maximized) {
		maximizeListeners.forEach(listener -> listener.maximized(dockable, maximized));
	}

	/**
	 * Add a new docking listener
	 *
	 * @param listener Listener to add
	 */
	public static void addDockingListener(DockingListener listener) {
		if (!dockingListeners.contains(listener)) {
			dockingListeners.add(listener);
		}
	}

	/**
	 * Remove a docking listener
	 *
	 * @param listener Listener to remove
	 */
	public static void removeDockingListener(DockingListener listener) {
		dockingListeners.remove(listener);
	}

	/**
	 * Fire a new docked event
	 *
	 * @param dockable Dockable that was docked
	 */
	public static void fireDockedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.DOCKED, dockable)));
	}

	/**
	 * Fire a new undocked event
	 *
	 * @param dockable Dockable that was undocked
	 */
	public static void fireUndockedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.UNDOCKED, dockable)));
	}

	/**
	 * Fire a new pinned event
	 *
	 * @param dockable Dockable that was pinned
	 */
	public static void firePinnedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.PINNED, dockable)));
	}

	/**
	 * Fire a new unpinned event
	 *
	 * @param dockable Dockable that was unpinned
	 */
	public static void fireUnpinnedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.UNPINNED, dockable)));
	}

	/**
	 * Fire a new shown event
	 *
	 * @param dockable Dockable that was shown
	 */
	public static void fireShownEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.SHOWN, dockable)));
	}

	/**
	 * Fire a new hidden event
	 *
	 * @param dockable Dockable that was hidden
	 */
	public static void fireHiddenEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.HIDDEN, dockable)));
	}

	public static void fireDockingEvent(DockingEvent e) {
		dockingListeners.forEach(listener -> listener.dockingChange(e));
	}
}
