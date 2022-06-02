package docking;

import javax.swing.*;
import java.awt.*;

public interface Dockable {
	// provide the drag source to the docking framework
	// this is usually a title bar JPanel
	JComponent dragSource();

	// provide the persistent ID to the docking framework
	// this should be unique in the application (will be verified when adding dockable)
	String persistentID();

	// provide the tab text to the docking framework
	// the tab text to be displayed when Dockable is in a tabbed pane. Does not need to be unique
	// NOTE: this text should be static. If it needs to change, then the Dockable needs to be undocked and docked again.
	String tabText();
}
