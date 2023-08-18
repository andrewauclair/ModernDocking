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
package docking.ui;

import ModernDocking.internal.DockingProperties;
import ModernDocking.ui.DefaultHeaderUI;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Custom DefaultHeaderUI that uses SVG Icons for settings and close when using FlatLaf
 */
public class FlatLafHeaderUI extends DefaultHeaderUI implements DockingHeaderUI {
	/**
	 * Settings icon for the header. Uses an SVG icon for sharper icons
	 */

	private final FlatSVGIcon settingsIcon = new FlatSVGIcon("icons/settings.svg");
	/**
	 * Close icon for the header. Uses an SVG icon for sharper icons
	 */
	private final FlatSVGIcon closeIcon = new FlatSVGIcon("icons/close.svg");

	/**
	 * Construct a new FlatLafHeaderUI
	 *
	 * @param headerController Header controller to use for this UI
	 * @param headerModel Header model to use for this UI
	 */
	public FlatLafHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		super(headerController, headerModel);

		setBackground(UIManager.getColor("TableHeader.background"));
		Color foreground = UIManager.getColor("TableHeader.foreground");

		settingsIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> foreground));
		closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> foreground));
	}

	@Override
	protected void init() {
		super.init();

		settings.setIcon(settingsIcon);
		close.setIcon(closeIcon);
	}

	@Override
	protected void configureColors() {
		super.configureColors();

		setBackground(UIManager.getColor("TableHeader.background"));
		Color foreground = UIManager.getColor("TableHeader.foreground");
		setForeground(foreground);

		settings.setForeground(foreground);
		close.setForeground(foreground);

		if (DockingProperties.isTitlebarBorderEnabled()) {
			Border border = BorderFactory.createMatteBorder(0, 0, DockingProperties.getTitlebarBorderSize(), 0, UIManager.getColor("TableHeader.borderColor"));
			setBorder(border);
		}

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(() -> {
					setBackground(UIManager.getColor("TableHeader.background"));
					Color newForeground = UIManager.getColor("TableHeader.foreground");
					setForeground(newForeground);

					if (DockingProperties.isTitlebarBorderEnabled()) {
						Border border = BorderFactory.createMatteBorder(0, 0, DockingProperties.getTitlebarBorderSize(), 0, UIManager.getColor("TableHeader.borderColor"));
						setBorder(border);
					}
				});

			}
		});
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (settingsIcon != null) {
			settingsIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> fg));
		}
		if (closeIcon != null) {
			closeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> fg));
		}
	}
}
