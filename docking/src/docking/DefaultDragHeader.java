package docking;

import floating.DockingHandlesFrame;

import javax.swing.*;
import java.awt.*;

public class DefaultDragHeader extends JPanel {
	public DefaultDragHeader(Dockable dockable, String title) {
		JButton minMax = new JButton();
		JButton pin = new JButton();
		JButton close = new JButton();

		minMax.setFocusable(false);
		minMax.setOpaque(false);
		minMax.setContentAreaFilled(false);

		minMax.setIcon(new ImageIcon(DefaultDragHeader.class.getResource("/icons/maximize.png")));
		pin.setIcon(new ImageIcon(DefaultDragHeader.class.getResource("/icons/map-marker-3.png")));
		close.setIcon(new ImageIcon(DefaultDragHeader.class.getResource("/icons/close.png")));

		JPanel flow = new JPanel(new FlowLayout());

		flow.add(minMax);
		flow.add(pin);
		flow.add(close);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		add(new JLabel(title), gbc);

		add(flow);
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		
	}
}
