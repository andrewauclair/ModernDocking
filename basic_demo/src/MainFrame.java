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
import docking.Docking;
import docking.DockingRegion;
import docking.RootDockingPanel;
import exception.FailOnThreadViolationRepaintManager;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MainFrame extends JFrame {
	public MainFrame() {
		setTitle("Test Docking Framework");

		setSize(800, 600);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SimplePanel one = new SimplePanel("one", "one");
		SimplePanel two = new SimplePanel("two", "two");
		SimplePanel three = new SimplePanel("three", "three");
		SimplePanel four = new SimplePanel("four", "four");
		SimplePanel five = new SimplePanel("five", "five");
		SimplePanel six = new SimplePanel("six", "six");
		SimplePanel seven = new SimplePanel("seven", "seven");
		SimplePanel eight = new SimplePanel("eight", "eight");

		JToolBar toolBar = new JToolBar();
		toolBar.add(new JButton("Test1"));
		toolBar.add(new JButton("Test2"));
		toolBar.add(new JButton("Test3"));

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		add(toolBar, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		RootDockingPanel dockingPanel = new RootDockingPanel();
		Docking.registerDockingPanel(dockingPanel, this);
		Random rand = new Random();
		dockingPanel.setBackground(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));

		add(dockingPanel, gbc);

		Docking.setMainFrame(this);

		Docking.dock(this, one);
		Docking.dock(this, two);
		Docking.dock(this, three);
		Docking.dock(this, four);
		Docking.dock(this, five);
		Docking.dock(this, six);
		Docking.dock(this, seven);
		Docking.dock(this, eight, DockingRegion.SOUTH);
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(() -> {
			FailOnThreadViolationRepaintManager.install();
			FailOnThreadViolationRepaintManager.install();

			MainFrame mainFrame = new MainFrame();
			mainFrame.setVisible(true);
		});
	}
}
