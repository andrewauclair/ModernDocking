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
import docking.Dockable;
import docking.Docking;
import docking.DockingListeners;
import event.DockingListener;
import event.MaximizeListener;
import floating.DockingHandle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO create a "model" of the header that this JPanel implements against. that way users can create their own and the logic that we want to happen is in the model
public class FlatLafDragHeader extends JPanel implements MaximizeListener, DockingListener {
	private final JPopupMenu settings = new JPopupMenu();
	private final Dockable dockable;

	private final JMenuItem pinned = new JMenuItem("Pinned");
	private final JMenuItem unpinned = new JMenuItem("Unpinned");
	private final JMenuItem undock = new JMenuItem("Undock");
	private final JMenuItem window = new JMenuItem("Window");

	private final JLabel maximizedIndicator = new JLabel("Maximized");
	private final JCheckBoxMenuItem maximizeOption = new JCheckBoxMenuItem("Maximize");

	public FlatLafDragHeader(Dockable dockable, String title) {
		this.dockable = dockable;
		setOpaque(true);

		DockingListeners.addMaximizeListener(this);
		DockingListeners.addDockingListener(this);

		FlatSVGIcon settings = new FlatSVGIcon("icons/settings.svg");

		JButton more = new JButton(settings);
		more.addActionListener(e -> this.settings.show(more, more.getWidth(), more.getHeight()));

		FlatSVGIcon closeIcon = new FlatSVGIcon("icons/x.svg");
		JButton close = new JButton(closeIcon);

		close.addActionListener(e -> Docking.undock(dockable));

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

		JLabel label = new JLabel(title);
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

		if (dockable.hasMoreOptions() || dockable.allowMinMax() || dockable.allowPinning()) {
			addOptions();

			add(more, gbc);
			gbc.gridx++;
		}
		if (dockable.allowClose()) {
			add(close, gbc);
			gbc.gridx++;
		}
	}

	private void addOptions() {
		dockable.addMoreOptions(settings);

		if (settings.getComponentCount() > 0) {
			settings.addSeparator();
		}

		undock.setEnabled(dockable.allowClose());
		window.setEnabled(dockable.floatingAllowed());

		pinned.addActionListener(e -> Docking.pinDockable(dockable));
		unpinned.addActionListener(e -> Docking.unpinDockable(dockable));
		undock.addActionListener(e -> Docking.undock(dockable));
		window.addActionListener(e -> Docking.newWindow(dockable));

		JMenu viewMode = new JMenu("View Mode");
		viewMode.add(pinned);
		viewMode.add(unpinned);
		viewMode.add(undock);
		viewMode.add(window);

		settings.add(viewMode);
		settings.addSeparator();

		settings.add(maximizeOption);

		maximizeOption.addActionListener(e -> {
			boolean maxed = Docking.isMaximized(dockable);

			maximizeOption.setSelected(!maxed);
			maximizedIndicator.setVisible(!maxed);

			if (maxed) {
				Docking.minimize(dockable);
			}
			else {
				Docking.maximize(dockable);
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
	public void maximized(Dockable dockable, boolean maximized) {
		if (this.dockable == dockable) {
			maximizedIndicator.setVisible(maximized);
			maximizeOption.setSelected(maximized);
		}
	}

	@Override
	public void docked(String persistentID) {
		if (dockable.persistentID().equals(persistentID)) {
			pinned.setEnabled(Docking.pinningAllowed(dockable));
			unpinned.setEnabled(Docking.pinningAllowed(dockable));
		}
	}

	@Override
	public void undocked(String persistentID) {

	}
}
