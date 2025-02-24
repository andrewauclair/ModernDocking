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
package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;

import java.util.HashMap;
import java.util.Map;

public class DockingAnchorPanelNode implements DockingLayoutNode {
    private final DockingAPI docking;
    private final String persistentID;
    private final String className;

    private Map<String, Property> properties = new HashMap<>();
    private DockingLayoutNode parent;

    /**
     * Create a new DockingAnchorPanelNode with just a persistent ID
     *
     * @param persistentID The persistent ID of the anchor
     */
    public DockingAnchorPanelNode(DockingAPI docking, String persistentID, String className) {
        this.docking = docking;
        this.persistentID = persistentID;
        this.className = className;
    }

    @Override
    public DockingLayoutNode findNode(String persistentID) {
        return null;
    }

    @Override
    public void dock(String persistentID, DockingRegion region, double dividerProportion) {
    }

    @Override
    public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
    }

    @Override
    public DockingLayoutNode getParent() {
        return parent;
    }

    public void setParent(DockingLayoutNode parent) {
        this.parent = parent;
    }

    /**
     * Get Persistent ID of the contained dockable
     *
     * @return Persistent ID
     */
    public String getPersistentID() {
        return persistentID;
    }

    public String getClassName() {
        return className;
    }
}
