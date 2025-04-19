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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.exception.DockableNotFoundException;
import io.github.andrewauclair.moderndocking.exception.DockableRegistrationFailureException;
import io.github.andrewauclair.moderndocking.exception.RootDockingPanelRegistrationFailureException;
import io.github.andrewauclair.moderndocking.floating.Floating;
import io.github.andrewauclair.moderndocking.ui.DefaultHeaderUI;
import io.github.andrewauclair.moderndocking.ui.DockingHeaderUI;
import io.github.andrewauclair.moderndocking.ui.HeaderController;
import io.github.andrewauclair.moderndocking.ui.HeaderModel;
import java.awt.Dimension;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Internal utilities for the library
 */
public class DockingInternal {
	private final Map<String, DockableWrapper> anchors = new HashMap<>();
	private final Map<String, DockableWrapper> dockables = new HashMap<>();
	private final DockingAPI docking;

	private final Map<Window, InternalRootDockingPanel> rootPanels = new HashMap<>();

	private static final Map<DockingAPI, DockingInternal> internals = new HashMap<>();

	private final AppStatePersister appStatePersister;

	/**
	 * Create a new instance of our internal helper for the docking instance
	 *
	 * @param docking Docking instance
	 */
	public DockingInternal(DockingAPI docking) {
		this.docking = docking;
		this.appStatePersister = new AppStatePersister(docking);
		internals.put(docking, this);
	}

	/**
	 * Lookup the internal helper for a docking instance
	 *
	 * @param docking The instance to find a helper for
	 * @return The internal helper or null if not found
	 */
	public static DockingInternal get(DockingAPI docking) {
		return internals.get(docking);
	}

	/**
	 * Remove an internal helper
	 *
	 * @param docking The docking instance
	 */
	public static void remove(DockingAPI docking) {
		internals.remove(docking);
	}

	/**
	 * Get a map of RootDockingPanels to their Windows
	 *
	 * @return map of root panels
	 */
	public Map<Window, InternalRootDockingPanel> getRootPanels() {
		return rootPanels;
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
	 * registration function for DockingPanel
	 *
	 * @param panel Panel to register
	 * @param parent The parent frame of the panel
	 */
	public void registerDockingPanel(RootDockingPanelAPI panel, JFrame parent) {
		if (rootPanels.containsKey(parent)) {
			throw new RootDockingPanelRegistrationFailureException(panel, parent);
		}

		Optional<Window> window = rootPanels.entrySet().stream()
				.filter(entry -> entry.getValue().getRootPanel() == panel)
				.findFirst()
				.map(Map.Entry::getKey);

		if (window.isPresent()) {
			throw new RootDockingPanelRegistrationFailureException(panel, window.get());
		}

		InternalRootDockingPanel internalRoot = new InternalRootDockingPanel(docking, panel);
		rootPanels.put(parent, internalRoot);
		Floating.registerDockingWindow(docking, parent, internalRoot);

		appStatePersister.addWindow(parent);
	}

	/**
	 * Register a RootDockingPanel
	 *
	 * @param panel RootDockingPanel to register
	 * @param parent The parent JDialog of the panel
	 */
	public void registerDockingPanel(RootDockingPanelAPI panel, JDialog parent) {
		if (rootPanels.containsKey(parent)) {
			throw new RootDockingPanelRegistrationFailureException(panel, parent);
		}

		Optional<Window> window = rootPanels.entrySet().stream()
				.filter(entry -> entry.getValue().getRootPanel() == panel)
				.findFirst()
				.map(Map.Entry::getKey);

		if (window.isPresent()) {
			throw new RootDockingPanelRegistrationFailureException(panel, parent);
		}

		InternalRootDockingPanel internalRoot = new InternalRootDockingPanel(docking, panel);
		rootPanels.put(parent, internalRoot);
		Floating.registerDockingWindow(docking, parent, internalRoot);

		appStatePersister.addWindow(parent);
	}

	/**
	 * Deregister a docking root panel
	 *
	 * @param parent The parent of the panel that we're deregistering
	 */
	public void deregisterDockingPanel(Window parent) {
		if (rootPanels.containsKey(parent)) {
			InternalRootDockingPanel root = rootPanels.get(parent);

			DockingComponentUtils.undockComponents(docking, root);
		}

		rootPanels.remove(parent);
		Floating.deregisterDockingWindow(parent);

		appStatePersister.removeWindow(parent);
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
		dockables.put(dockable.getPersistentID(), new DockableWrapper(docking, dockable, false));
	}

	/**
	 * Register an anchor with the framework
	 *
	 * @param anchor The anchor to register
	 */
	public void registerDockingAnchor(Dockable anchor) {
		if (anchors.containsKey(anchor.getPersistentID())) {
			throw new DockableRegistrationFailureException(anchor.getPersistentID());
		}
		anchors.put(anchor.getPersistentID(), new DockableWrapper(docking, anchor, true));
	}

	/**
	 * Deregister an anchor with the framework
	 *
	 * @param anchor The anchor to deregister
	 */
	public void deregisterDockingAnchor(Dockable anchor) {
		anchors.remove(anchor.getPersistentID());
	}

	private void validateDockingProperties(Dockable dockable) {
		List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
				.filter(field -> field.getAnnotation(DockingProperty.class) != null)
				.collect(Collectors.toList());

		if (dockingPropFields.size() > 0) {
			try {
				Method updateProperties = dockable.getClass().getMethod("updateProperties");
				if (updateProperties.getDeclaringClass() == Dockable.class) {
					throw new RuntimeException("Dockable class " + dockable.getClass().getSimpleName() + " contains DockingProperty instances and should override updateProperties");
				}
			} catch (NoSuchMethodException ignored) {
				// updateProperties has a default implementation in Dockable, so we will always find it and this exception should never happen
			}
		}

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
		if (anchors.containsKey(dockable.getPersistentID())) {
			return anchors.get(dockable.getPersistentID());
		}
		throw new DockableNotFoundException(dockable.getPersistentID());
	}

	public boolean hasDockable(String persistentID) {
		return dockables.containsKey(persistentID);
	}

	public boolean hasAnchor(String persistentID) {
		return anchors.containsKey(persistentID);
	}

	/**
	 * Find a dockable with the given persistent ID
	 * @param persistentID persistent ID to search for
	 * @return found dockable
	 * @throws DockableNotFoundException if the dockable has not been found
	 */
	public Dockable getDockable(String persistentID) {
		if (dockables.containsKey(persistentID)) {
			return dockables.get(persistentID).getDockable();
		}
		// TODO I'm not 100% sure about this one
		if (anchors.containsKey(persistentID)) {
			return anchors.get(persistentID).getDockable();
		}
		throw new DockableNotFoundException(persistentID);
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
	 *
	 * @param docking The docking instance
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
			wrapper.getHeaderUI().update();
		}

		for (DockableWrapper wrapper : anchors.values()) {
			SwingUtilities.updateComponentTreeUI(wrapper.getDisplayPanel());
		}

		for (InternalRootDockingPanel root : rootPanels.values()) {
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
		if (panel != null) {
			SwingUtilities.updateComponentTreeUI(panel);
		}
	}

	/**
	 * Function used to create a new instance of the Header UI. This allows us to replace the function in the Modern Docking UI Extension
	 */
	public static BiFunction<HeaderController, HeaderModel, DockingHeaderUI> createHeaderUI = DefaultHeaderUI::new;

	/**
	 * Create an instance of the default header specified with createHeaderUI
	 *
	 * @param headerController The header controller to use for the docking header
	 * @param headerModel The header model to use for the docking heade
	 *
	 * @return A new instance of a DockingHeaderUI
	 */
	public static DockingHeaderUI createDefaultHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return createHeaderUI.apply(headerController, headerModel);
	}

	/**
	 * Get the wrapper for an anchor
	 *
	 * @param anchor Anchor Persistent ID
	 *
	 * @return Anchor wrapper or null
	 */
	public DockableWrapper getAnchor(String anchor) {
		return anchors.get(anchor);
	}

	public void dockToLargestAnchorPanel(Dockable dockable, String anchor) {
		Optional<Dockable> biggest = docking.getDockables().stream()
				.filter(d -> docking.isDocked(d))
				.filter(d -> getWrapper(d).getAnchor() == anchor)
				.sorted((o1, o2) -> {
                    Dimension sizeA = getWrapper(o1).getParent().getSize();
                    Dimension sizeB = getWrapper(o2).getParent().getSize();

                    return Integer.compare(sizeB.height * sizeB.width, sizeA.height * sizeA.width);
                })
				.findFirst();

		if (biggest.isPresent()) {
			docking.dock(dockable, biggest.get(), DockingRegion.CENTER);
		}
	}
}
