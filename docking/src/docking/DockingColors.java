package docking;

import javax.swing.*;
import java.awt.*;

// TODO Need some sensible defaults in case we're using the system l&f

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
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), colors.dockingOverlayAlpha / 255.0f);
	}

	public static void setDockingOverlayAlpha(String key) {
		colors.dockingOverlayAlphaKey = key;
		colors.dockingOverlayAlpha = UIManager.getInt(key);
	}
}
