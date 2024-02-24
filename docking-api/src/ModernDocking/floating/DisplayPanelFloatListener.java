/*
Copyright (c) 2024 Andrew Auclair

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
package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.api.DockingAPI;
import ModernDocking.internal.DisplayPanel;
import ModernDocking.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class DisplayPanelFloatListener extends FloatListener {
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
