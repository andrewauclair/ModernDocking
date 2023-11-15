/*
Copyright (c) 2022-2023 Andrew Auclair

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
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.floating.FloatListener;
import ModernDocking.settings.Settings;
import ModernDocking.ui.DockingSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

/**
 * DockingPanel that has a JTabbedPane inside its center
 */
public class DockedTabbedPanel extends DockingPanel implements ChangeListener {
	/**
	 * Wrapper objects of the contained dockables in this tabbed panel
	 */
	private final List<DockableWrapper> panels = new ArrayList<>();

	private FloatListener floatListener;

	private final CustomTabbedPane tabs = new CustomTabbedPane();
	private final DockingAPI docking;

	/**
	 * The parent of this DockedTabbedPanel
	 */
	private DockingPanel parent;

	/**
	 * The selected tab index. default to -1 and set properly when state changes
	 */
	private int selectedTab = -1;

	private static Icon settingsIcon = new ImageIcon(Objects.requireNonNull(DockedTabbedPanel.class.getResource("/icons/settings.png")));

	/**
	 * Create a new instance of DockedTabbedPanel
	 *
	 * @param dockable The first dockable in the tabbed pane
	 */
	public DockedTabbedPanel(DockingAPI docking, DockableWrapper dockable) {
		this.docking = docking;
		setLayout(new BorderLayout());

		// set the initial border. Docking handles the border after this using a global AWT listener
		setNotSelectedBorder();

		// default to tabs on bottom. if we need to change it we will when the first dockable is added
		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		tabs.setTabLayoutPolicy(Settings.getTabLayoutPolicy());

		if (Settings.alwaysDisplayTabsMode()) {
			configureTrailingComponent();
		}

		add(tabs, BorderLayout.CENTER);

		addPanel(dockable);
	}

	public static void setSettingsIcon(Icon icon) {
		settingsIcon = icon;
	}

	private void configureTrailingComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		JButton menu = new JButton(settingsIcon);

		setupButton(menu);

		menu.addActionListener(e -> {
			DockableWrapper dockable = panels.get(tabs.getSelectedIndex());

			dockable.getHeaderUI().displaySettingsMenu(menu);

		});

		panel.add(menu, gbc);

		tabs.putClientProperty("JTabbedPane.trailingComponent", panel);
		tabs.putClientProperty("JTabbedPane.tabCloseCallback", (IntConsumer) tabIndex -> docking.undock(panels.get(tabIndex).getDockable()));
	}

	private void setupButton(JButton button) {
		Color color = DockingSettings.getHeaderBackground();
		button.setBackground(color);
		button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		button.setFocusable(false);
		button.setOpaque(false);
		button.setContentAreaFilled(false);

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setContentAreaFilled(true);
				button.setOpaque(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setContentAreaFilled(false);
				button.setOpaque(false);
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();

		tabs.addChangeListener(this);

		if (floatListener != null) {
			floatListener.removeListeners();
		}

		floatListener = new FloatListener(docking, this, tabs);
	}

	@Override
	public void removeNotify() {
		tabs.removeChangeListener(this);

		super.removeNotify();
	}

	/**
	 * Add a new panel to this tabbed panel.
	 *
	 * @param dockable The dockable to add
	 */
	public void addPanel(DockableWrapper dockable) {
		// we only support tabs on top if we have FlatLaf because we can add a trailing component for our menu
		boolean usingFlatLaf = tabs.getUI().getClass().getPackageName().startsWith("com.formdev.flatlaf");

		if (Settings.alwaysDisplayTabsMode() && usingFlatLaf) {
			tabs.setTabPlacement(JTabbedPane.TOP);
		}
		else {
			tabs.setTabPlacement(JTabbedPane.BOTTOM);
		}

		panels.add(dockable);
		tabs.add(dockable.getDockable().getTabText(), dockable.getDisplayPanel());

		tabs.setToolTipTextAt(tabs.getTabCount() - 1, dockable.getDockable().getTabTooltip());
		tabs.setIconAt(tabs.getTabCount() - 1, dockable.getDockable().getIcon());
		tabs.setSelectedIndex(tabs.getTabCount() - 1);
		selectedTab = tabs.getSelectedIndex();

		if (Settings.alwaysDisplayTabsMode()) {
			JLabel tabComponent = new JLabel(dockable.getDockable().getTabText());
			tabComponent.setIcon(dockable.getDockable().getIcon());
			tabComponent.setToolTipText(dockable.getDockable().getTabTooltip());

			tabComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					tabs.setSelectedIndex(tabs.indexOfTabComponent(tabComponent));
				}
			});
			tabs.setTabComponentAt(tabs.getTabCount() - 1, tabComponent);

			if (dockable.getDockable().isClosable()) {
				dockable.getDisplayPanel().putClientProperty("JTabbedPane.tabClosable", true);
			}
		}

		dockable.setParent(this);
	}

	/**
	 * Remove a panel from this DockedTabbedPanel. This is done when the dockable is closed or docked elsewhere
	 *
	 * @param dockable The dockable to remove
	 */
	public void removePanel(DockableWrapper dockable) {
		if (panels.contains(dockable)) {
			panels.indexOf(dockable);

			tabs.remove(dockable.getDisplayPanel());
			panels.remove(dockable);

			dockable.setParent(null);
		}
	}

	/**
	 * Get a list of the dockables in this tabbed panel
	 *
	 * @return List of persistent IDs of dockable tabs
	 */
	public List<DockableWrapper> getDockables() {
		return Collections.unmodifiableList(panels);
	}

	@Override
	public void setParent(DockingPanel parent) {
		this.parent = parent;
	}

	@Override
	public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
		wrapper.setWindow(panels.get(0).getWindow());

		if (region == DockingRegion.CENTER) {
			addPanel(wrapper);
		}
		else {
			DockedSplitPanel split = new DockedSplitPanel(docking, panels.get(0).getWindow());
			parent.replaceChild(this, split);

			DockingPanel newPanel;

			if (Settings.alwaysDisplayTabsMode()) {
				newPanel = new DockedTabbedPanel(docking, wrapper);
			}
			else {
				newPanel = new DockedSimplePanel(docking, wrapper);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
				split.setLeft(this);
				split.setRight(newPanel);
				dividerProportion = 1.0 - dividerProportion;
			}
			else {
				split.setLeft(newPanel);
				split.setRight(this);
			}

			if (region == DockingRegion.EAST || region == DockingRegion.WEST) {
				split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
			else {
				split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}

			split.setDividerLocation(dividerProportion);
		}

		revalidate();
		repaint();
	}

	public void dockAtIndex(Dockable dockable, int index) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);
		wrapper.setWindow(panels.get(0).getWindow());

		addPanel(DockingInternal.get(docking).getWrapper(dockable));

		if (index != -1) {
			int lastIndex = tabs.getTabCount() - 1;

			for (int i = index; i < lastIndex; i++) {
				DockableWrapper panel = panels.get(index);

				removePanel(panel);
				addPanel(panel);
			}

			tabs.setSelectedIndex(index);
		}
	}

	@Override
	public void undock(Dockable dockable) {
		removePanel(DockingInternal.get(docking).getWrapper(dockable));

		if (!Settings.alwaysDisplayTabsMode() && tabs.getTabCount() == 1 && parent != null) {
			parent.replaceChild(this, new DockedSimplePanel(docking, panels.get(0)));
		}

		if (tabs.getTabCount() == 0) {
			parent.removeChild(this);
		}
	}

	@Override
	public void replaceChild(DockingPanel child, DockingPanel newChild) {
		// no-op, docked tab can't have panel children, wrappers only
	}

	@Override
	public void removeChild(DockingPanel child) {
		// no-op, docked tab can't have panel children, wrappers only
	}

	private void setNotSelectedBorder() {
		Color color = UIManager.getColor("Component.borderColor");

		setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 1),
						BorderFactory.createLineBorder(color, 1)
				)
		);
	}

	/**
	 * Set the specified dockable as the selected tab in the tab pane
	 *
	 * @param dockable Dockable to bring to front
	 */
	public void bringToFront(Dockable dockable) {
		for (int i = 0; i < panels.size(); i++) {
			DockableWrapper panel = panels.get(i);

			if (panel.getDockable() == dockable) {
				if (tabs.getSelectedIndex() != i) {
					if (tabs.getSelectedIndex() != -1) {
						DockingListeners.fireHiddenEvent(panels.get(tabs.getSelectedIndex()).getDockable());
					}
					DockingListeners.fireShownEvent(panels.get(i).getDockable());
				}
				tabs.setSelectedIndex(i);
				selectedTab = tabs.getSelectedIndex();
			}
		}
	}

	/**
	 * Get the persistent ID of the selected tab
	 *
	 * @return The persistent ID of the selected tab
	 */
	public String getSelectedTabID() {
		return panels.get(tabs.getSelectedIndex()).getDockable().getPersistentID();
	}

	public int getSelectedTabIndex() {
		return tabs.getSelectedIndex();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		docking.getAppState().persist();

		if (tabs.getSelectedIndex() == -1) {
			return;
		}

		if (selectedTab != -1 && !FloatListener.isFloating()) {
			DockingListeners.fireHiddenEvent(panels.get(selectedTab).getDockable());
		}
		selectedTab = tabs.getSelectedIndex();

		if (selectedTab != -1) {
			DockingListeners.fireShownEvent(panels.get(selectedTab).getDockable());
		}
	}

	public boolean isUsingTopTabs() {
		return tabs.getTabPlacement() == JTabbedPane.TOP;
	}

	public boolean isUsingBottomTabs() {
		return tabs.getTabPlacement() == JTabbedPane.BOTTOM;
	}

	public Component getTabForDockable(DockableWrapper wrapper) {
		for (int i = 0; i < panels.size(); i++) {
			DockableWrapper panel = panels.get(i);

			if (panel.getDockable() == wrapper.getDockable()) {
				return tabs.getTabComponentAt(i);
			}
		}
		return null;
	}

	public void updateTabInfo(Dockable dockable) {
		for (int i = 0; i < panels.size(); i++) {
			DockableWrapper panel = panels.get(i);

			if (panel.getDockable() == dockable) {
				tabs.setTitleAt(i, dockable.getTabText());
				tabs.setToolTipTextAt(i, dockable.getTabTooltip());

				Component tabComponent = tabs.getTabComponentAt(i);

				if (tabComponent instanceof JLabel) {
					((JLabel) tabComponent).setText(dockable.getTabText());
					((JLabel) tabComponent).setToolTipText(dockable.getTabTooltip());
				}
			}
		}
	}

	public int getTargetTabIndex(Point point) {
		return tabs.getTargetTabIndex(point);
	}

	public boolean isDraggingFromTabGutter(Point point) {
		Rectangle boundsAt = tabs.getBoundsAt(0);

		return boundsAt.y <= point.y && (boundsAt.y + boundsAt.height) >= point.y;

	}
}
