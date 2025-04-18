/*
Copyright (c) 2023 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.settings.Settings;

/**
 * The root node of a docking layout
 */
public class DockingLayoutRootNode implements DockingLayoutNode {
    private final DockingAPI docking;
    private DockingLayoutNode node;

    /**
     * Create a new instance for the docking instance
     *
     * @param docking Docking instance this root node is tied to
     */
    public DockingLayoutRootNode(DockingAPI docking) {
        this.docking = docking;
    }

    @Override
    public DockingLayoutNode findNode(String persistentID) {
        return node.findNode(persistentID);
    }

    @Override
    public void dock(String persistentID, DockingRegion region, double dividerProportion) {
        if (node != null) {
            node.dock(persistentID, region, dividerProportion);
        }
        else if (DockingInternal.get(docking).hasAnchor(persistentID)) {
            String className = DockingInternal.get(docking).getDockable(persistentID).getClass().getTypeName();

            node = new DockingAnchorPanelNode(docking, persistentID, className);
        }
        else if (Settings.alwaysDisplayTabsMode()) {
            node = new DockingTabPanelNode(docking, persistentID, "", "");
            node.setParent(this);
        }
        else {
            String className = DockingInternal.get(docking).getDockable(persistentID).getClass().getTypeName();

            node = new DockingSimplePanelNode(docking, persistentID, className, "");
            node.setParent(this);
        }
    }

    @Override
    public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
        if (node == child) {
            node = newChild;
            node.setParent(this);
        }
    }

    @Override
    public DockingLayoutNode getParent() {
        return null;
    }

    @Override
    public void setParent(DockingLayoutNode parent) {
    }

    /**
     * Get the first node in the hierarchy
     * @return The primary node. This will be null if the root is empty
     */
    public DockingLayoutNode getNode() {
        return node;
    }
}
