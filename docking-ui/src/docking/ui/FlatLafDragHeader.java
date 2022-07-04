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
package docking.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import modern_docking.Dockable;
import modern_docking.Docking;
import modern_docking.DockingListeners;
import modern_docking.event.DockingListener;
import modern_docking.event.MaximizeListener;
import modern_docking.ui.DockingHeaderUI;
import modern_docking.ui.HeaderController;
import modern_docking.ui.HeaderModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO create a "model" of the header that this JPanel implements against. that way users can create their own and the logic that we want to happen is in the model
public class FlatLafDragHeader extends JPanel implements DockingHeaderUI {
	private final JPopupMenu settings = new JPopupMenu();
//	private final Dockable dockable;

	private final JMenuItem pinned = new JMenuItem("Pinned");
	private final JMenuItem unpinned = new JMenuItem("Unpinned");
	private final JMenuItem window = new JMenuItem("Window");

	private final JLabel maximizedIndicator = new JLabel("Maximized");
	private final JCheckBoxMenuItem maximizeOption = new JCheckBoxMenuItem("Maximize");
	private final HeaderController headerController;
	private final HeaderModel headerModel;

	public FlatLafDragHeader(HeaderController headerController, HeaderModel headerModel) {
		this.headerController = headerController;
		this.headerModel = headerModel;
//		this.dockable = dockable;
		setOpaque(true);

		FlatSVGIcon settings = new FlatSVGIcon("icons/settings.svg");

		JButton more = new JButton(settings);
		more.addActionListener(e -> this.settings.show(more, more.getWidth(), more.getHeight()));

		FlatSVGIcon closeIcon = new FlatSVGIcon("icons/x.svg");
		JButton close = new JButton(closeIcon);

		close.addActionListener(e -> headerController.close());

		setupButton(more);
		setupButton(close);

		Color color = UIManager.getColor("Docking.titlebar.default");
		setBackground(color);
		close.setBackground(color);

		UIManager.addPropertyChangeListener( e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				Color bg = UIManager.getColor("Docking.titlebar.default");
				SwingUtilities.invokeLater(() -> {
					setBackground(bg);
					close.setBackground(bg);
				});

			}
		});

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JLabel label = new JLabel(headerModel.titleText());
		label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		add(label, gbc);

		gbc.gridx++;
		gbc.weightx = 0.2;

		maximizedIndicator.setVisible(false);
		maximizedIndicator.setFont(maximizedIndicator.getFont().deriveFont(Font.BOLD));

		add(maximizedIndicator, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;

		if (headerModel.hasMoreOptions() || headerModel.isMaximizeAllowed() || headerModel.isPinnedAllowed()) {
			addOptions();

			add(more, gbc);
			gbc.gridx++;
		}
		if (headerModel.isCloseAllowed()) {
			add(close, gbc);
			gbc.gridx++;
		}
	}

	private void addOptions() {
		headerModel.addMoreOptions(settings);

		if (settings.getComponentCount() > 0) {
			settings.addSeparator();
		}

		window.setEnabled(headerModel.isFloatingAllowed());

		pinned.addActionListener(e -> headerController.pinDockable());
		unpinned.addActionListener(e -> headerController.unpinDockable());
		window.addActionListener(e -> headerController.newWindow());

		JMenu viewMode = new JMenu("View Mode");
		viewMode.add(pinned);
		viewMode.add(unpinned);
		viewMode.add(window);

		settings.add(viewMode);
		settings.addSeparator();

		settings.add(maximizeOption);

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
		Color color = UIManager.getColor("Docking.titlebar.default");
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
	public void update() {
		maximizedIndicator.setVisible(headerModel.isMaximized());
		maximizeOption.setSelected(headerModel.isMaximized());

		pinned.setEnabled(headerModel.isPinnedAllowed() && headerModel.isUnpinned());
		unpinned.setEnabled(headerModel.isPinnedAllowed() && !headerModel.isUnpinned());
	}
}
