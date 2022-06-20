package com.auclair.packet;

import com.auclair.packet.view.MainFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class Main {
	public static void main(String[] args) {
		UIManager.put("docking.handles.north", new FlatSVGIcon("icons/layout-3 (1).svg"));

		SwingUtilities.invokeLater(() -> {
			try {
			FlatLaf.registerCustomDefaultsSource( "docking" );
//			FlatLightLaf.setup();
//			UIManager.setLookAndFeel(new FlatDarkLaf());
			UIManager.setLookAndFeel(new FlatLightLaf());
			FlatLaf.updateUI();
		}
		catch (Exception e) {
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
