/*
Copyright (c) 2022-2024 Andrew Auclair

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
import ModernDocking.DockingProperty;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.ui.DefaultHeaderUI;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Internal utilities for the library
 */
public class DockingInternal {
	private final Map<String, DockableWrapper> dockables = new HashMap<>();
	private final DockingAPI docking;

	private static final Map<DockingAPI, DockingInternal> internals = new HashMap<>();

	public DockingInternal(DockingAPI docking) {
		this.docking = docking;
		internals.put(docking, this);
	}

	public static DockingInternal get(DockingAPI docking) {
		return internals.get(docking);
	}

	public static void remove(DockingAPI docking) {
		internals.remove(docking);
	}

	/**
	 * Get access to the registered dockables
	 *
	 * @return List of registered dockables
	 */
	public List<Dockable> getDockables() {
		return dockables.values().stream()
				.map(DockableWrapper::getDockable)
				.collect(Collectors.toList());
	}

	/**
	 * register a dockable with the framework
	 *
	 * @param dockable The dockable to register
	 */
	public void registerDockable(Dockable dockable) {
		if (dockables.containsKey(dockable.getPersistentID())) {
			throw new DockableRegistrationFailureException(dockable.getPersistentID());
		}
		if (dockable.getTabText() == null) {
			throw new RuntimeException("Dockable '" + dockable.getPersistentID() + "' should not return 'null' for tabText()");
		}
		validateDockingProperties(dockable);
		dockables.put(dockable.getPersistentID(), new DockableWrapper(docking, dockable));
	}

	private void validateDockingProperties(Dockable dockable) {
		List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
				.filter(field -> field.getAnnotation(DockingProperty.class) != null)
				.collect(Collectors.toList());

		for (Field field : dockingPropFields) {
			try {
				// make sure we can access the field if it is private/protected. only try this if we're sure we can't already access it
				// because it may result in an IllegalAccessException for trying
				if (!field.canAccess(dockable)) {
					field.setAccessible(true);
				}

				// grab the property and store the value by its name
				DockingProperty property = field.getAnnotation(DockingProperty.class);

				try {
					DockableProperties.validateProperty(field, property);
				}
				catch (Exception e) {
					// TODO possibly make a new DockingPropertyException
					throw new RuntimeException(String.format("Dockable: '%s' (%s), default value: '%s' for field '%s' (%s) is invalid", dockable.getPersistentID(), dockable.getClass().getSimpleName(), property.defaultValue(), field.getName(), field.getType().getSimpleName()), e);
				}
			} catch (SecurityException e) {
				// TODO handle this better
				e.printStackTrace();
			}

		}
	}

	/**
	 * Dockables must be deregistered so it can be properly disposed
	 *
	 * @param dockable The dockable to deregister
	 */
	public void deregisterDockable(Dockable dockable) {
		getWrapper(dockable).removeListeners();
		dockables.remove(dockable.getPersistentID());
	}

	// internal function to get the dockable wrapper
	public DockableWrapper getWrapper(Dockable dockable) {
		if (dockables.containsKey(dockable.getPersistentID())) {
			return dockables.get(dockable.getPersistentID());
		}
		throw new DockableRegistrationFailureException(dockable.getPersistentID());
	}

	/**
	 * Find a dockable with the given persistent ID
	 * @param persistentID persistent ID to search for
	 * @return found dockable
	 * @throws DockableRegistrationFailureException if the dockable has not been registered
	 */
	public Dockable getDockable(String persistentID) {
		if (dockables.containsKey(persistentID)) {
			return dockables.get(persistentID).getDockable();
		}
		throw new DockableRegistrationFailureException(persistentID);
	}

	public void fireDockedEventForFrame(Window window) {
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
	public static void fireDockedEventForAll(DockingAPI docking) {
		for (Dockable dockable : DockingInternal.get(docking).getDockables()) {
			if (docking.isDocked(dockable)) {
				DockingListeners.fireDockedEvent(dockable);
			}
		}
	}

	/**
	 * Force a UI update on all dockables when changing look and feel. This ensures that any dockables not part of a free (i.e. not docked)
	 * are properly updated with the new look and feel
	 */
	public void updateLAF() {
		for (DockableWrapper wrapper : dockables.values()) {
			SwingUtilities.updateComponentTreeUI(wrapper.getDisplayPanel());
		}

		for (RootDockingPanelAPI root : docking.getRootPanels().values()) {
			root.updateLAF();
			updateLAF(root.getPanel());
		}
	}

	private void updateLAF(DockingPanel panel) {
		if (panel instanceof DockedTabbedPanel) {
			ActiveDockableHighlighter.setNotSelectedBorder(panel);
		}
		else if (panel instanceof DockedSimplePanel) {
			DockedSimplePanel simplePanel = (DockedSimplePanel) panel;

			if (simplePanel.getParent() instanceof DockedTabbedPanel) {
				DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) simplePanel.getParent();

				if (tabbedPanel.getDockables().size() == 1) {
					ActiveDockableHighlighter.setNotSelectedBorder(panel);
				}
			}
			else {
				ActiveDockableHighlighter.setNotSelectedBorder(panel);
			}
		}
		else if (panel instanceof DockedSplitPanel) {
			DockedSplitPanel splitPanel = (DockedSplitPanel) panel;

			updateLAF(splitPanel.getLeft());
			updateLAF(splitPanel.getRight());
		}
	}
	public static BiFunction<HeaderController, HeaderModel, DockingHeaderUI> createHeaderUI = DefaultHeaderUI::new;

	public static DockingHeaderUI createDefaultHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return createHeaderUI.apply(headerController, headerModel);
	}
}
