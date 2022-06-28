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
package docking;

import event.DockingListener;
import event.MaximizeListener;

import java.util.ArrayList;
import java.util.List;

public class DockingListeners {
	private static final List<MaximizeListener> maximizeListeners = new ArrayList<>();
	private static final List<DockingListener> dockingListeners = new ArrayList<>();

	public static void addMaximizeListener(MaximizeListener listener) {
		if (!maximizeListeners.contains(listener)) {
			maximizeListeners.add(listener);
		}
	}

	public static void removeMaximizeListener(MaximizeListener listener) {
		maximizeListeners.remove(listener);
	}

	// package private function to fire an event
	static void fireMaximizeEvent(Dockable dockable, boolean maximized) {
		maximizeListeners.forEach(listener -> listener.maximized(dockable, maximized));
	}

	public static void addDockingListener(DockingListener listener) {
		if (!dockingListeners.contains(listener)) {
			dockingListeners.add(listener);
		}
	}

	public static void removeDockingListener(DockingListener listener) {
		dockingListeners.remove(listener);
	}

	// package private function to fire docked event
	static void fireDockedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.docked(dockable.persistentID()));
	}

	// package private function to fire undocked event
	static void fireUndockedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.undocked(dockable.persistentID()));
	}

	// package private function to fire auto undocked event
	static void fireAutoUndockedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.autoUndocked(dockable.persistentID()));
	}

	static void fireUnpinnedEvent(Dockable dockable) {
		dockingListeners.forEach(listener -> listener.unpinned(dockable.persistentID()));
	}
}
