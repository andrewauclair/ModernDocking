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
	String tabText();
	// TODO should we have a way to update the tab text if it changes?
}
