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
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import io.github.andrewauclair.moderndocking.event.MaximizeListener;
import io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 * Manager class for docking and maximize listeners
 */
public class DockingListeners {
	private static final List<MaximizeListener> maximizeListeners = new ArrayList<>();
	private static final List<DockingListener> dockingListeners = new ArrayList<>();
	private static final List<NewFloatingFrameListener> newFloatingFrameListeners = new ArrayList<>();

	/**
	 * Unused. All methods are static
	 */
	private DockingListeners() {
	}

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
		List<MaximizeListener> listeners = new ArrayList<>(maximizeListeners);
		listeners.forEach(listener -> listener.maximized(dockable, maximized));
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
	 * Add a new floating frame listener
	 *
	 * @param listener Listener to add
	 */
	public static void addNewFloatingFrameListener(NewFloatingFrameListener listener) {
		newFloatingFrameListeners.add(listener);
	}

	/**
	 * Remove a floating frame listener
	 *
	 * @param listener Listener to remove
	 */
	public static void removeNewFloatingFrameListener(NewFloatingFrameListener listener) {
		newFloatingFrameListeners.remove(listener);
	}

	/**
	 * Fire a new floating frame event
	 *
	 * @param frame The frame that was created
	 * @param root The root of the frame
	 */
	public static void fireNewFloatingFrameEvent(JFrame frame, RootDockingPanelAPI root) {
		List<NewFloatingFrameListener> listeners = new ArrayList<>(newFloatingFrameListeners);
		listeners.forEach(listener -> listener.newFrameCreated(frame, root));
	}

	/**
	 * Fire a new floating frame event
	 *
	 * @param frame The frame that was created
	 * @param root The root of the frame
	 * @param dockable The dockable in the frame
	 */
	public static void fireNewFloatingFrameEvent(JFrame frame, RootDockingPanelAPI root, Dockable dockable) {
		List<NewFloatingFrameListener> listeners = new ArrayList<>(newFloatingFrameListeners);
		listeners.forEach(listener -> listener.newFrameCreated(frame, root, dockable));
	}

	/**
	 * Fire a new docked event
	 *
	 * @param dockable Dockable that was docked
	 */
	public static void fireDockedEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.DOCKED, dockable)));
	}

	/**
	 * Fire a new undocked event
	 *
	 * @param dockable Dockable that was undocked
	 */
	public static void fireUndockedEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.UNDOCKED, dockable)));
	}

	/**
	 * Fire a new auto hide enabled event
	 *
	 * @param dockable Dockable that was auto hide enabled
	 */
	public static void fireAutoShownEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.AUTO_HIDE_ENABLED, dockable)));
	}

	/**
	 * Fire a new auto hide disabled event
	 *
	 * @param dockable Dockable that was auto hide disabled
	 */
	public static void fireAutoHiddenEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.AUTO_HIDE_DISABLED, dockable)));
	}

	/**
	 * Fire a new shown event
	 *
	 * @param dockable Dockable that was shown
	 */
	public static void fireShownEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.SHOWN, dockable)));
	}

	/**
	 * Fire a new hidden event
	 *
	 * @param dockable Dockable that was hidden
	 */
	public static void fireHiddenEvent(Dockable dockable) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(new DockingEvent(DockingEvent.ID.HIDDEN, dockable)));
	}

	/**
	 * Fire a new docking event
	 *
	 * @param e Docking event to fire
	 */
	public static void fireDockingEvent(DockingEvent e) {
		List<DockingListener> listeners = new ArrayList<>(dockingListeners);
		listeners.forEach(listener -> listener.dockingChange(e));
	}
}
