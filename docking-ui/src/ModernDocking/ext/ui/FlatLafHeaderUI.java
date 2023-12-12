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
package ModernDocking.ext.ui;

import ModernDocking.ui.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;

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

		setBackground(DockingSettings.getHeaderBackground());
		Color foreground = DockingSettings.getHeaderForeground();

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
