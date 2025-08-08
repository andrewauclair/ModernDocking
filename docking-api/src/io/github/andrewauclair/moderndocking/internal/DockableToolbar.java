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
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import io.github.andrewauclair.moderndocking.internal.util.CombinedIcon;
import io.github.andrewauclair.moderndocking.internal.util.RotatedIcon;
import io.github.andrewauclair.moderndocking.internal.util.TextIcon;
import io.github.andrewauclair.moderndocking.internal.util.UnselectableButtonGroup;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * This class is a special panel used to display toolbars on the West, East or South side of a frame to display dockables
 * that are auto hide enabled
 */
public class DockableToolbar extends JPanel implements ComponentListener {
	/**
	 * The docking instance this toolbar belongs to
	 */
	private final DockingAPI docking;
	/**
	 * The window this toolbar is in
	 */
	private final Window window;
	/**
	 * The root docking panel of the window
	 */
	private final RootDockingPanelAPI root;
	/**
	 * The location of this toolbar. Either WEST, SOUTH or EAST
	 */
	private final ToolbarLocation location;

	private static class Entry {
		private final Dockable dockable;
		private final JToggleButton button;
		private final DockedAutoHidePanel panel;

		private Entry(Dockable dockable, JToggleButton button, DockedAutoHidePanel panel) {
			this.dockable = dockable;
			this.button = button;
			this.panel = panel;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Entry panel = (Entry) o;
			return Objects.equals(dockable, panel.dockable);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dockable);
		}
	}

	/**
	 * The dockables that are assigned to this auto-hide toolbar
	 */
	private final List<Entry> dockables = new ArrayList<>();

	/**
	 * The group of all the toolbar buttons for the auto-hide dockables
	 */
	private final UnselectableButtonGroup buttonGroup = new UnselectableButtonGroup();

	/**
	 * Create a new dockable toolbar for the window, its root and a location (west, south or east)
	 *
	 * @param docking The docking instance
	 * @param window The window this toolbar is attached to
	 * @param root The root of the attached window
	 * @param location The location of this toolbar within the window
	 */
	public DockableToolbar(DockingAPI docking, Window window, RootDockingPanelAPI root, ToolbarLocation location) {
		super(new GridBagLayout());
		this.docking = docking;

		// the window must be a JFrame or a JDialog to support pinning (we need a JLayeredPane)
		assert window instanceof JFrame || window instanceof JDialog;

		this.window = window;
		this.root = root;
		this.location = location;

		addComponentListener(this);
	}

	/**
	 * Get the location within the window that this toolbar is docked
	 *
	 * @return Location, west, south or east
	 */
	public ToolbarLocation getDockedLocation() {
		return location;
	}

	/**
	 * Check if this toolbar is vertical (east or west)
	 *
	 * @return True if vertical
	 */
	public boolean isVertical() {
		return location == ToolbarLocation.EAST || location == ToolbarLocation.WEST;
	}

	private void createContents() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;

		for (Entry dockable : dockables) {
			add(dockable.button, gbc);

			if (isVertical()) {
				gbc.gridy++;
			}
			else {
				gbc.gridx++;
			}
		}

		if (isVertical()) {
			gbc.weighty = 1.0;
		}
		else {
			gbc.weightx = 1.0;
		}
		add(new JLabel(""), gbc);
	}

	private void updateButtons() {
		for (Entry entry : dockables) {
			boolean isSelected = buttonGroup.getSelection() == entry.button.getModel();

			if (entry.panel.isVisible() && !isSelected) {
				DockingListeners.fireHiddenEvent(entry.dockable);
			}
			else if (!entry.panel.isVisible() && isSelected) {
				DockingListeners.fireShownEvent(entry.dockable);
			}
			else if (isSelected) {
				DockingListeners.fireShownEvent(entry.dockable);
			}

			// set only a single panel visible
			entry.panel.setVisible(isSelected);

			if (isSelected) {
				Color color = DockingSettings.getHighlighterSelectedBorder();
				entry.panel.setBorder(BorderFactory.createLineBorder(color, 2));
			}
		}
	}

	/**
	 * Add a new dockable to this toolbar
	 *
	 * @param dockable Dockable to add
	 */
	public void addDockable(Dockable dockable) {
		if (!hasDockable(dockable)) {
			JToggleButton button = new JToggleButton();

			button.setIcon(dockable.getIcon());

			if (isVertical()) {
				TextIcon textIcon = new TextIcon(button, dockable.getTabText(), TextIcon.Layout.HORIZONTAL);
				RotatedIcon rotatedIcon = new RotatedIcon(textIcon, location == ToolbarLocation.WEST ? RotatedIcon.Rotate.UP : RotatedIcon.Rotate.DOWN);

				if (dockable.getIcon() != null) {
					button.setIcon(new CombinedIcon(dockable.getIcon(), rotatedIcon));
				}
				else {
					button.setIcon(rotatedIcon);
				}

				Insets insets = UIManager.getInsets("Button.margin");
				// purposefully putting them in this order to set the margins of a vertical button
				//noinspection SuspiciousNameCombination
				Insets margin = new Insets(insets.left, insets.top, insets.left, insets.top);
				button.setMargin(margin);
			}
			else {
				button.setText(dockable.getTabText());
			}

			DockedAutoHidePanel panel = new DockedAutoHidePanel(docking, dockable, root, this);

			DockingInternal.get(docking).getWrapper(dockable).setWindow(window);

			// update all the buttons and panels
			button.addActionListener(e -> updateButtons());

			buttonGroup.add(button);

			dockables.add(new Entry(dockable, button, panel));

			JLayeredPane layeredPane;

			if (window instanceof JFrame) {
				layeredPane = ((JFrame) window).getLayeredPane();
			}
			else {
				layeredPane = ((JDialog) window).getLayeredPane();
			}

			layeredPane.add(panel, root.getAutoHideLayer());

			createContents();
		}
	}

	/**
	 * Remove a dockable from this toolbar
	 *
	 * @param dockable Dockable to remove
	 */
	public void removeDockable(Dockable dockable) {
		for (Entry entry : dockables) {
			if (entry.dockable == dockable) {
				JLayeredPane layeredPane;

				if (window instanceof JFrame) {
					layeredPane = ((JFrame) window).getLayeredPane();
				}
				else {
					layeredPane = ((JDialog) window).getLayeredPane();
				}

				layeredPane.remove(entry.panel);
				break;
			}
		}
		if (dockables.removeIf(panel -> panel.dockable.equals(dockable))) {
			createContents();
		}
	}

	/**
	 * Check if this toolbar contains a certain dockable
	 *
	 * @param dockable Dockable to search for
	 * @return Is dockable contained in this toolbar?
	 */
	public boolean hasDockable(Dockable dockable) {
		return dockables.stream()
				.anyMatch(panel -> panel.dockable.equals(dockable));
	}

	/**
	 * Check if this toolbar should be displayed.
	 *
	 * @return True if there are 1 or more dockables in the toolbar
	 */
	public boolean shouldDisplay() {
		return dockables.size() > 0;
	}

	/**
	 * Hide all the dockables in the toolbar
	 */
	public void hideAll() {
		buttonGroup.setSelected(buttonGroup.getSelection(), false);

		updateButtons();
	}

	/**
	 * Get a list of the persistent IDs of the dockables in this toolbar
	 *
	 * @return List of persistent IDs
	 */
	public List<String> getPersistentIDs() {
		return dockables.stream()
				.map(entry -> entry.dockable.getPersistentID())
				.collect(Collectors.toList());
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
		dockables.forEach(dockable -> dockable.panel.componentResized(e));
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
