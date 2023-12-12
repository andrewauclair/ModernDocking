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
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.app.*;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.DockingLayouts;
import ModernDocking.settings.Settings;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import ModernDocking.ext.ui.DockingUI;
import exception.FailOnThreadViolationRepaintManager;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;

public class MainFrame extends JFrame implements Callable<Integer> {
	private final File layoutFile;
	@CommandLine.Option(names = "--laf", required = true, description = "look and feel to use. one of: system, light, dark, github-dark or solarized-dark")
	String lookAndFeel;

	@CommandLine.Option(names = "--enable-edt-violation-detector", defaultValue = "false", description = "enable the Event Dispatch Thread (EDT) violation checker")
	boolean edtViolationDetector;

	@CommandLine.Option(names = "--ui-scale", defaultValue = "1", description = "scale to use for the FlatLaf.uiScale value")
	int uiScale;

	@CommandLine.Option(names = "--always-use-tabs", defaultValue = "false", description = "always use tabs, even when there is only 1 dockable in the tab group")
	boolean alwaysUseTabs;

	private DockingAPI docking;

	public MainFrame(File layoutFile) {
		this.layoutFile = layoutFile;
	}

	static Random rng = new Random();
	public static String generateString(String characters, int length)
	{
		char[] text = new char[length];

		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	@Override
	public void setVisible(boolean visible) {
		setSize(800, 600);

		setTitle("Modern Docking Basic Demo (" + layoutFile.getName() + ")");

		docking = new Docking(this);

		Settings.setAlwaysDisplayTabMode(alwaysUseTabs);

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

				ApplicationLayout layout = docking.getDockingState().getApplicationLayout();

				try {
					docking.getLayoutPersistence().saveLayoutToFile(selectedFile, layout);
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

			SimplePanel panel = new SimplePanel(docking, panelName, panelName);
			docking.dock(panel, MainFrame.this, DockingRegion.EAST);
		});
		file.add(createPanel);

		loadLayout.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				ApplicationLayout layout = null;
				try {
					layout = docking.getLayoutPersistence().loadApplicationLayoutFromFile(selectedFile);
				}
				catch (DockingLayoutException ex) {
					ex.printStackTrace();
				}

				if (layout != null) {
					docking.getDockingState().restoreApplicationLayout(layout);
				}
			}
		});

		JMenu window = new JMenu("Window");
		window.add(new LayoutsMenu(docking));

		menuBar.add(window);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SimplePanel one = new SimplePanel(docking, "one", "one");
		SimplePanel two = new SimplePanel(docking, "two", "two");
		SimplePanel three = new SimplePanel(docking, "three", "three");
		SimplePanel four = new SimplePanel(docking, "four", "four");
		SimplePanel five = new SimplePanel(docking, "five", "five");
		SimplePanel six = new SimplePanel(docking, "six", "six");
		SimplePanel seven = new SimplePanel(docking, "seven", "seven");
		SimplePanel eight = new SimplePanel(docking, "eight", "eight");
		ToolPanel explorer = new ToolPanel(docking, "Explorer", "explorer", DockableStyle.VERTICAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		ToolPanel output = new OutputPanel(docking, "Output", "output", DockableStyle.HORIZONTAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		AlwaysDisplayedPanel alwaysDisplayed = new AlwaysDisplayedPanel(docking, "always displayed", "always-displayed");

		ThemesPanel themes = new ThemesPanel(docking);

		one.setTitleBackground(new Color(0xa1f2ff));
		two.setTitleBackground(new Color(0xdda1ff));
		three.setTitleBackground(new Color(0xffaea1));
		four.setTitleBackground(new Color(0xc3ffa1));

		one.setTitleForeground(Color.black);
		two.setTitleForeground(Color.black);
		three.setTitleForeground(Color.black);
		four.setTitleForeground(Color.black);

		JMenuItem changeText = new JMenuItem("Change tab text");
		changeText.addActionListener(e -> {
			String rand = generateString("abcdefg", 4);
			one.setTabText(rand);
			docking.updateTabInfo("one");
		});

		JMenu view = new JMenu("View");
		menuBar.add(view);

		JMenuItem createNewDockable = new JMenuItem();
		createNewDockable.addActionListener(e -> {
			SimplePanel rand = new SimplePanel(docking, generateString("alpha", 6), generateString("abcdefg", 10));
			docking.dock(rand, one, DockingRegion.WEST);
		});
		view.add(createNewDockable);

		view.add(actionListenDock(docking, one));
		view.add(actionListenDock(docking, two));
		view.add(actionListenDock(docking, three));
		view.add(actionListenDock(docking, four));
		view.add(actionListenDock(docking, five));
		view.add(actionListenDock(docking, six));
		view.add(actionListenDock(docking, seven));
		view.add(actionListenDock(docking, eight));
		view.add(actionListenDock(docking, explorer));
		view.add(actionListenDock(docking, output));
		view.add(new DockableMenuItem(docking, () -> ((Dockable) alwaysDisplayed).getPersistentID(), ((Dockable) alwaysDisplayed).getTabText()));
		view.add(changeText);
		view.add(actionListenDock(docking, themes));

		JMenuItem storeCurrentLayout = new JMenuItem("Store Current Layout...");
		storeCurrentLayout.addActionListener(e -> {
			String layoutName = JOptionPane.showInputDialog("Name of Layout");

			DockingLayouts.addLayout(layoutName, docking.getDockingState().getApplicationLayout());
		});
		window.add(storeCurrentLayout);

		JMenuItem restoreDefaultLayout = new ApplicationLayoutMenuItem(docking, "default", "Restore Default Layout");
		window.add(restoreDefaultLayout);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		RootDockingPanelAPI dockingPanel = new RootDockingPanel(docking, this);
//		dockingPanel.setPinningSupported(false);

		gbc.insets = new Insets(0, 5, 5, 5);

		add(dockingPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;

		ApplicationLayout defaultLayout = new WindowLayoutBuilder(docking, alwaysDisplayed.getPersistentID())
				.dock(one.getPersistentID(), alwaysDisplayed.getPersistentID())
				.dock(two.getPersistentID(), one.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(three.getPersistentID(), DockingRegion.WEST)
				.dock(four.getPersistentID(), two.getPersistentID(), DockingRegion.CENTER)
				.dockToRoot(output.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(themes.getPersistentID(), DockingRegion.EAST)
				.dock(explorer.getPersistentID(), themes.getPersistentID(), DockingRegion.CENTER)
				.display(themes.getPersistentID())
				.buildApplicationLayout();

		DockingLayouts.addLayout("default", defaultLayout);
		docking.getAppState().setDefaultApplicationLayout(defaultLayout);

		super.setVisible(visible);
	}

	private JMenuItem actionListenDock(DockingAPI docking, Dockable dockable) {
		return new DockableMenuItem(docking, dockable.getPersistentID(), dockable.getTabText());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			DockingUI.initialize();

			MainFrame one = new MainFrame(new File("multiframe_demo_layout_1.xml"));
			MainFrame two = new MainFrame(new File("multiframe_demo_layout_2.xml"));

			new CommandLine(one).execute(args);
			new CommandLine(two).execute(args);

			one.setLocation(100, 100);

			two.setLocation(1000, 100);
		});
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
			else if (lookAndFeel.equals("github-dark")) {
				UIManager.setLookAndFeel(new FlatGitHubDarkIJTheme());
			}
			else if (lookAndFeel.equals("solarized-dark")) {
				UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
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
		SwingUtilities.invokeLater(this::configureLookAndFeel);

		SwingUtilities.invokeLater(() -> {

			setVisible(true);

			// now that the main frame is set up with the defaults, we can restore the layout
			docking.getAppState().setPersistFile(layoutFile);

			try {
				docking.getAppState().restore();
			} catch (DockingLayoutException e) {
				// something happened trying to load the layout file, record it here
				e.printStackTrace();
			}

			docking.getAppState().setAutoPersist(true);
		});
		return 0;
	}

	Optional<Integer> tryParse(String s) {
		try {
			return Optional.of(Integer.parseInt(s));
		}
		catch (NumberFormatException ignored) {
		}
		return Optional.empty();
	}

	void test() {
		tryParse("3").ifPresent(integer -> {
			// use it
		});
	}

}
