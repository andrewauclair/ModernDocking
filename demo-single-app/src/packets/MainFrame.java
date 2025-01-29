/*
Copyright (c) 2023 Andrew Auclair

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
package packets;

import io.github.andrewauclair.moderndocking.app.AppState;
import io.github.andrewauclair.moderndocking.app.DockableMenuItem;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import exception.FailOnThreadViolationRepaintManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {
	private final PacketBytesPanel bytesPanel;
	private final PacketInfoPanel infoPanel;

	public MainFrame() {
		Docking.initialize(this);

		setTitle("Package Capture Demo");

		setSize(800, 600);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu view = new JMenu("View");
		bytesPanel = new PacketBytesPanel();
		view.add(new DockableMenuItem(bytesPanel));
		infoPanel = new PacketInfoPanel();
		view.add(new DockableMenuItem(infoPanel));
		menuBar.add(view);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;

		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		RootDockingPanel dockingPanel = new RootDockingPanel(this);

		add(dockingPanel, gbc);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			configureLookAndFeel(args);

			MainFrame mainFrame = new MainFrame();
			mainFrame.setVisible(true);

			// now that the main frame is setup with the defaults, we can restore the layout
			AppState.setPersistFile(new File("auto_persist_layout.xml"));

			try {
				AppState.restore();
			}
			catch (DockingLayoutException e) {
				e.printStackTrace();
			}

			AppState.setAutoPersist(true);
		});
	}

	private static void configureLookAndFeel(String[] args) {
		try {
			FlatLaf.registerCustomDefaultsSource( "docking" );

			if (args.length > 1) {
				System.setProperty("flatlaf.uiScale", args[1]);
			}

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

		// this is an app to test the docking framework, we want to make sure we detect EDT violations as soon as possible
		FailOnThreadViolationRepaintManager.install();
	}
}
