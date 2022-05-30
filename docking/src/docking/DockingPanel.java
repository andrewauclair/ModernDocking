package docking;

import javax.swing.*;

// Docking panel with docking regions of: north, south, east, west and center
public abstract class DockingPanel extends JPanel {
//	public abstract void dock();

	// undock the given dockable, returns true if the dockable was found and removed
	public abstract boolean undock(Dockable dockable);
}
