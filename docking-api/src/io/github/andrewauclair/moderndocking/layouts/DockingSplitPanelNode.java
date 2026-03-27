/*
Copyright (c) 2022-2023 Andrew Auclair

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

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.settings.Settings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JSplitPane;

/**
 * Layout node representing a flat N-ary split (N >= 2 children).
 * Stores divider positions as fractions of this node's own rect so that
 * each divider's absolute position is independent of its siblings.
 */
public class DockingSplitPanelNode implements DockingLayoutNode {

    private final DockingAPI docking;
    private final List<DockingLayoutNode> children;
    private final int orientation;
    /**
     * Length is children.size() - 1.
     * dividerPositions[i] is the fraction of this node's axis dimension where
     * the divider between children[i] and children[i+1] sits.
     */
    private final double[] dividerPositions;
    private String anchor;
    private DockingLayoutNode parent;

    /**
     * N-ary constructor.
     */
    public DockingSplitPanelNode(DockingAPI docking, List<DockingLayoutNode> children,
                                  int orientation, double[] dividerPositions, String anchor) {
        this.docking = docking;
        this.children = new ArrayList<>(children);
        this.orientation = orientation;
        this.dividerPositions = dividerPositions.clone();
        this.anchor = anchor;
        for (DockingLayoutNode child : this.children) {
            if (child != null) {
                child.setParent(this);
            }
        }
    }

    /**
     * Binary constructor kept for callers that still create 2-child splits
     * (e.g. the dock() helper below and XML loading of old layouts).
     */
    public DockingSplitPanelNode(DockingAPI docking, DockingLayoutNode left,
                                  DockingLayoutNode right, int orientation,
                                  double dividerProportion, String anchor) {
        this(docking, Arrays.asList(left, right), orientation,
                new double[]{dividerProportion}, anchor);
    }

    // ----------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------

    public List<DockingLayoutNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public double[] getDividerPositions() {
        return dividerPositions.clone();
    }

    /** Convenience for binary splits and XML serialisation. */
    public DockingLayoutNode getLeft() {
        return children.get(0);
    }

    /** Convenience for binary splits and XML serialisation. */
    public DockingLayoutNode getRight() {
        return children.get(1);
    }

    public int getOrientation() {
        return orientation;
    }

    /** Convenience for binary splits and XML serialisation. */
    public double getDividerProportion() {
        return dividerPositions[0];
    }

    public String getAnchor() {
        return anchor;
    }

    // ----------------------------------------------------------------
    // DockingLayoutNode interface
    // ----------------------------------------------------------------

    @Override
    public DockingLayoutNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DockingLayoutNode parent) {
        this.parent = parent;
    }

    @Override
    public DockingLayoutNode findNode(String persistentID) {
        for (DockingLayoutNode child : children) {
            if (child != null) {
                DockingLayoutNode found = child.findNode(persistentID);

                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    public void dock(String persistentID, DockingRegion region, double dividerProportion) {
        if (region == DockingRegion.CENTER) {
            return;
        }

        int newOrientation = (region == DockingRegion.EAST || region == DockingRegion.WEST)
                ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;

        Dockable dockable = DockingInternal.get(docking).getDockable(persistentID);
        String className = dockable.getClass().getTypeName();

        DockingLayoutNode newNode = Settings.alwaysDisplayTabsMode()
                ? new DockingTabPanelNode(docking, persistentID, className, anchor,
                        dockable.getTitleText(), dockable.getTabText())
                : new DockingSimplePanelNode(docking, persistentID, className, anchor,
                        dockable.getTitleText(), dockable.getTabText());

        if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
            dividerProportion = 1.0 - dividerProportion;
        }

        DockingLayoutNode left  = (region == DockingRegion.NORTH || region == DockingRegion.WEST)
                ? newNode : this;
        DockingLayoutNode right = (region == DockingRegion.NORTH || region == DockingRegion.WEST)
                ? this : newNode;

        DockingLayoutNode oldParent = parent;
        DockingSplitPanelNode split = new DockingSplitPanelNode(
                docking, left, right, newOrientation, dividerProportion, anchor);
        oldParent.replaceChild(this, split);
    }

    @Override
    public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
        int idx = children.indexOf(child);
        if (idx >= 0) {
            children.set(idx, newChild);
            newChild.setParent(this);
        }
    }
}
