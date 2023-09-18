package ModernDocking;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DockableToolbar;

import java.awt.*;
import java.util.EnumSet;

public class RootDockingPanel extends RootDockingPanelAPI {
    public RootDockingPanel() {
    }

    public RootDockingPanel(Window window) {
        super(Docking.getSingleInstance(), window);
    }

    public RootDockingPanel(Window window, EnumSet<DockableToolbar.Location> supportedToolbars) {
        super(Docking.getSingleInstance(), window, supportedToolbars);
    }
}
