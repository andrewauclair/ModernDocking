package ModernDocking;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DockableToolbar;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;

public class RootDockingPanel extends RootDockingPanelAPI {
    public RootDockingPanel(DockingAPI docking, Window window) {
        super(docking, window);
    }

    public RootDockingPanel(DockingAPI docking, Window window, EnumSet<DockableToolbar.Location> supportedToolbars) {
        super(docking, window, supportedToolbars);
    }
}
