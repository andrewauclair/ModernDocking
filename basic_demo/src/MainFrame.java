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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import docking.Docking;
import docking.DockingColors;
import docking.DockingRegion;
import docking.RootDockingPanel;
import exception.FailOnThreadViolationRepaintManager;
import layouts.DockingLayout;
import layouts.DockingLayoutXML;
import layouts.FullAppLayout;
import layouts.FullAppLayoutXML;
import persist.AppState;
import persist.RootDockState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Random;

public class MainFrame extends JFrame {
	static RootDockState state;
	static DockingLayout currentLayout;

	public MainFrame() {
		setTitle("Test Docking Framework");

		setSize(800, 600);

		new Docking(this);

		AppState.setAutoPersist(false);
		AppState.setPersistFile(new File("auto_persist_layout.xml"));

		SwingUtilities.invokeLater(() -> {
			AppState.restore();
			AppState.setAutoPersist(true);
		});

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu file = new JMenu("File");
		menuBar.add(file);

		JMenuItem saveLayout = new JMenuItem("Save Layout to File...");
		file.add(saveLayout);

		saveLayout.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showSaveDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				FullAppLayout layout = Docking.getFullLayout();

				boolean saved = FullAppLayoutXML.saveLayoutToFile(selectedFile, layout);

				if (!saved) {
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to save layout");
				}
			}
		});

		JMenuItem loadLayout = new JMenuItem("Load Layout from File...");
		file.add(loadLayout);

		loadLayout.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				FullAppLayout layout = FullAppLayoutXML.loadLayoutFromFile(selectedFile);

				if (layout != null) {
//					Docking.setLayout(MainFrame.this, layout);
					Docking.restoreFullLayout(layout);
				}
			}
		});

		JLabel test = new JLabel("Test");
		test.setOpaque(true);
		test.setBackground(Color.RED);
		test.setSize(100, 100);
		test.setLocation(100, 100);

//		getLayeredPane().add(test, 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SimplePanel one = new SimplePanel("one", "one");
		SimplePanel two = new SimplePanel("two", "two");
		SimplePanel three = new SimplePanel("three", "three");
		SimplePanel four = new SimplePanel("four", "four");
		SimplePanel five = new SimplePanel("five", "five");
		SimplePanel six = new SimplePanel("six", "six");
		SimplePanel seven = new SimplePanel("seven", "seven");
		SimplePanel eight = new SimplePanel("eight", "eight");
		ToolPanel explorer = new ToolPanel("Explorer", "explorer", true);
		ToolPanel output = new ToolPanel("Output", "output", false);

		JMenu view = new JMenu("View");
		menuBar.add(view);

		view.add(actionListenDock("one"));
		view.add(actionListenDock("two"));
		view.add(actionListenDock("three"));
		view.add(actionListenDock("four"));
		view.add(actionListenDock("five"));
		view.add(actionListenDock("six"));
		view.add(actionListenDock("seven"));
		view.add(actionListenDock("eight"));
		view.add(actionListenDock("explorer"));
		view.add(actionListenDock("output"));

		JToolBar toolBar = new JToolBar();
		JButton test1 = new JButton("Test1");
		test1.addActionListener(e -> Docking.undock(one));
		toolBar.add(test1);
		JButton test2 = new JButton("Test2");
		toolBar.add(test2);
		JButton test3 = new JButton("Test3");
		toolBar.add(test3);
		JButton save = new JButton("save");
		JButton restore = new JButton("restore");
		toolBar.add(save);
		toolBar.add(restore);


		save.addActionListener(e -> {
//			state = Docking.getRootState(this);
			currentLayout = Docking.getCurrentLayout(this);
		});
		restore.addActionListener(e -> {
//			Docking.restoreState(this, state);
			Docking.setLayout(this, currentLayout);
		});

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

		RootDockingPanel dockingPanel = new RootDockingPanel();
		Docking.registerDockingPanel(dockingPanel, this);
		Random rand = new Random();
		dockingPanel.setBackground(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));

		add(dockingPanel, gbc);

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

	private JMenuItem actionListenDock(String persistentID) {
		JMenuItem item = new JMenuItem(persistentID);
		item.addActionListener(e -> {
			if (!Docking.isDocked(Docking.getDockable(persistentID))) {
				Docking.dock(Docking.getDockable(persistentID), this, DockingRegion.SOUTH);
			}
		});
		return item;
	}

	public static void main(String[] args) {
		DockingColors.setHandlesBackground("Docking.handles.background");
		DockingColors.setHandlesBackgroundBorder("Docking.handles.background.border");
		DockingColors.setHandlesOutline("Docking.handles.outline");
		DockingColors.setHandlesFill("Docking.handles.fill");
		DockingColors.setDockingOverlay("Docking.overlay.color");
		DockingColors.setDockingOverlayBorder("Docking.overlay.border.color");
		DockingColors.setDockingOverlayAlpha("Docking.overlay.alpha");

		SwingUtilities.invokeLater(() -> {
			try {
				FlatLaf.registerCustomDefaultsSource( "docking" );

				if (args.length > 1) {
					System.setProperty("flatlaf.uiScale", args[1]);
				}
//			FlatLightLaf.setup();
//			UIManager.setLookAndFeel(new FlatDarkLaf());
//			UIManager.setLookAndFeel(new FlatLightLaf());
				if (args.length > 0 && args[0].equals("light")) {
					UIManager.setLookAndFeel(new FlatLightLaf());
				}
				else if (args.length > 0 && args[0].equals("dark")) {
					UIManager.setLookAndFeel(new FlatDarkLaf());
				}
				else {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
						   UnsupportedLookAndFeelException ex) {
						throw new RuntimeException(ex);
					}
				}
				FlatLaf.updateUI();
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
					   UnsupportedLookAndFeelException ex) {
					throw new RuntimeException(ex);
				}
			}
			UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
			UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

			MainFrame mainFrame = new MainFrame();
			mainFrame.setVisible(true);
		});
	}
}
