/*
Copyright (c) 2022-2023 Andrew Auclair

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
package basic;

import ModernDocking.*;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.ApplicationLayoutXML;
import ModernDocking.layouts.DockingLayouts;
import ModernDocking.layouts.WindowLayoutBuilder;
import ModernDocking.persist.AppState;
import ModernDocking.ui.ApplicationLayoutMenuItem;
import ModernDocking.ui.DockableMenuItem;
import ModernDocking.ui.LayoutsMenu;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import exception.FailOnThreadViolationRepaintManager;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.Callable;

public class MainFrame extends JFrame implements Callable<Integer> {
	@CommandLine.Option(names = "--laf", required = true, description = "look and feel to use. one of: system, light or dark")
	String lookAndFeel;

	@CommandLine.Option(names = "--enable-edt-violation-detector", arity = "0..1", defaultValue = "false", description = "enable the Event Dispatch Thread (EDT) violation checker")
	boolean edtViolationDetector;

	@CommandLine.Option(names = "--ui-scale", arity = "0..1", defaultValue = "1", description = "scale to use for the FlatLaf.uiScale value")
	int uiScale;

	public MainFrame() {
		setTitle("Modern Docking Basic Demo");

		setSize(800, 600);

		Docking.initialize(this);

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

				ApplicationLayout layout = DockingState.getApplicationLayout();

				try {
					ApplicationLayoutXML.saveLayoutToFile(selectedFile, layout);
				}
				catch (DockingLayoutException ex) {
					ex.printStackTrace();

					JOptionPane.showMessageDialog(MainFrame.this, "Failed to save layout");
				}
			}
		});

		JMenuItem loadLayout = new JMenuItem("Load Layout from File...");
		file.add(loadLayout);

		JMenuItem createPanel = new JMenuItem("Create Panel...");
		createPanel.addActionListener(e -> {
			String panelName = JOptionPane.showInputDialog("Panel name");

			SimplePanel panel = new SimplePanel(panelName, panelName);
			Docking.dock(panel, MainFrame.this, DockingRegion.EAST);
		});
		file.add(createPanel);

		loadLayout.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				ApplicationLayout layout = null;
				try {
					layout = ApplicationLayoutXML.loadLayoutFromFile(selectedFile);
				}
				catch (DockingLayoutException ex) {
					ex.printStackTrace();
				}

				if (layout != null) {
					DockingState.restoreApplicationLayout(layout);
				}
			}
		});

		JMenu window = new JMenu("Window");
		window.add(new LayoutsMenu());

		menuBar.add(window);

		JMenuItem dialogTest = new JMenuItem("Dialog Test");

		dialogTest.addActionListener(e -> {
			DialogWithDocking dialog = new DialogWithDocking();
			Point loc = getLocation();

			//SwingUtilities.convertPointToScreen(loc, MainFrame.this);
			loc.x -= 250;
			loc.y -= 250;

			loc.x += getSize().width / 2;
			loc.y += getSize().height / 2;
			dialog.setLocation(loc);

			dialog.setSize(500, 500);

			dialog.setVisible(true);

		});
		window.add(dialogTest);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SimplePanel one = new SimplePanel("one", "one");
		SimplePanel two = new SimplePanel("two", "two");
		SimplePanel three = new SimplePanel("three", "three");
		SimplePanel four = new SimplePanel("four", "four");
		SimplePanel five = new SimplePanel("five", "five");
		SimplePanel six = new SimplePanel("six", "six");
		SimplePanel seven = new SimplePanel("seven", "seven");
		SimplePanel eight = new SimplePanel("eight", "eight");
		ToolPanel explorer = new ToolPanel("Explorer", "explorer", DockableStyle.VERTICAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		ToolPanel output = new OutputPanel("Output", "output", DockableStyle.HORIZONTAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		AlwaysDisplayedPanel alwaysDisplayed = new AlwaysDisplayedPanel("always displayed", "always-displayed");

		one.setTitleBackground(new Color(0xa1f2ff));
		two.setTitleBackground(new Color(0xdda1ff));
		three.setTitleBackground(new Color(0xffaea1));
		four.setTitleBackground(new Color(0xc3ffa1));

		JMenuItem changeText = new JMenuItem("Change tab text");
		changeText.addActionListener(e -> {
			one.setTabText("test");
			Docking.updateTabText("one");
		});

		JMenu view = new JMenu("View");
		menuBar.add(view);

		view.add(actionListenDock(one));
//		JMenuItem oneItem = new JMenuItem("one");
//		oneItem.addActionListener(e -> {
//			Docking.dock(one, MainFrame.this, DockingRegion.WEST);
//			Docking.dock(one, two, DockingRegion.SOUTH);
//		});
//		view.add(oneItem);
		view.add(actionListenDock(two));
		view.add(actionListenDock(three));
		view.add(actionListenDock(four));
		view.add(actionListenDock(five));
		view.add(actionListenDock(six));
		view.add(actionListenDock(seven));
		view.add(actionListenDock(eight));
		view.add(actionListenDock(explorer));
		view.add(actionListenDock(output));
		view.add(new DockableMenuItem(() -> ((Dockable) alwaysDisplayed).getPersistentID(), ((Dockable) alwaysDisplayed).getTabText()));
		view.add(changeText);

		JMenuItem storeCurrentLayout = new JMenuItem("Store Current Layout...");
		storeCurrentLayout.addActionListener(e -> {
			String layoutName = JOptionPane.showInputDialog("Name of Layout");

			DockingLayouts.addLayout(layoutName, DockingState.getApplicationLayout());
		});
		window.add(storeCurrentLayout);

		JMenuItem restoreDefaultLayout = new ApplicationLayoutMenuItem("default", "Restore Default Layout");
		window.add(restoreDefaultLayout);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		RootDockingPanel dockingPanel = new RootDockingPanel(this);

		gbc.insets = new Insets(0, 5, 5, 5);

		add(dockingPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;

		ApplicationLayout defaultLayout = new WindowLayoutBuilder(alwaysDisplayed.getPersistentID())
				.dock(one.getPersistentID(), alwaysDisplayed.getPersistentID())
				.dock(two.getPersistentID(), one.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(three.getPersistentID(), DockingRegion.WEST)
				.dock(four.getPersistentID(), two.getPersistentID(), DockingRegion.CENTER)
				.dockToRoot(output.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(explorer.getPersistentID(), DockingRegion.EAST)
				.buildApplicationLayout();

		DockingLayouts.addLayout("default", defaultLayout);
		AppState.setDefaultApplicationLayout(defaultLayout);
	}

	private JMenuItem actionListenDock(Dockable dockable) {
		return new DockableMenuItem(dockable.getPersistentID(), dockable.getTabText());
	}

	public static void main(String[] args) {
		new CommandLine(new MainFrame()).execute(args);
	}

	private void configureLookAndFeel() {
		try {
			FlatLaf.registerCustomDefaultsSource( "docking" );

			System.setProperty("flatlaf.uiScale", String.valueOf(uiScale));

			if (lookAndFeel.equals("light")) {
				UIManager.setLookAndFeel(new FlatLightLaf());
			}
			else if (lookAndFeel.equals("dark")) {
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

		if (edtViolationDetector) {
			// this is an app to test the docking framework, we want to make sure we detect EDT violations as soon as possible
			FailOnThreadViolationRepaintManager.install();
		}
	}

	@Override
	public Integer call() throws Exception {
		SwingUtilities.invokeLater(() -> {
			configureLookAndFeel();

			setVisible(true);

			// now that the main frame is set up with the defaults, we can restore the layout
			AppState.setPersistFile(new File("basic_demo_layout.xml"));

			try {
				AppState.restore();
			} catch (DockingLayoutException e) {
				// something happened trying to load the layout file, record it here
				e.printStackTrace();
			}

			AppState.setAutoPersist(true);
		});
		return 0;
	}
}
