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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.DockableTabPreference;
import ModernDocking.DockingRegion;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.settings.Settings;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class InternalRootDockingPanel extends DockingPanel {
    private final DockingAPI docking;
    private final RootDockingPanelAPI rootPanel;

    private DockingPanel panel = null;

    /**
     * South toolbar of this panel. Only created if pinning is supported.
     */
    private DockableToolbar southToolbar = null;
    /**
     * West toolbar of this panel. Only created if pinning is supported.
     */
    private DockableToolbar westToolbar = null;
    /**
     * East toolbar of this panel. Only created if pinning is supported.
     */
    private DockableToolbar eastToolbar = null;

    public InternalRootDockingPanel(DockingAPI docking, RootDockingPanelAPI rootPanel) {
        this.docking = docking;
        this.rootPanel = rootPanel;

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        rootPanel.add(this, gbc);

        southToolbar = new DockableToolbar(docking, rootPanel.getWindow(), rootPanel, ToolbarLocation.SOUTH);
        westToolbar = new DockableToolbar(docking, rootPanel.getWindow(), rootPanel, ToolbarLocation.WEST);
        eastToolbar = new DockableToolbar(docking, rootPanel.getWindow(), rootPanel, ToolbarLocation.EAST);
    }

    public RootDockingPanelAPI getRootPanel() {
        return rootPanel;
    }

    /**
     * Get the main panel contained in this root panel
     *
     * @return Main panel
     */
    public DockingPanel getPanel() {
        return panel;
    }

    /**
     * Check if this root is empty
     *
     * @return True if empty
     */
    public boolean isEmpty() {
        return panel == null;
    }

    /**
     * Set the main panel
     *
     * @param panel New main panel
     */
    public void setPanel(DockingPanel panel) {
        this.panel = panel;

        if (panel != null) {
            this.panel.setParent(this);

            createContents();
        }
    }

    private boolean removeExistingPanel() {
        remove(rootPanel.getEmptyPanel());

        if (panel != null) {
            remove(panel);
            panel = null;
            return true;
        }
        return false;
    }

    @Override
    public void removeNotify() {
        // this class has a default constructor which could be called and docking would be null
        if (docking != null) {
            Window rootWindow = (Window) SwingUtilities.getRoot(this);

            docking.deregisterDockingPanel(rootWindow);
        }

        super.removeNotify();
    }

    @Override
    public void setParent(DockingPanel parent) {
    }

    @Override
    public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

        // pass docking to panel if it exists
        // panel does not exist, create new simple panel
        if (panel != null) {
            panel.dock(dockable, region, dividerProportion);
        }
        else if (Settings.defaultTabPreference() == DockableTabPreference.TOP_ALWAYS) {
            setPanel(new DockedTabbedPanel(docking, wrapper));
            wrapper.setWindow(rootPanel.getWindow());
        }
        else {
            setPanel(new DockedSimplePanel(docking, wrapper));
            wrapper.setWindow(rootPanel.getWindow());
        }
    }

    @Override
    public void undock(Dockable dockable) {
        if (rootPanel.isLocationSupported(ToolbarLocation.WEST) && westToolbar.hasDockable(dockable)) {
            westToolbar.removeDockable(dockable);
        }
        else if (rootPanel.isLocationSupported(ToolbarLocation.EAST) && eastToolbar.hasDockable(dockable)) {
            eastToolbar.removeDockable(dockable);
        }
        else if (rootPanel.isLocationSupported(ToolbarLocation.SOUTH) && southToolbar.hasDockable(dockable)) {
            southToolbar.removeDockable(dockable);
        }

        createContents();
    }

    @Override
    public void replaceChild(DockingPanel child, DockingPanel newChild) {
        if (panel == child) {
            setPanel(newChild);
        }
    }

    @Override
    public void removeChild(DockingPanel child) {
        if (child == panel) {
            if (removeExistingPanel()) {
                createContents();
            }
        }
    }

    /**
     * Remove a dockable from its toolbar and pin it back into the root
     *
     * @param dockable Dockable to pin
     */
    public void setDockableShown(Dockable dockable) {
        // if the dockable is currently unpinned, remove it from the toolbar, then adjust the toolbars
        if (rootPanel.isLocationSupported(ToolbarLocation.WEST) && westToolbar.hasDockable(dockable)) {
            westToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.WEST, 0.25f);
        }
        else if (rootPanel.isLocationSupported(ToolbarLocation.EAST) && eastToolbar.hasDockable(dockable)) {
            eastToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.EAST, 0.25f);
        }
        else if (rootPanel.isLocationSupported(ToolbarLocation.SOUTH) && southToolbar.hasDockable(dockable)) {
            southToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.SOUTH, 0.25f);
        }

        createContents();
    }

    /**
     * set a dockable to be unpinned at the given location
     *
     * @param dockable Dockable to unpin
     * @param location Toolbar to unpin to
     */
    public void setDockableHidden(Dockable dockable, ToolbarLocation location) {
        if (!rootPanel.isAutoHideSupported()) {
            return;
        }

        switch (location) {
            case WEST: {
                if (rootPanel.isLocationSupported(ToolbarLocation.WEST)) {
                    westToolbar.addDockable(dockable);
                }
                break;
            }
            case SOUTH: {
                if (rootPanel.isLocationSupported(ToolbarLocation.SOUTH)) {
                    southToolbar.addDockable(dockable);
                }
                break;
            }
            case EAST: {
                if (rootPanel.isLocationSupported(ToolbarLocation.EAST)) {
                    eastToolbar.addDockable(dockable);
                }
                break;
            }
        }

        createContents();
    }

    /**
     * Get a list of the unpinned dockables on the specified toolbar
     *
     * @param location Toolbar location
     * @return List of unpinned dockables
     */
    public java.util.List<String> hiddenPersistentIDs(ToolbarLocation location) {
        switch (location) {
            case WEST: return westToolbar.getPersistentIDs();
            case EAST: return eastToolbar.getPersistentIDs();
            case SOUTH: return southToolbar.getPersistentIDs();
        }
        return Collections.emptyList();
    }

    private void createContents() {
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;

        if (rootPanel.isAutoHideSupported() && westToolbar.shouldDisplay()) {
            add(westToolbar, gbc);
            gbc.gridx++;
        }

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        if (panel == null) {
            add(rootPanel.getEmptyPanel(), gbc);
        }
        else {
            add(panel, gbc);
        }
        gbc.gridx++;

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;

        if (rootPanel.isAutoHideSupported() && eastToolbar.shouldDisplay()) {
            add(eastToolbar, gbc);
        }

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (rootPanel.isAutoHideSupported() && southToolbar.shouldDisplay()) {
            add(southToolbar, gbc);
        }

        revalidate();
        repaint();
    }

    /**
     * Hide all unpinned panels on the west, south and east toolbars
     */
    public void hideHiddenPanels() {
        if (westToolbar != null) {
            westToolbar.hideAll();
        }
        if (southToolbar != null) {
            southToolbar.hideAll();
        }
        if (eastToolbar != null) {
            eastToolbar.hideAll();
        }
    }

    /**
     * Get a list of IDs for unpinned dockables on the west toolbar
     *
     * @return Persistent IDs
     */
    public java.util.List<String> getWestAutoHideToolbarIDs() {
        if (westToolbar == null) {
            return Collections.emptyList();
        }
        return westToolbar.getPersistentIDs();
    }

    /**
     * Get a list of IDs for unpinned dockables on the east toolbar
     *
     * @return Persistent IDs
     */
    public java.util.List<String> getEastAutoHideToolbarIDs() {
        if (eastToolbar == null) {
            return Collections.emptyList();
        }
        return eastToolbar.getPersistentIDs();
    }

    /**
     * Get a list of IDs for unpinned dockables on the south toolbar
     *
     * @return Persistent IDs
     */
    public List<String> getSouthAutoHideToolbarIDs() {
        if (southToolbar == null) {
            return Collections.emptyList();
        }
        return southToolbar.getPersistentIDs();
    }

    public void updateLAF() {
        if (southToolbar != null) {
            SwingUtilities.updateComponentTreeUI(southToolbar);
        }
        if (westToolbar != null) {
            SwingUtilities.updateComponentTreeUI(westToolbar);
        }
        if (eastToolbar != null) {
            SwingUtilities.updateComponentTreeUI(eastToolbar);
        }
        if (rootPanel.getEmptyPanel() != null) {
            SwingUtilities.updateComponentTreeUI(rootPanel.getEmptyPanel());
        }
    }
}
