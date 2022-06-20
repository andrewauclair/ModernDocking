package docking;

import javax.swing.*;

public class DockingIcons {
	private static final DockingIcons instance = new DockingIcons();

	private Icon handleWest;
	private Icon handleEast;
	private Icon handleNorth;
	private Icon handleSouth;

	public DockingIcons() {
		UIManager.addPropertyChangeListener(e -> {
			if (e.getPropertyName().equals("lookAndFeel")) {
				updateIcons();
			}
		});

		updateIcons();
	}

	public static Icon handleNorth() {
		return instance.handleNorth;
	}

	private void updateIcons() {
		handleWest = getIcon("docking.handles.west");
		handleEast = getIcon("docking.handles.east");
		handleNorth = getIcon("docking.handles.north");
		handleSouth = getIcon("docking.handles.south");
	}

	private Icon getIcon(String key) {
		Icon icon = UIManager.getIcon(key);

		if (icon != null) {
			return icon;
		}
//		throw new RuntimeException("Missing icon for: " + key);
		return null;
	}
}
