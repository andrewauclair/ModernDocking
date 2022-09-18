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
package ModernDocking;

import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.exception.NotDockedException;
import ModernDocking.floating.FloatListener;
import ModernDocking.internal.*;
import ModernDocking.layouts.*;
import ModernDocking.persist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static ModernDocking.internal.DockingInternal.getDockable;
import static ModernDocking.internal.DockingInternal.getWrapper;

// TODO find a good solution for where to dock new dockables. For example, I might select a view menu item which docks a certain dockable, that dockable should go in a logical location which is entirely app dependent (might depend on what other dockables are docked)

// TODO support pinning dockables, this should also support icons so that text or an icon can be shown on the toolbar button

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	// cached size of the decorated frame border size
	public static Insets frameBorderSizes = new Insets(0, 0, 0, 0);

	// map of all the root panels in the application
	private final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	// the applications main frame
	private final JFrame mainFrame;

	private static Docking instance;

	private final ActiveDockableHighlighter activeDockableHighlighter = new ActiveDockableHighlighter();

	private final AppStatePersister appStatePersister = new AppStatePersister();

	/**
	 * Create the one and only instance of the Docking class for the application
	 * @param mainFrame The main frame of the application
	 */
	public Docking(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		instance = this;

		FloatListener.reset();
	}

	public static Docking getInstance() {
		return instance;
	}

	public Map<JFrame, RootDockingPanel> getRootPanels() {
		return rootPanels;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	// register a dockable with the framework
	public static void registerDockable(Dockable dockable) {
		DockingInternal.registerDockable(dockable);
	}

	// Dockables must be deregistered so it can be properly disposed
	public static void deregisterDockable(Dockable dockable) {
		DockingInternal.deregisterDockable(dockable);
	}

	// registration function for DockingPanel
	public static void registerDockingPanel(RootDockingPanel panel, JFrame parent) {
		// calculate the frame border size, used when dropping a dockable and changing from an undecorated frame (TempFloatingFrame) to a FloatingFrame
		if (frameBorderSizes.top == 0) {
			parent.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					Point location = parent.getLocation();
					Point contentsLocation = parent.getContentPane().getLocation();

					// convert content point to screen, location is already in screen coordinates because it's the location of a frame
					SwingUtilities.convertPointToScreen(contentsLocation, parent.getContentPane().getParent());

					Dimension size = parent.getSize();
					Dimension contentsSize = parent.getContentPane().getSize();

					// frame border size is the difference between the content's location and size on screen and the frame's location and size on screen
					int top = contentsLocation.y - location.y;
					int left = contentsLocation.x - location.x;
					frameBorderSizes = new Insets(top, left, size.height - contentsSize.height - top, size.width - contentsSize.width - left);

					// finally, remove this listener now that we've calculated the size
					parent.removeComponentListener(this);
				}
			});
		}

		if (instance.rootPanels.containsKey(parent)) {
			throw new DockableRegistrationFailureException("RootDockingPanel already registered for frame: " + parent);
		}

		instance.rootPanels.put(parent, panel);
		FloatListener.registerDockingFrame(parent, panel);

		instance.appStatePersister.addFrame(parent);
	}

	// allows the user to configure pinning per frame. by default pinning is only enabled on the frames the docking framework creates
	public static void configurePinning(JFrame frame, int layer, boolean allow) {
		if (!instance.rootPanels.containsKey(frame)) {
			throw new DockableRegistrationFailureException("No root panel for frame has been registered.");
		}

		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);
		root.setPinningSupported(allow);
		root.setPinningLayer(layer);
	}

	public static boolean pinningAllowed(Dockable dockable) {
		RootDockingPanel root = DockingComponentUtils.rootForFrame(DockingComponentUtils.findFrameForDockable(dockable));

		return dockable.allowPinning() && root.isPinningSupported();
	}

	public static void deregisterDockingPanel(JFrame parent) {
		if (instance.rootPanels.containsKey(parent)) {
			RootDockingPanel root = instance.rootPanels.get(parent);

			DockingComponentUtils.undockComponents(root);
		}

		instance.rootPanels.remove(parent);

		instance.appStatePersister.removeFrame(parent);
	}

	public static void dock(String persistentID, JFrame frame) {
		dock(getDockable(persistentID), frame, DockingRegion.CENTER);
	}

	public static void dock(Dockable dockable, JFrame frame) {
		dock(dockable, frame, DockingRegion.CENTER);
	}

	public static void dock(String persistentID, JFrame frame, DockingRegion region) {
		dock(getDockable(persistentID), frame, region, 0.5);
	}

	public static void dock(Dockable dockable, JFrame frame, DockingRegion region) {
		dock(dockable, frame, region, 0.5);
	}

	public static void dock(String persistentID, JFrame frame, DockingRegion region, double dividerProportion) {
		dock(getDockable(persistentID), frame, region, dividerProportion);
	}

	public static void dock(Dockable dockable, JFrame frame, DockingRegion region, double dividerProportion) {
		RootDockingPanel root = instance.rootPanels.get(frame);

		if (root == null) {
			throw new DockableRegistrationFailureException("Frame does not have a RootDockingPanel: " + frame);
		}

		root.dock(dockable, region, dividerProportion);

		getWrapper(dockable).setFrame(frame);

		// fire a docked event when the component is actually added
		DockingListeners.fireDockedEvent(dockable);

		AppState.persist();
	}

	public static void dock(String source, String target, DockingRegion region) {
		dock(getDockable(source), getDockable(target), region, 0.5);
	}

	public static void dock(String source, Dockable target, DockingRegion region) {
		dock(getDockable(source), target, region, 0.5);
	}

	public static void dock(Dockable source, String target, DockingRegion region) {
		dock(source, getDockable(target), region, 0.5);
	}

	public static void dock(Dockable source, Dockable target, DockingRegion region) {
		dock(source, target, region, 0.5);
	}

	public static void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion) {
		if (!isDocked(target)) {
			throw new NotDockedException(target);
		}
		DockableWrapper wrapper = getWrapper(target);

		wrapper.getParent().dock(source, region, dividerProportion);

		getWrapper(source).setFrame(wrapper.getFrame());

		DockingListeners.fireDockedEvent(source);

		AppState.persist();
	}

	public static void newWindow(Dockable dockable) {
		Point location = getWrapper(dockable).getDisplayPanel().getLocationOnScreen();
		Dimension size = getWrapper(dockable).getDisplayPanel().getSize();

		location.x -= frameBorderSizes.left;
		location.y -= frameBorderSizes.top;

		size.width += frameBorderSizes.left + frameBorderSizes.right;
		size.height += frameBorderSizes.top + frameBorderSizes.bottom;

		FloatingFrame frame = new FloatingFrame(location, size, JFrame.NORMAL);

		undock(dockable);
		dock(dockable, frame);
	}

	public static void bringToFront(Dockable dockable) {
		if (!isDocked(dockable)) {
			throw new NotDockedException(dockable);
		}

		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);

		if (getWrapper(dockable).getParent() instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) getWrapper(dockable).getParent();
			tabbedPanel.bringToFront(dockable);
		}
	}

	public static void undock(String persistentID) {
		undock(getDockable(persistentID));
	}

	public static void undock(Dockable dockable) {
		if (!isDocked(dockable)) {
			// nothing to undock
			return;
		}

		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);

		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		DockableWrapper wrapper = getWrapper(dockable);

		if (isUnpinned(dockable)) {
			root.undock(dockable);
			wrapper.setParent(null);
			wrapper.setUnpinned(false);
		}
		else {
			wrapper.getParent().undock(dockable);
		}
		wrapper.setFrame(null);

		DockingListeners.fireUndockedEvent(dockable);

		if (frame != null && root != null && canDisposeFrame(frame) && root.isEmpty()) {
			deregisterDockingPanel(frame);
			frame.dispose();
		}

		AppState.persist();
	}

	public static boolean isDocked(String persistentID) {
		return isDocked(getDockable(persistentID));
	}

	public static boolean isDocked(Dockable dockable) {
		return getWrapper(dockable).getParent() != null;
	}

	public static boolean isUnpinned(String persistentID) {
		return isUnpinned(getDockable(persistentID));
	}

	public static boolean isUnpinned(Dockable dockable) {
		return getWrapper(dockable).isUnpinned();
	}

	public static boolean canDisposeFrame(JFrame frame) {
		return frame != instance.mainFrame && !DockingState.maximizeRestoreLayout.containsKey(frame);
	}

	public static boolean isMaximized(Dockable dockable) {
		return getWrapper(dockable).isMaximized();
	}

	public static void maximize(Dockable dockable) {
		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		// can only maximize one panel per root
		if (!DockingState.maximizeRestoreLayout.containsKey(frame) && root != null) {
			getWrapper(dockable).setMaximized(true);
			DockingListeners.fireMaximizeEvent(dockable, true);

			DockingLayout layout = DockingState.getCurrentLayout(frame);
			layout.setMaximizedDockable(dockable.persistentID());

			DockingState.maximizeRestoreLayout.put(frame, layout);

			DockingComponentUtils.undockComponents(root);

			dock(dockable, frame);
		}
	}

	public static void minimize(Dockable dockable) {
		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);

		// can only minimize if already maximized
		if (DockingState.maximizeRestoreLayout.containsKey(frame)) {
			getWrapper(dockable).setMaximized(false);
			DockingListeners.fireMaximizeEvent(dockable, false);

			DockingState.setLayout(frame, DockingState.maximizeRestoreLayout.get(frame));

			DockingState.maximizeRestoreLayout.remove(frame);

			DockingInternal.fireDockedEventForFrame(frame);
		}
	}

	public static void pinDockable(Dockable dockable) {
		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		if (getWrapper(dockable).isUnpinned()) {
			root.setDockablePinned(dockable);

			getWrapper(dockable).setUnpinned(false);
			DockingListeners.fireDockedEvent(dockable);
		}
	}

	public static void unpinDockable(Dockable dockable) {
		JFrame frame = DockingComponentUtils.findFrameForDockable(dockable);
		RootDockingPanel root = DockingComponentUtils.rootForFrame(frame);

		Component component = (Component) dockable;

		Point posInFrame = component.getLocation();
		SwingUtilities.convertPointToScreen(posInFrame, component.getParent());
		SwingUtilities.convertPointFromScreen(posInFrame, root);

		posInFrame.x += component.getWidth() / 2;
		posInFrame.y += component.getHeight() / 2;

		undock(dockable);

		// reset the frame, undocking the dockable sets it to null
		getWrapper(dockable).setFrame(frame);
		getWrapper(dockable).setUnpinned(true);

		boolean allowedSouth = dockable.style() == DockableStyle.BOTH || dockable.style() == DockableStyle.HORIZONTAL;

		int westDist = posInFrame.x;
		int eastDist = frame.getWidth() - posInFrame.x;
		int southDist = frame.getHeight() - posInFrame.y;

		boolean east = eastDist <= westDist;
		boolean south = southDist < westDist && southDist < eastDist;

		if (south && allowedSouth) {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.SOUTH);
		}
		else if (east) {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.EAST);
		}
		else {
			root.setDockableUnpinned(dockable, DockableToolbar.Location.WEST);
		}
		DockingListeners.fireUnpinnedEvent(dockable);
	}

	// display a dockable by persistentID
	public static void display(String persistentID) {
		display(getDockable(persistentID));
	}

	// if the dockable is already docked, then bringToFront is called.
	// if it is not docked, then dock is called, docking it with dockables of the same type
	public static void display(Dockable dockable) {
		if (isDocked(dockable)) {
			bringToFront(dockable);
		}
		else {
			// go through all the dockables and find the first one that is the same type
			Optional<Dockable> firstOfType = DockingComponentUtils.findFirstDockableOfType(dockable.type());

			if (firstOfType.isPresent()) {
				dock(dockable, firstOfType.get(), DockingRegion.CENTER);
			}
			else {
				// if we didn't find any dockables of the same type, we'll dock to north
				dock(dockable, instance.mainFrame, DockingRegion.NORTH);
			}
		}
	}

	public static void updateTabText(String persistentID) {
		updateTabText(getDockable(persistentID));
	}

	public static void updateTabText(Dockable dockable) {
		if (!isDocked(dockable)) {
			// if the dockable isn't docked then we don't have to do anything to update its tab text
			return;
		}

		FullAppLayout layout = DockingState.getFullLayout();

		Docking.undock(dockable);

		DockingState.restoreFullLayout(layout);
	}
}
