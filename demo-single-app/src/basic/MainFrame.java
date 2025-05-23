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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubDarkIJTheme;
import exception.FailOnThreadViolationRepaintManager;
import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.AppState;
import io.github.andrewauclair.moderndocking.app.ApplicationLayoutMenuItem;
import io.github.andrewauclair.moderndocking.app.DockableMenuItem;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.DockingState;
import io.github.andrewauclair.moderndocking.app.LayoutPersistence;
import io.github.andrewauclair.moderndocking.app.LayoutsMenu;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.settings.Settings;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import picocli.CommandLine;

public class MainFrame extends JFrame implements Callable<Integer> {
	private final File layoutFile;
	@CommandLine.Option(names = "--laf", defaultValue = "light", description = "look and feel to use. one of: system, light, dark, github-dark or solarized-dark")
	String lookAndFeel;

	@CommandLine.Option(names = "--enable-edt-violation-detector", defaultValue = "false", description = "enable the Event Dispatch Thread (EDT) violation checker")
	boolean edtViolationDetector;

	@CommandLine.Option(names = "--ui-scale", defaultValue = "1", description = "scale to use for the FlatLaf.uiScale value")
	int uiScale;

	@CommandLine.Option(names = "--always-use-tabs", defaultValue = "false", description = "always use tabs, even when there is only 1 dockable in the tab group")
	boolean alwaysUseTabs;

	@CommandLine.Option(names = "--tab-location", defaultValue = "NONE", description = "Location to display tabs. values: ${COMPLETION-CANDIDATES}")
	DockableTabPreference tabLocation;

	@CommandLine.Option(names = "--create-docking-instance", defaultValue = "false", description = "create a separate instance of the framework for this MainFrame")
	boolean createDockingInstance;

	public MainFrame(File layoutFile) {
		this.layoutFile = layoutFile;

		setSize(800, 600);

		setTitle("Modern Docking Basic Demo");

		if (alwaysUseTabs) {
			if (tabLocation == DockableTabPreference.TOP) {
				Settings.setDefaultTabPreference(DockableTabPreference.TOP_ALWAYS);
			}
			else {
				Settings.setDefaultTabPreference(DockableTabPreference.BOTTOM_ALWAYS);
			}
		}
		else {
			Settings.setDefaultTabPreference(tabLocation);
		}

		Settings.setActiveHighlighterEnabled(false);

		Docking.initialize(this);
		DockingUI.initialize();


		Docking.addNewFloatingFrameListener(new NewFloatingFrameListener() {
			@Override
			public void newFrameCreated(JFrame frame, RootDockingPanelAPI root) {
				frame.setTitle("Testing New Floating Frame Listener");
			}

			@Override
			public void newFrameCreated(JFrame frame, RootDockingPanelAPI root, Dockable dockable) {
				frame.setTitle("Testing New Floating Frame Listener");
			}
		});

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu file = new JMenu("File");
		menuBar.add(file);

		JCheckBoxMenuItem persistOn = new JCheckBoxMenuItem("Auto Persist Layout");
		file.add(persistOn);

		persistOn.setSelected(true);

		persistOn.addActionListener(e -> AppState.setAutoPersist(persistOn.isSelected()));

		JMenuItem saveLayout = new JMenuItem("Save Layout to File...");
		file.add(saveLayout);

		saveLayout.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showSaveDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				ApplicationLayout layout = DockingState.getApplicationLayout();

				try {
					LayoutPersistence.saveLayoutToFile(selectedFile, layout);
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

			SimplePanel panel = new SimplePanel(panelName, panelName, panelName);
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
					layout = LayoutPersistence.loadApplicationLayoutFromFile(selectedFile);
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

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		SimplePanel one = new SimplePanel("one", "Panel One", "one");
		SimplePanel two = new SimplePanel("two", "Panel Two", "two");
		SimplePanel three = new SimplePanel("three", "Panel Three", "three");
		SimplePanel four = new SimplePanel("four", "Panel Four", "four");
		SimplePanel five = new SimplePanel("five", "Panel Five", "five");
		SimplePanel six = new SimplePanel("six", "Panel Six", "six");
		SimplePanel seven = new SimplePanel("seven", "Panel Seven", "seven", DockableStyle.CENTER_ONLY);
		SimplePanel eight = new SimplePanel("eight", "Panel Eight", "eight");
		ToolPanel explorer = new ToolPanel("Explorer", "explorer", DockableStyle.VERTICAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		ToolPanel output = new OutputPanel("Output", "output", DockableStyle.HORIZONTAL, new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/light/icons8-vga-16.png"))));
		AlwaysDisplayedPanel alwaysDisplayed = new AlwaysDisplayedPanel("always displayed", "always-displayed");
		ScrollingWithToolbarPanel scrolling = new ScrollingWithToolbarPanel();
		FixedSizePanel fixedSize = new FixedSizePanel();

		PropertiesDemoPanel propertiesDemoPanel = new PropertiesDemoPanel();

		ThemesPanel themes = new ThemesPanel();

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
			Docking.updateTabInfo("one");
		});

		JMenu view = new JMenu("View");
		menuBar.add(view);

		JMenuItem createNewDockable = new JMenuItem("Generate Random Dockable");
		createNewDockable.addActionListener(e -> {
			SimplePanel rand = new SimplePanel("rand", generateString("alpha", 6), generateString("abcdefg", 10));
			Docking.dock(rand, one, DockingRegion.WEST);
		});
		view.add(createNewDockable);

		JMenuItem createDynamicDockable = new JMenuItem("Create Dynamic Dockable");
		createDynamicDockable.addActionListener(e -> {
			IncorrectDynamicDockable incorrect = new IncorrectDynamicDockable();
			Docking.dock(incorrect, one, DockingRegion.CENTER);
		});
		view.add(createDynamicDockable);

		view.add(actionListenDock(one));
		JMenuItem oneToCenter = new JMenuItem("one (to center of window)");
		oneToCenter.addActionListener(e -> Docking.dock("one", this));
		view.add(oneToCenter);
		view.add(actionListenDock(two));
		view.add(actionListenDock(three));
		view.add(actionListenDock(four));
		view.add(actionListenDock(five));
		view.add(actionListenDock(six));
		view.add(actionListenDock(seven));
		view.add(actionListenDock(eight));
		view.add(actionListenDock(explorer));
		view.add(actionListenDock(output));
		view.add(actionListenDock(fixedSize));
		view.add(new DockableMenuItem("non-existent-dockable", "Does Not Exist"));
		view.add(actionListenDock(propertiesDemoPanel));
		view.add(new DockableMenuItem(() -> ((Dockable) alwaysDisplayed).getPersistentID(), ((Dockable) alwaysDisplayed).getTabText()));
		view.add(changeText);
		view.add(actionListenDock(themes));
		view.add(actionListenDock(scrolling));

		Anchor anchor = new Anchor();

		Docking.registerDockingAnchor(anchor);

		JMenuItem createAnchor = new JMenuItem("Create Anchor");
		createAnchor.addActionListener(e -> {


			Docking.dock(anchor, one, DockingRegion.EAST);
		});
		view.add(createAnchor);

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
//		dockingPanel.setPinningSupported(false);

		gbc.insets = new Insets(0, 5, 5, 5);

		add(dockingPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;

		WindowLayoutBuilderAPI layoutBuilder = new WindowLayoutBuilder(alwaysDisplayed.getPersistentID())
				.dock(one.getPersistentID(), alwaysDisplayed.getPersistentID())
				.dock(two.getPersistentID(), one.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(three.getPersistentID(), DockingRegion.WEST)
				.dock(four.getPersistentID(), two.getPersistentID(), DockingRegion.CENTER)
				.dock(propertiesDemoPanel.getPersistentID(), four.getPersistentID(), DockingRegion.CENTER)
				.dockToRoot(output.getPersistentID(), DockingRegion.SOUTH)
				.dockToRoot(themes.getPersistentID(), DockingRegion.EAST)
				.dock(explorer.getPersistentID(), themes.getPersistentID(), DockingRegion.CENTER)
				.display(themes.getPersistentID());

		layoutBuilder.setProperty(one.getPersistentID(), SimplePanel.STRING_TEST_PROP, "value");
		layoutBuilder.setProperty(one.getPersistentID(), SimplePanel.TEST_INT_1_PROP, 100);

		ApplicationLayout defaultLayout = layoutBuilder.buildApplicationLayout();

		DockingLayouts.addLayout("default", defaultLayout);
//		AppState.setDefaultApplicationLayout(defaultLayout);
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

	private JMenuItem actionListenDock(Dockable dockable) {
		return new DockableMenuItem(dockable.getPersistentID(), dockable.getTabText());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new CommandLine(new MainFrame(new File("basic_demo_layout.xml"))).execute(args));
	}

	private void configureLookAndFeel() {
		try {
			FlatLaf.registerCustomDefaultsSource("docking");

			System.setProperty("flatlaf.uiScale", String.valueOf(uiScale));

			switch (lookAndFeel) {
				case "light":
					UIManager.setLookAndFeel(new FlatLightLaf());
					break;
				case "dark":
					UIManager.setLookAndFeel(new FlatDarkLaf());
					break;
				case "github-dark":
					UIManager.setLookAndFeel(new FlatMTGitHubDarkIJTheme());
					break;
				case "solarized-dark":
					UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
					break;
				default:
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
							 UnsupportedLookAndFeelException ex) {
						throw new RuntimeException(ex);
					}
					break;
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
	public Integer call() {
		configureLookAndFeel();

		// now that the main frame is set up with the defaults, we can restore the layout
		AppState.setPersistFile(layoutFile);

		try {
			AppState.restore();
		} catch (DockingLayoutException e) {
			// something happened trying to load the layout file, record it here
			e.printStackTrace();
		}

		AppState.setAutoPersist(true);
		SwingUtilities.invokeLater(() -> setVisible(true));
		return 0;
	}
}
