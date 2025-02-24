/*
Copyright (c) 2025 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.settings.Settings;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class DockedAnchorPanel extends DockingPanel {
    private final DockingAPI docking;

    private final DockableWrapper anchor;

    /**
     * Parent panel of this simple panel
     */
    private DockingPanel parent;

    public DockedAnchorPanel(DockingAPI docking, DockableWrapper anchor) {
        setLayout(new BorderLayout());

        anchor.setParent(this);

        this.docking = docking;
        this.anchor = anchor;

        add(anchor.getDisplayPanel(), BorderLayout.CENTER);
    }

    @Override
    public String getAnchor() {
        return null;
    }

    @Override
    public void setParent(DockingPanel parent) {
        this.parent = parent;
    }

    @Override
    public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
        // when docking to an anchor we replace the anchor with the new dockable always
        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

        DockingPanel newPanel;

        if (Settings.alwaysDisplayTabsMode()) {
            newPanel = new DockedTabbedPanel(docking, wrapper, anchor.getDockable().getPersistentID());
        }
        else {
            newPanel = new DockedSimplePanel(docking, wrapper, anchor.getDockable().getPersistentID());
        }

        parent.replaceChild(this, newPanel);
    }

    @Override
    public void undock(Dockable dockable) {
    }

    @Override
    public void replaceChild(DockingPanel child, DockingPanel newChild) {
    }

    @Override
    public void removeChild(DockingPanel child) {
    }

    public List<DockingPanel> getChildren() {
        return Collections.emptyList();
    }

    public DockableWrapper getWrapper() {
        return anchor;
    }
}
