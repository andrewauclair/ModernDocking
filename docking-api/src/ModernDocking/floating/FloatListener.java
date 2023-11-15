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
package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.*;
import ModernDocking.layouts.WindowLayout;
import ModernDocking.ui.DockingHeaderUI;

import javax.swing.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener responsible for tracking dockables both when they are first dragged and while being dragged
 */
public class FloatListener extends DragSourceAdapter implements DragSourceListener, DragSourceMotionListener {
	/**
	 * Flag indicating if there is a dockable currently floating
	 */
	private static boolean isFloating = false;

	public static boolean isFloating() { return isFloating; }

	private static boolean isOverTab = false;

	// current floating dockable
//	private final JPanel floatingDockable;
	private final JPanel source;
	private JPanel floatingPanel;

	private final DockingAPI docking;

	// our drag source to support dragging the dockables
	private final DragSource dragSource = new DragSource();
	// dummy transferable, we don't actually transfer anything
	private final Transferable transferable = new StringSelection("");
	private final Component dragSource1;

	private Point dragOffset = new Point(0, 0);
	private TempFloatingFrame floatingFrame;

	private static final Map<Window, DockingUtilsFrame> utilFrames = new HashMap<>();

	private DockingUtilsFrame activeUtilsFrame = null;

	private static Window windowToDispose = null;

	private Window currentTopWindow = null;
	private Window currentTargetWindow = null;
	private Window originalWindow;

	private WindowLayout windowLayout;

	private ModalityType modalityType = ModalityType.MODELESS;

	public FloatListener(DockingAPI docking, DisplayPanel panel) {
		this(docking, panel, (JComponent) panel.getWrapper().getHeaderUI());
	}

	public FloatListener(DockingAPI docking, DockedTabbedPanel tabs, JComponent dragSource) {
		this(docking, (JPanel) tabs, dragSource);
	}

	private FloatListener(DockingAPI docking, JPanel dockable, JComponent dragSource) {
		this.source = dockable;
		this.docking = docking;

		dragSource1 = dragSource;
		if (dragSource1 != null) {
			this.dragSource.addDragSourceMotionListener(FloatListener.this);

			this.dragSource.createDefaultDragGestureRecognizer(dragSource, DnDConstants.ACTION_MOVE, dge -> {
				try {
					if (source instanceof DockedTabbedPanel) {
						Point mousePos = new Point(dge.getDragOrigin());
						SwingUtilities.convertPointToScreen(mousePos, dragSource1);

						DockedTabbedPanel tabs = (DockedTabbedPanel) source;
						int targetTabIndex = tabs.getTargetTabIndex(mousePos);

						if (targetTabIndex != -1) {
							floatingPanel = tabs.getDockables().get(targetTabIndex).getDisplayPanel();
						}
						else if (tabs.isDraggingFromTabGutter(mousePos)) {
							floatingPanel = tabs;
						}
						else {
							DockingHeaderUI headerUI = tabs.getDockables().get(tabs.getSelectedTabIndex()).getHeaderUI();
							JPanel panel = (JPanel) headerUI;

							if (panel.contains(mousePos)) {
								floatingPanel = tabs.getDockables().get(tabs.getSelectedTabIndex()).getDisplayPanel();
							}
							else {
								return;
							}
						}
					}
					this.dragSource.startDrag(dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), transferable, FloatListener.this);
				}
				catch (InvalidDnDOperationException ignored) {
					// someone beat us to it
					return;
				}
				mouseDragStarted(dge.getDragOrigin());

				if (originalWindow instanceof JDialog) {
					modalityType = ((JDialog) originalWindow).getModalityType();

					((JDialog) originalWindow).setModalityType(ModalityType.MODELESS);

					// Set all of these as invokeLater to force the order they happen in
					SwingUtilities.invokeLater(() -> {
						// check that the floating frame still exists since we invoked later and time might have passed
						if (floatingFrame != null) {
							floatingFrame.toFront();
						}
					});
					SwingUtilities.invokeLater(() -> {
						// check that the utils frame still exists since we invoked later and time might have passed
						if (activeUtilsFrame != null) {
							activeUtilsFrame.toFront();
						}
					});
				}
			});
		}
	}

	public void removeListeners() {
		dragSource.removeDragSourceMotionListener(this);

//		floatingDockable.removedListeners();
	}

	public static void registerDockingWindow(DockingAPI docking, Window window, RootDockingPanelAPI root) {
		utilFrames.put(window, new DockingUtilsFrame(docking, window, root));
	}

	public static void deregisterDockingWindow(Window window) {
		utilFrames.remove(window);
	}

	private void updateFramePosition(Point mousePosOnScreen) {
		// update the frames position to our mouse position
		Point framePos = new Point(mousePosOnScreen.x - dragOffset.x, mousePosOnScreen.y - dragOffset.y);
		floatingFrame.setLocation(framePos);

		// find the frame at our current position
		Window frame = DockingComponentUtils.findRootAtScreenPos(docking, mousePosOnScreen);

		// findRootAtScreenPos has a tendency to find the last added frame at the position. meaning it ignores Z order. override it here because we know better.
		if (currentTopWindow != null && currentTopWindow.getBounds().contains(mousePosOnScreen)) {
			frame = currentTopWindow;
		}

		boolean isModal = modalityType == ModalityType.TOOLKIT_MODAL || modalityType == ModalityType.APPLICATION_MODAL;

		// change overlays and bring frames to front if we move over a new frame
		if (frame != currentTargetWindow && !isModal) {
			currentTargetWindow = frame;
			currentTopWindow = frame;

			changeFrameOverlays(frame);
		}

		Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, currentTopWindow);

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setFloating(floatingPanel);
			activeUtilsFrame.setTargetDockable(dockable);
			activeUtilsFrame.update(mousePosOnScreen);
		}

		CustomTabbedPane tabbedPane = (CustomTabbedPane) DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, currentTopWindow);

		if (activeUtilsFrame != null) {
			boolean overTab = dockable == null && tabbedPane != null && source instanceof DisplayPanel;

			if (overTab) {
				int targetTabIndex = tabbedPane.getTargetTabIndex(mousePosOnScreen);

				Rectangle boundsAt;
				boolean last = false;
				if (targetTabIndex != -1) {
					boundsAt = tabbedPane.getBoundsAt(targetTabIndex);

					Point p = new Point(boundsAt.x, boundsAt.y);
					SwingUtilities.convertPointToScreen(p, tabbedPane);
					SwingUtilities.convertPointFromScreen(p, activeUtilsFrame);
					boundsAt.x = p.x;
					boundsAt.y = p.y;

					boundsAt.width /=2;
				} else {
					boundsAt = tabbedPane.getBoundsAt(tabbedPane.getTabCount() - 1);

					Point p = new Point(boundsAt.x, boundsAt.y);
					SwingUtilities.convertPointToScreen(p, tabbedPane);
					SwingUtilities.convertPointFromScreen(p, activeUtilsFrame);
					boundsAt.x = p.x;
					boundsAt.y = p.y;
					boundsAt.x += boundsAt.width;
					last = true;
				}

				activeUtilsFrame.setOverTab(true, boundsAt, last);
				activeUtilsFrame.update(mousePosOnScreen);
				floatingFrame.setVisible(false);
			}
			else if (isOverTab) {
				activeUtilsFrame.setOverTab(false, null, false);
				floatingFrame.setVisible(true);

				reorderWindows();
			}

			isOverTab = overTab;
		}

		if (tabbedPane != null && tabbedPane.getSelectedComponent() instanceof DisplayPanel) {
			DisplayPanel panel = (DisplayPanel) tabbedPane.getSelectedComponent();

			if (activeUtilsFrame != null) {
				activeUtilsFrame.setFloating(floatingPanel);
				activeUtilsFrame.setTargetDockable(panel.getWrapper().getDockable());
				activeUtilsFrame.update(mousePosOnScreen);
			}
		}
	}

	private void changeFrameOverlays(Window newWindow) {
		if (activeUtilsFrame != null) {
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;
		}

		if (newWindow != null) {
			activeUtilsFrame = utilFrames.get(newWindow);

			if (currentTopWindow != null && floatingFrame != null && activeUtilsFrame != null) {
				Point mousePos = MouseInfo.getPointerInfo().getLocation();
				activeUtilsFrame.setFloating(floatingPanel);
				activeUtilsFrame.update(mousePos);
				activeUtilsFrame.setActive(true);

				reorderWindows();
			}
		}
	}

	private void reorderWindows() {
		// Set all of these as invokeLater to force the order they happen in
		SwingUtilities.invokeLater(() -> {
			// check that the current top frame still exists since we invoked later and time might have passed
			if (currentTopWindow != null) {
				currentTopWindow.toFront();
			}
		});
		SwingUtilities.invokeLater(() -> {
			// check that the floating frame still exists since we invoked later and time might have passed
			if (floatingFrame != null) {
				floatingFrame.toFront();
			}
		});
		SwingUtilities.invokeLater(() -> {
			// check that the utils frame still exists since we invoked later and time might have passed
			if (activeUtilsFrame != null) {
				activeUtilsFrame.toFront();
			}
		});
	}

	public void mouseDragStarted(Point point) {
		isFloating = true;

		dragOffset = point;

		// force the drag offset to be inset from the edge slightly
		dragOffset.y = Math.max(5, dragOffset.y);
		dragOffset.x = Math.max(5, dragOffset.x);

		currentTargetWindow = null;

		// make sure we are still using the mouse press point, not the current mouse position which might not be over the frame anymore
		Point mousePos = new Point(point);
		SwingUtilities.convertPointToScreen(mousePos, dragSource1);

		if (source instanceof DisplayPanel) {
			originalWindow = ((DisplayPanel) source).getWrapper().getWindow();
			floatingPanel = source;
		}
		else {
			originalWindow = ((DockedTabbedPanel) source).getDockables().get(0).getWindow();
		}
		windowLayout = docking.getDockingState().getWindowLayout(originalWindow);

		RootDockingPanelAPI currentRoot = DockingComponentUtils.rootForWindow(docking, originalWindow);

		if (floatingPanel instanceof DisplayPanel) {
			floatingFrame = new TempFloatingFrame(((DisplayPanel) floatingPanel).getWrapper(), source, floatingPanel.getSize());

			docking.undock(((DisplayPanel) floatingPanel).getWrapper().getDockable());
		}
		else {
			DockedTabbedPanel tabs = (DockedTabbedPanel) floatingPanel;

			List<DockableWrapper> wrappers = new ArrayList<>(tabs.getDockables());

			floatingFrame = new TempFloatingFrame(wrappers, tabs.getSelectedTabIndex(), source, floatingPanel.getSize());

			for (DockableWrapper wrapper : wrappers) {
				docking.undock(wrapper.getDockable());

				// undock does not remove the last panel
//				tabs.removePanel(wrapper);
			}

//			for (DockableWrapper wrapper : wrappers) {
//				tabs.addPanel(wrapper);
//				wrapper.setParent(null); // we don't want this to count as docked
//			}
		}

		DockingComponentUtils.removeIllegalFloats(docking, originalWindow);

		if (originalWindow != null && currentRoot != null && currentRoot.getPanel() == null && docking.canDisposeWindow(originalWindow)) {
			windowToDispose = originalWindow;
			windowToDispose.setVisible(false);
		}

		if (originalWindow != windowToDispose) {
			currentTopWindow = originalWindow;
			currentTargetWindow = originalWindow;
			activeUtilsFrame = utilFrames.get(originalWindow);
		}

		if (activeUtilsFrame != null) {
			activeUtilsFrame.setFloating(floatingPanel);
			activeUtilsFrame.update(mousePos);
			activeUtilsFrame.setActive(true);
			activeUtilsFrame.toFront();
		}

		docking.getAppState().setPaused(true);
	}

	private void dropFloatingPanel() {
		docking.getAppState().setPaused(false);

		Point mousePos = MouseInfo.getPointerInfo().getLocation();

		Point point = MouseInfo.getPointerInfo().getLocation();

		RootDockingPanelAPI root = currentTopWindow == null ? null : DockingComponentUtils.rootForWindow(docking, currentTopWindow);

		DockingPanel dockingPanel = DockingComponentUtils.findDockingPanelAtScreenPos(point, currentTopWindow);
		Dockable dockableAtPos = DockingComponentUtils.findDockableAtScreenPos(point, currentTopWindow);

		DockingRegion region = activeUtilsFrame != null ? activeUtilsFrame.getRegion(mousePos) : DockingRegion.CENTER;

		if (floatingPanel instanceof DisplayPanel) {
			DockableWrapper floatingDockable = ((DisplayPanel) this.floatingPanel).getWrapper();

			if (activeUtilsFrame != null && activeUtilsFrame.isDockingToPin()) {
				docking.unpinDockable(floatingDockable.getDockable(), activeUtilsFrame.getToolbarLocation(), currentTopWindow, root);
			}
			else if (root != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToRoot()) {
				docking.dock(floatingDockable.getDockable(), currentTopWindow, region, 0.25);
			}
			else if (floatingDockable.getDockable().isLimitedToRoot() && floatingDockable.getRoot() != root) {
				docking.getDockingState().restoreWindowLayout(originalWindow, windowLayout);
			}
			else if (dockableAtPos != null && currentTopWindow != null && dockingPanel != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToDockable()) {
				docking.dock(floatingDockable.getDockable(), dockableAtPos, region);
			}
			else if (root != null && region != DockingRegion.CENTER && activeUtilsFrame == null) {
				docking.dock(floatingDockable.getDockable(), currentTopWindow, region);
			}
			else if (!floatingDockable.getDockable().isFloatingAllowed()) {
				docking.getDockingState().restoreWindowLayout(originalWindow, windowLayout);
			}
			else if (dockableAtPos == null && root != null) {
				// we're inserting at a specific position in a tabbed pane
				CustomTabbedPane tabbedPane = (CustomTabbedPane) DockingComponentUtils.findTabbedPaneAtPos(point, currentTopWindow);

				if (tabbedPane != null) {
					DockedTabbedPanel parent = (DockedTabbedPanel) tabbedPane.getParent();

					int targetTabIndex = tabbedPane.getTargetTabIndex(point);

					Rectangle boundsAt;
					boolean last = false;
					if (targetTabIndex != -1) {
						boundsAt = tabbedPane.getBoundsAt(targetTabIndex);

						Point p = new Point(boundsAt.x, boundsAt.y);
						SwingUtilities.convertPointToScreen(p, tabbedPane);
						SwingUtilities.convertPointFromScreen(p, activeUtilsFrame);
						boundsAt.x = p.x;
						boundsAt.y = p.y;

						boundsAt.width /= 2;
					} else {
						boundsAt = tabbedPane.getBoundsAt(tabbedPane.getTabCount() - 1);

						Point p = new Point(boundsAt.x, boundsAt.y);
						SwingUtilities.convertPointToScreen(p, tabbedPane);
						SwingUtilities.convertPointFromScreen(p, activeUtilsFrame);
						boundsAt.x = p.x;
						boundsAt.y = p.y;
						boundsAt.x += boundsAt.width;
						last = true;
					}

					parent.dockAtIndex(floatingDockable.getDockable(), targetTabIndex);
				}
			}
			else {
				new FloatingFrame(docking, floatingDockable.getDockable(), floatingFrame);
			}
		}
		else {
//			DockedTabbedPanel tabs = (DockedTabbedPanel) floatingPanel;
			List<DockableWrapper> dockables = new ArrayList<>(floatingFrame.getDockables());

			boolean first = true;
			Dockable firstDockable = null;

			for (DockableWrapper dockable : dockables) {
				if (first) {
					if (dockableAtPos != null && currentTopWindow != null && dockingPanel != null && activeUtilsFrame != null && activeUtilsFrame.isDockingToDockable()) {
						docking.dock(dockable.getDockable(), dockableAtPos, region);
					}
					else {
						new FloatingFrame(docking, dockable.getDockable(), floatingFrame);
					}
					firstDockable = dockable.getDockable();
				}
				else {
					docking.dock(dockable.getDockable(), firstDockable, DockingRegion.CENTER);
				}
				first = false;
			}

			docking.bringToFront(dockables.get(floatingFrame.getSelectedIndex()).getDockable());
		}

		// auto persist the new layout to the file
		docking.getAppState().persist();

		if (originalWindow instanceof JDialog) {
			((JDialog) originalWindow).setModalityType(modalityType);
		}

		originalWindow = null;

		// if we're disposing the frame we started dragging from, dispose of it now
		if (windowToDispose != null) {
			docking.deregisterDockingPanel(windowToDispose);
			windowToDispose.dispose();
			windowToDispose = null;
		}

		// dispose of the temp floating frame now that we're done with it
		floatingFrame.dispose();
		floatingFrame = null;

		// hide the overlay frame if one is active
		if (activeUtilsFrame != null) {
			activeUtilsFrame.setTargetDockable(null);
			activeUtilsFrame.setFloating(null);
			activeUtilsFrame.setActive(false);
			activeUtilsFrame = null;
		}
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (!isFloating) {
			return;
		}
		dropFloatingPanel();

		isFloating = false;
	}

	@Override
	public void dragMouseMoved(DragSourceDragEvent dsde) {
		if (!isFloating) {
			return;
		}
		updateFramePosition(dsde.getLocation());
	}
}
