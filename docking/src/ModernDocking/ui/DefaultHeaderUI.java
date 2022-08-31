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
package ModernDocking.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// this can be replaced by the user or with the docking-ui FlatLaf header UI
public class DefaultHeaderUI extends JPanel implements DockingHeaderUI {
	private final HeaderController controller;
	private final HeaderModel model;

	private final JPopupMenu settings = new JPopupMenu();

	private final JMenuItem pinned = new JMenuItem("Pinned");
	private final JMenuItem unpinned = new JMenuItem("Unpinned");
	private final JMenuItem window = new JMenuItem("Window");

	private final JLabel maximizedIndicator = new JLabel("Maximized");
	private final JCheckBoxMenuItem maximizeOption = new JCheckBoxMenuItem("Maximize");

	public DefaultHeaderUI(HeaderController controller, HeaderModel model) {
		this.controller = controller;
		this.model = model;

		JButton more = new JButton("S");
		more.addActionListener(e -> this.settings.show(more, more.getWidth(), more.getHeight()));

		JButton close = new JButton("X");
		close.addActionListener(e -> controller.close());

		setupButton(more);
		setupButton(close);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JLabel label = new JLabel(model.titleText());
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

		if (model.hasMoreOptions() || model.isMaximizeAllowed() || model.isPinnedAllowed()) {
			addOptions();

			add(more, gbc);
			gbc.gridx++;
		}
		if (model.isCloseAllowed()) {
			add(close, gbc);
			gbc.gridx++;
		}
	}

	private void addOptions() {
		model.addMoreOptions(settings);

		if (settings.getComponentCount() > 0) {
			settings.addSeparator();
		}

		window.setEnabled(model.isFloatingAllowed());

		pinned.addActionListener(e -> controller.pinDockable());
		unpinned.addActionListener(e -> controller.unpinDockable());
		window.addActionListener(e -> controller.newWindow());

		JMenu viewMode = new JMenu("View Mode");
		viewMode.add(pinned);
		viewMode.add(unpinned);
		viewMode.add(window);

		settings.add(viewMode);
		settings.addSeparator();

		settings.add(maximizeOption);

		maximizeOption.addActionListener(e -> {
			boolean maxed = model.isMaximized();

			maximizeOption.setSelected(!maxed);
			maximizedIndicator.setVisible(!maxed);

			if (maxed) {
				controller.minimize();
			}
			else {
				controller.maximize();
			}
		});
	}

	private void setupButton(JButton button) {
//		Color color = UIManager.getColor("Docking.titlebar.default");
//		button.setBackground(color);
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

	}
}
