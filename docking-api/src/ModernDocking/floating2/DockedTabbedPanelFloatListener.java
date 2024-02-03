package ModernDocking.floating2;

import ModernDocking.api.DockingAPI;
import ModernDocking.floating.TempFloatingFrame;
import ModernDocking.internal.DockableWrapper;
import ModernDocking.internal.DockedTabbedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DockedTabbedPanelFloatListener extends FloatListener2 {
    private final DockingAPI docking;
    private final DockedTabbedPanel tabs;

    public DockedTabbedPanelFloatListener(DockingAPI docking, DockedTabbedPanel tabs, JComponent dragComponent) {
        super(docking, tabs, dragComponent);

        this.docking = docking;
        this.tabs = tabs;
    }

    @Override
    protected Window getOriginalWindow() {
        return SwingUtilities.windowForComponent(tabs);
    }

    @Override
    protected void undock() {
        List<DockableWrapper> wrappers = new ArrayList<>(tabs.getDockables());

        for (DockableWrapper wrapper : wrappers) {
            docking.undock(wrapper.getDockable());
        }
    }

    @Override
    protected JFrame createFloatingFrame() {
        List<DockableWrapper> wrappers = new ArrayList<>(tabs.getDockables());

        return new TempFloatingFrame(wrappers, tabs.getSelectedTabIndex(), tabs, tabs.getSize());
    }
}
