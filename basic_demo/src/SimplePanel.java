import docking.Dockable;
import docking.Docking;

import javax.swing.*;
import java.awt.*;

public class SimplePanel extends JPanel implements Dockable {
	private final JPanel titlePanel = new JPanel();

	private final String title;
	private final String persistentID;

	public SimplePanel(String title, String persistentID) {
		super(new GridBagLayout());

		Docking.registerDockable(this);

		this.title = title;
		this.persistentID = persistentID;

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		titlePanel.add(new JLabel(title));
		titlePanel.setBackground(new Color(78, 78, 247, 255));

		add(titlePanel, gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		JPanel panel = new JPanel();

		add(panel, gbc);
	}

	@Override
	public JComponent dragSource() {
		return titlePanel;
	}

	@Override
	public String persistentID() {
		return persistentID;
	}

	@Override
	public String tabText() {
		return title;
	}
}
