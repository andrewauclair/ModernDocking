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
package ModernDocking.internal;

import javax.swing.*;
import java.awt.*;

public class DockingColors {
	private static class Entry {
		String key = ""; // key to look up color in UIManager
		Color color = Color.RED; // the cached color, refreshed when UIManager l&f changes

		public void setColor(String key) {
			this.key = key;
		}

		public void update() {
			color = UIManager.getColor(key);

			if (color == null) {
				throw new RuntimeException("Unable to find color: " + key);
			}
		}
	}

	private final Entry handlesBackground = new Entry();
	private final Entry handlesBackgroundBorder = new Entry();
	private final Entry handlesOutline = new Entry();
	private final Entry handlesFill = new Entry();

	private final Entry dockingOverlay = new Entry();
	private final Entry dockingOverlayBorder = new Entry();
	private int dockingOverlayAlpha = 60;
	private String dockingOverlayAlphaKey = "";

	private static final DockingColors colors = new DockingColors();

	private DockingColors() {
		UIManager.addPropertyChangeListener(e -> {
			if (e.getPropertyName().equals("lookAndFeel")) {
				handlesBackground.update();
				handlesBackgroundBorder.update();
				handlesOutline.update();
				handlesFill.update();
				dockingOverlay.update();
				dockingOverlayBorder.update();
				colors.dockingOverlayAlpha = UIManager.getInt(colors.dockingOverlayAlphaKey);
			}
		});
	}

	public static void setHandlesBackground(String key) {
		colors.handlesBackground.setColor(key);
	}

	public static Color getHandlesBackground() {
		return colors.handlesBackground.color;
	}

	public static void setHandlesBackgroundBorder(String key) {
		colors.handlesBackgroundBorder.setColor(key);
	}

	public static Color getHandlesBackgroundBorder() {
		return colors.handlesBackgroundBorder.color;
	}

	public static void setHandlesOutline(String key) {
		colors.handlesOutline.setColor(key);
	}

	public static Color getHandlesOutline() {
		return colors.handlesOutline.color;
	}

	public static void setHandlesFill(String key) {
		colors.handlesFill.setColor(key);
	}

	public static Color getHandlesFill() {
		return colors.handlesFill.color;
	}

	public static void setDockingOverlay(String key) {
		colors.dockingOverlay.setColor(key);
	}

	public static Color getDockingOverlay() {
		Color color = colors.dockingOverlay.color;
		return new Color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, colors.dockingOverlayAlpha / 255.0f);
	}

	public static void setDockingOverlayBorder(String key) {
		colors.dockingOverlayBorder.setColor(key);
	}

	public static Color getDockingOverlayBorder() {
		Color color = colors.dockingOverlayBorder.color;
		return new Color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, colors.dockingOverlayAlpha / 255.0f);
	}

	public static void setDockingOverlayAlpha(String key) {
		colors.dockingOverlayAlphaKey = key;
	}
}
