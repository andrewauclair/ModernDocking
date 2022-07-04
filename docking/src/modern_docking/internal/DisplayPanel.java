package modern_docking.internal;

import javax.swing.*;
import java.awt.*;

public class DisplayPanel extends JPanel {
	private final DockableWrapper wrapper;

	public DisplayPanel(DockableWrapper wrapper) {
		this.wrapper = wrapper;

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		add((Component) wrapper.getUI(), gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		add((Component) wrapper.getDockable(), gbc);
	}

	public DockableWrapper getWrapper() {
		return wrapper;
	}
}
