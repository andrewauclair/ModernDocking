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
