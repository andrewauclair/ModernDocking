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
package io.github.andrewauclair.moderndocking.ui;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * This can be replaced by the user or with the docking-ui FlatLaf header UI
 */
public class DefaultHeaderUI extends JPanel implements DockingHeaderUI, AncestorListener {
	/**
	 * Header controller which determines what menu options are enabled
	 */
	private final HeaderController headerController;
	/**
	 * Header model which provides values to the UI
	 */
	private final HeaderModel headerModel;

	/**
	 * Label that displays the name of the dockable
	 */
	protected final JLabel titleLabel = new JLabel();
	/**
	 * Settings button. Displays a popup menu when pressed
	 */
	protected final JButton settings = new JButton();
	/**
	 * Close button that shows an X and when pressed undocks the dockable
	 */
	protected final JButton close = new JButton();
	/**
	 * Label that is displayed when the dockable is maximized
	 */
	protected final JLabel maximizedIndicator = new JLabel("Maximized");

	/**
	 * Popup menu that is displayed when the settings button is pressed
	 */
	private final JPopupMenu settingsMenu = new JPopupMenu();

	/**
	 * Menu option to auto hide the dockable. Available when the dockable is auto hide enabled
	 */
	private final JCheckBoxMenuItem autoHide = new JCheckBoxMenuItem("Auto Hide");

	/**
	 * Option to move the dockable to its own window
	 */
	private final JMenuItem window = new JMenuItem("Window");
	/**
	 * Option to maximize the dockable
	 */
	private final JCheckBoxMenuItem maximizeOption = new JCheckBoxMenuItem("Maximize");

	/**
	 * Used to ensure that the header UI is only initialized once when added to its parent
	 */
	protected boolean initialized = false;

	private Color backgroundOverride = null;
	private Color foregroundOverride = null;

	/**
	 * Create a new DefaultHeaderUI
	 *
	 * @param headerController Header controller to use for this UI
	 * @param headerModel Header model to use for this UI
	 */
	public DefaultHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		this.headerController = headerController;
		this.headerModel = headerModel;

		setOpaque(true);

		// delay the actual init of the UI in case the dockable object is partially constructed
		JComponent component = (JComponent) headerModel.dockable;
		component.addAncestorListener(this);
	}

	@Override
	public void displaySettingsMenu(JButton settings) {
		settingsMenu.show(settings, settings.getWidth(), settings.getHeight());
	}

	protected void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		try {
			settings.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/api_icons/settings.png"))));
			close.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/api_icons/close.png"))));
		}
		catch (Exception ignored) {
		}

		settings.addActionListener(e -> displaySettingsMenu(settings));
		settings.addActionListener(e -> this.settingsMenu.show(settings, settings.getWidth(), settings.getHeight()));
		close.addActionListener(e -> headerController.close());

		setupButton(settings);
		setupButton(close);

		configureColors();

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(1, 6, 1, 2);

		JLabel iconLabel = new JLabel(headerModel.icon());
		add(iconLabel, gbc);
		gbc.gridx++;

		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		titleLabel.setText(headerModel.titleText());
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setMinimumSize(new Dimension(0, 28));
		titleLabel.setPreferredSize(new Dimension(0, 28));

		add(titleLabel, gbc);

		gbc.gridx++;
		gbc.weightx = 0.2;

		maximizedIndicator.setVisible(false);
		maximizedIndicator.setFont(maximizedIndicator.getFont().deriveFont(Font.BOLD));

		add(maximizedIndicator, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;

		if (headerModel.hasMoreOptions() || headerModel.isMaximizeAllowed() || headerModel.isAutoHideAllowed() || headerModel.isFloatingAllowed()) {
			addOptions();

			add(settings, gbc);
			gbc.gridx++;
		}
		if (headerModel.isCloseAllowed()) {
			add(close, gbc);
			gbc.gridx++;
		}
	}

	protected void configureColors() {
		Color color = DockingSettings.getHeaderBackground();
		setBackground(color);

		setForeground(DockingSettings.getHeaderForeground());

		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DockingSettings.getHighlighterNotSelectedBorder()));

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				Color bg = DockingSettings.getHeaderBackground();
				SwingUtilities.invokeLater(() -> {
					setBackground(bg);

					setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DockingSettings.getHighlighterNotSelectedBorder()));
				});

				SwingUtilities.updateComponentTreeUI(settingsMenu);
			}
			else if (e.getPropertyName().equals("ModernDocking.titlebar.background")) {
				Color bg = DockingSettings.getHeaderBackground();
				SwingUtilities.invokeLater(() -> setBackground(bg));
			}
		});
	}

	private void addOptions() {
		headerModel.addMoreOptions(settingsMenu);

		if (settingsMenu.getComponentCount() > 0) {
			settingsMenu.addSeparator();
		}

		window.setEnabled(headerModel.isFloatingAllowed());

		autoHide.addActionListener(e -> headerController.toggleAutoHide());

		window.addActionListener(e -> headerController.newWindow());

		JMenu viewMode = new JMenu("View Mode");
		viewMode.add(autoHide);
		viewMode.add(window);

		settingsMenu.add(viewMode);
		settingsMenu.addSeparator();

		settingsMenu.add(maximizeOption);

		maximizeOption.addActionListener(e -> {
			boolean maxed = headerModel.isMaximized();

			maximizeOption.setSelected(!maxed);
			maximizedIndicator.setVisible(!maxed);

			if (maxed) {
				headerController.minimize();
			}
			else {
				headerController.maximize();
			}
		});
	}

	private void setupButton(JButton button) {
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
	public void setBackgroundOverride(Color color) {
		backgroundOverride = color;
	}

	@Override
	public void setForegroundOverride(Color color) {
		foregroundOverride = color;
	}

	@Override
	public void setBackground(Color bg) {
		if (backgroundOverride != null) {
			bg = backgroundOverride;
		}

		super.setBackground(bg);

		if (close != null) close.setBackground(bg);
		if (settings != null) settings.setBackground(bg);
	}

	@Override
	public void setForeground(Color fg) {
		if (foregroundOverride != null) {
			fg = foregroundOverride;
		}

		super.setForeground(fg);

		if (titleLabel != null) titleLabel.setForeground(fg);

		if (close != null) close.setForeground(fg);
		if (settings != null) settings.setForeground(fg);
	}

	@Override
	public void update() {
		titleLabel.setText(headerModel.titleText());

		maximizedIndicator.setVisible(headerModel.isMaximized());
		maximizeOption.setSelected(headerModel.isMaximized());
		maximizeOption.setEnabled(headerModel.isMaximizeAllowed());

		autoHide.setEnabled(headerModel.isAutoHideAllowed());
		autoHide.setSelected(headerModel.isAutoHideEnabled());
	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		init();
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}
}
