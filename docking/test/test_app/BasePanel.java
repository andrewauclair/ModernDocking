package test_app;/*
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
import docking.Dockable;
import docking.Docking;

import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel implements Dockable {
	private final JPanel titlePanel = new JPanel();

	private final String title;
	private final String persistentID;

	public BasePanel(String title, String persistentID) {
		super(new GridBagLayout());

		this.title = title;
		this.persistentID = persistentID;

		Docking.registerDockable(this);

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
