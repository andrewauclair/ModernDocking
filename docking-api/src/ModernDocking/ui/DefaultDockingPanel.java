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
package ModernDocking.ui;

import ModernDocking.Dockable;
import ModernDocking.DockableStyle;
import ModernDocking.event.DockingEvent;
import ModernDocking.event.DockingListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the Dockable interface. Useful for GUI builders where you can set each property.
 */
public class DefaultDockingPanel extends JPanel implements Dockable, DockingListener {
    private String persistentID;
    private int type;
    private String tabText = "";
    private Icon icon;
    private boolean floatingAllowed;
    private boolean limitToRoot;
    private DockableStyle style;
    private boolean canBeClosed;
    private boolean allowAutoHide;
    private boolean allowMinMax;
    private List<JMenu> moreOptions = new ArrayList<>();

    private final List<DockingListener> listeners = new ArrayList<>();

    public DefaultDockingPanel() {
    }

    public DefaultDockingPanel(String persistentID, String text) {
        this.persistentID = persistentID;
        this.tabText = text;
    }

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
    public boolean isLimitedToRoot() {
        return limitToRoot;
    }

    /**
     * Set limit to root flag
     *
     * @param limitToRoot New flag value
     */
    public void setLimitedToRoot(boolean limitToRoot) {
        this.limitToRoot = limitToRoot;
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
    public boolean getHasMoreOptions() {
        return moreOptions.size() > 0;
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

    public void addDockingListener(DockingListener listener) {
        listeners.add(listener);
    }

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
