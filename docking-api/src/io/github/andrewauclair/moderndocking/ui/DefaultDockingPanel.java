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
package io.github.andrewauclair.moderndocking.ui;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Default implementation of the Dockable interface. Useful for GUI builders where you can set each property.
 */
public class DefaultDockingPanel extends JPanel implements Dockable, DockingListener {
    /**
     * Persistent ID for the dockable in this panel
     */
    private String persistentID;
    /**
     * The type for this dockable
     */
    private int type;
    /**
     * Tab text to display on tabbed panes
     */
    private String tabText = "";
    /**
     * Icon to use on tabbed panes, auto-hide toolbars, and windows
     */
    private Icon icon;
    /**
     * Is this dockable allowed to float?
     */
    private boolean floatingAllowed;
    /**
     * Is this dockable limited to its starting window?
     */
    private boolean limitedToWindow;
    /**
     * What positions is this dockable allowed to be in?
     */
    private DockableStyle style;
    /**
     * Can the user close this dockable?
     */
    private boolean canBeClosed;
    /**
     * Can this dockable be displayed on an auto-hide toolbar?
     */
    private boolean allowAutoHide;
    /**
     * Is min/max an option for this dockable?
     */
    private boolean allowMinMax;
    /**
     * Extra menu options to display on context menu
     */
    private List<JMenu> moreOptions = new ArrayList<>();

    /**
     * Listeners listening to docking changes for this dockable
     */
    private final List<DockingListener> listeners = new ArrayList<>();

    /**
     * Create a new instance
     */
    public DefaultDockingPanel() {
    }

    /**
     * Create a new instance with the specific persistent ID and text
     * @param persistentID The persistent ID of the dockable
     * @param text The text to display on title bar and tabs
     */
    public DefaultDockingPanel(String persistentID, String text) {
        this.persistentID = persistentID;
        this.tabText = text;
    }

    /**
     * Retrieve the persistent ID of the dockable
     *
     * @return Persistent ID
     */
    @Override
    public String getPersistentID() {
        return persistentID;
    }

    /**
     * Set the persistent ID of the dockable
     *
     * @param persistentID New persistent ID
     */
    public void setPersistentID(String persistentID) {
        this.persistentID = persistentID;
    }

    @Override
    public int getType() {
        return type;
    }

    /**
     * Set the type of the dockable
     *
     * @param type New type
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getTabText() {
        return tabText;
    }

    /**
     * Set the tab text of the dockable
     *
     * @param tabText New tab text
     */
    public void setTabText(String tabText) {
        this.tabText = tabText;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    /**
     * Set the icon of this dockable
     *
     * @param icon The dockables icon to display on tabs and toolbars
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public boolean isFloatingAllowed() {
        return floatingAllowed;
    }

    /**
     * Set floating allowed flag
     *
     * @param isFloatingAllowed New flag value
     */
    public void setFloatingAllowed(boolean isFloatingAllowed) {
        this.floatingAllowed = isFloatingAllowed;
    }

    @Override
    public boolean isLimitedToWindow() {
        return limitedToWindow;
    }

    /**
     * Set the limited to window flag
     *
     * @param limitToWindow New flag value
     */
    public void setLimitedToWindow(boolean limitToWindow) {
        this.limitedToWindow = limitToWindow;
    }

    @Override
    public DockableStyle getStyle() {
        return style;
    }

    /**
     * Set the style of the dockable
     *
     * @param style New style
     */
    public void setStyle(DockableStyle style) {
        this.style = style;
    }

    @Override
    public boolean isClosable() {
        return canBeClosed;
    }

    /**
     * Set the close button support for this dockable
     *
     * @param canBeClosed Can this dockable be closed?
     */
    public void setClosable(boolean canBeClosed) {
        this.canBeClosed = canBeClosed;
    }

    @Override
    public boolean isAutoHideAllowed() {
        return allowAutoHide;
    }

    /**
     * Set the auto hide support on this dockable
     *
     * @param allowAutoHide Is auto hide allowed
     */
    public void setAutoHideAllowed(boolean allowAutoHide) {
        this.allowAutoHide = allowAutoHide;
    }

    @Override
    public boolean isMinMaxAllowed() {
        return allowMinMax;
    }

    /**
     * Set the min/max support on this dockable
     *
     * @param allowMinMax Is min/max supported
     */
    public void setMinMaxAllowed(boolean allowMinMax) {
        this.allowMinMax = allowMinMax;
    }

    @Override
    public boolean hasMoreMenuOptions() {
        return !moreOptions.isEmpty();
    }

    /**
     * Set the additional options to be displayed on the menu
     *
     * @param options Additional menu options
     */
    public void setMoreOptions(List<JMenu> options) {
        moreOptions = options;
    }

    @Override
    public void addMoreOptions(JPopupMenu menu) {
        for (JMenu option : moreOptions) {
            menu.add(option);
        }
    }

    /**
     * Add a new docking listener
     *
     * @param listener Listener to add
     */
    public void addDockingListener(DockingListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a docking listener
     *
     * @param listener Listener to remove
     */
    public void removeDockingListener(DockingListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void dockingChange(DockingEvent e) {
        if (e.getDockable() == this) {
            listeners.forEach(listener -> listener.dockingChange(e));
        }
    }
}
