package com.auclair.packet;

import com.auclair.packet.view.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import docking.DockingColors;

import javax.swing.*;
import java.awt.*;

public class Main {
	public static void main(String[] args) {
		UIManager.put("docking.handles.north", new FlatSVGIcon("icons/layout-3 (1).svg"));

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
