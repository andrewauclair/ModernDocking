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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import docking.Docking;
import docking.DockingRegion;
import docking.RootDockingPanel;
import exception.FailOnThreadViolationRepaintManager;
import persist.RootDockState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class MainFrame extends JFrame {
	static RootDockState state;

	public MainFrame() {
		setTitle("Test Docking Framework");

		setSize(800, 600);

		JLabel test = new JLabel("Test");
		test.setOpaque(true);
		test.setBackground(Color.RED);
		test.setSize(100, 100);
		test.setLocation(100, 100);

//		getLayeredPane().add(test, 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JToolBar toolBar = new JToolBar();
		JButton test1 = new JButton("Test1");
//		test1.addActionListener(e -> Docking.undock(one));
		toolBar.add(test1);
		JButton test2 = new JButton("Test2");
		toolBar.add(test2);
		JButton test3 = new JButton("Test3");
		toolBar.add(test3);
		JButton save = new JButton("save");
		JButton restore = new JButton("restore");
		toolBar.add(save);
		toolBar.add(restore);

		JButton light = new JButton("Light");
		JButton dark = new JButton("Dark");

		light.addActionListener(e -> {
			try {
				UIManager.setLookAndFeel(new FlatLightLaf());
				FlatLaf.updateUI();
			}
			catch (UnsupportedLookAndFeelException ex) {
				throw new RuntimeException(ex);
			}
		});

		dark.addActionListener(e -> {
			try {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				FlatLaf.updateUI();
			}
			catch (UnsupportedLookAndFeelException ex) {
				throw new RuntimeException(ex);
			}
		});

		toolBar.add(light);
		toolBar.add(dark);

		save.addActionListener(e -> {
			state = Docking.getRootState(this);
		});
		restore.addActionListener(e -> Docking.restoreState(this, state));

		test2.addActionListener(e -> test.setVisible(false));
		test3.addActionListener(e -> test.setVisible(true));

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

		new Docking(this);

		RootDockingPanel dockingPanel = new RootDockingPanel();
		Docking.registerDockingPanel(dockingPanel, this);
		Random rand = new Random();
		dockingPanel.setBackground(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));

		add(dockingPanel, gbc);

		JButton close = new JButton(new FlatSVGIcon("icons/x.svg"));
//		gbc.gridy++;
//		add(close, gbc);


		SimplePanel one = new SimplePanel("One", "one");
		SimplePanel two = new SimplePanel("Two", "two");
		SimplePanel three = new SimplePanel("Three", "three");
		SimplePanel four = new SimplePanel("Four", "four");
		SimplePanel five = new SimplePanel("Five", "five");
		SimplePanel six = new SimplePanel("Six", "six");
		SimplePanel seven = new SimplePanel("Seven", "seven");
		SimplePanel eight = new SimplePanel("Eight", "eight");
		ToolPanel explorer = new ToolPanel("Explorer", "explorer", true);
		ToolPanel output = new ToolPanel("Output", "output", false);



		gbc.gridy++;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.fill = GridBagConstraints.NONE;

		JTabbedPane tabs = new JTabbedPane();
		tabs.setTabPlacement(JTabbedPane.BOTTOM);
		tabs.add("Test", null);

		tabs.addChangeListener(e -> test.setVisible(!test.isVisible()));
		tabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				test.setVisible(!test.isVisible());
			}
		});
//		add(tabs, gbc);

		JToggleButton button = new JToggleButton("Test");
		button.addActionListener(e -> test.setVisible(button.isSelected()));



		Docking.dock(one, this);
		Docking.dock(two, one, DockingRegion.SOUTH);
		Docking.dock(three, this, DockingRegion.WEST);
		Docking.dock(four, two, DockingRegion.CENTER);
		Docking.dock(output, this, DockingRegion.SOUTH);
		Docking.dock(explorer, this, DockingRegion.EAST);

		// save the default layout so that we have something to restore, do it later so that the splits setup properly
		SwingUtilities.invokeLater(save::doClick);
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			FlatLaf.registerCustomDefaultsSource( "docking" );
			UIManager.setLookAndFeel(new FlatDarkLaf());
//			UIManager.setLookAndFeel(new FlatLightLaf());

//			System.setProperty("flatlaf.uiScale", "3.0x");
//			System.setProperty("flatlaf.uiScale.enabled", "true");
		}
		catch (Exception e) {
		}
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
		SwingUtilities.invokeLater(() -> {
			FailOnThreadViolationRepaintManager.install();

			MainFrame mainFrame = new MainFrame();
			mainFrame.setVisible(true);
		});
	}
}
