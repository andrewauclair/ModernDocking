package docking.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import docking.Dockable;
import docking.Docking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FlatLafDragHeader extends JPanel {
	public FlatLafDragHeader(Dockable dockable, String title) {
		setOpaque(true);

		JButton more = new JButton(new FlatSVGIcon("icons/more-vertical.svg"));

		FlatSVGIcon max = new FlatSVGIcon("icons/fullscreen.svg");
		FlatSVGIcon min = new FlatSVGIcon("icons/fullscreen-exit.svg");
//		max.setColorFilter(FlatSVGIcon.ColorFilter.create);
		JButton minMax = new JButton(max);
		minMax.addActionListener(e -> {
			if (Docking.isMaximized(dockable)) {
				Docking.minimize(dockable);
				minMax.setIcon(max);
			}
			else {
				Docking.maximize(dockable);
				minMax.setIcon(min);
			}
		});
//		minMax.setForeground(Color.white);

		//		closeIcon.setColorFilter(FlatSVGIcon.ColorFilter.getInstance());
		FlatSVGIcon closeIcon = new FlatSVGIcon("icons/x.svg");
		JButton close = new JButton(closeIcon);

		setupButton(more);
		setupButton(minMax);
//		setupButton(pin);
		setupButton(close);

//		setBackground(new Color(200, 238, 255));
		Color background = new Color(0, 78, 113);
//		setBackground(background);
		Color color = UIManager.getColor("Docking.titlebar.default");
		setBackground(color);
		close.setBackground(color);

//		setBackground(FlatSVGIcon.ColorFilter.getInstance().getMapper().apply(background));
		UIManager.addPropertyChangeListener( e -> {
			if ("lookAndFeel".equals(e.getPropertyName()))
//				state.put( KEY_LAF, UIManager.getLookAndFeel().getClass().getName() );
			{
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

		add(label, gbc);

		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;

		if (dockable.hasMoreOptions()) {
			add(more, gbc);
			gbc.gridx++;
		}

		if (dockable.allowMinMax()) {
			add(minMax, gbc);
			gbc.gridx++;
		}
//		add(pin, gbc);
//		gbc.gridx++;
		if (dockable.allowClose()) {
			add(close, gbc);
			gbc.gridx++;
		}
	}

	private void setupButton(JButton button) {
//		button.setBackground(new Color(200, 238, 255));
//		button.setBackground(new Color(0, 78, 113));
//		button.setForeground(Color.white);
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
}
