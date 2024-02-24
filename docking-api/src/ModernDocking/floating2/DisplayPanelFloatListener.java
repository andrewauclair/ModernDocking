package ModernDocking.floating2;

import ModernDocking.Dockable;
import ModernDocking.api.DockingAPI;
import ModernDocking.floating.TempFloatingFrame;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class DisplayPanelFloatListener extends FloatListener2 {
    private final DockingAPI docking;
    private final DisplayPanel panel;

    public DisplayPanelFloatListener(DockingAPI docking, DisplayPanel panel) {
        super(docking, panel);
        this.docking = docking;
        this.panel = panel;
    }

    public Dockable getDockable() {
        return panel.getWrapper().getDockable();
    }

    @Override
    protected Window getOriginalWindow() {
        return panel.getWrapper().getWindow();
    }

    @Override
    protected void undock() {
        docking.undock(panel.getWrapper().getDockable());
    }

    @Override
    protected JFrame createFloatingFrame() {
        if (Settings.alwaysDisplayTabsMode(panel.getWrapper().getDockable())) {
            return new TempFloatingFrame(Collections.singletonList(panel.getWrapper()), 0, panel, panel.getSize());
        }
        return new TempFloatingFrame(panel.getWrapper(), panel, panel.getSize());
    }
}
