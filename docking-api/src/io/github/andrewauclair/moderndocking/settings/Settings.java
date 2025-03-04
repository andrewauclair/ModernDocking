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
package io.github.andrewauclair.moderndocking.settings;

import io.github.andrewauclair.moderndocking.DockableTabPreference;
import javax.swing.JTabbedPane;

/**
 * General settings for Modern Docking
 */
public class Settings {
    private static DockableTabPreference defaultTabPreference = DockableTabPreference.BOTTOM;

    private static int tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT;

    private static boolean enableActiveHighlighter = true;

    /**
     * Unused. All methods are static
     */
    private Settings() {
    }

    /**
     * Checks if we're forcing tabs to always be on top or bottom
     *
     * @return Are we using a default tab preference of TOP_ALWAYS or BOTTOM_ALWAYS?
     */
    public static boolean alwaysDisplayTabsMode() {
        return defaultTabPreference == DockableTabPreference.TOP_ALWAYS || defaultTabPreference == DockableTabPreference.BOTTOM_ALWAYS;
    }

    /**
     * Get the current default tab preferenced used by Modern Docking in JTabbedPanes
     *
     * @return Default tab preference
     */
    public static DockableTabPreference defaultTabPreference() {
        return defaultTabPreference;
    }

    /**
     * Set the applications preference for default tab location when adding dockables to tab groups.
     *
     * @param tabPreference The new default tab location preference
     */
    public static void setDefaultTabPreference(DockableTabPreference tabPreference) {
        defaultTabPreference = tabPreference;
    }

    /**
     * Get the current tab layout policy used by Modern Docking for JTabbedPanes
     *
     * @return Current tab layout policy
     */
    public static int getTabLayoutPolicy() {
        return tabLayoutPolicy;
    }

    /**
     * Set the tab layout policy Modern Docking should use for JTabbedPanes
     *
     * @param tabLayoutPolicy New tab layout policy
     */
    public static void setTabLayoutPolicy(int tabLayoutPolicy) {
        if (tabLayoutPolicy != JTabbedPane.WRAP_TAB_LAYOUT && tabLayoutPolicy != JTabbedPane.SCROLL_TAB_LAYOUT) {
            throw new IllegalArgumentException("illegal tab layout policy: must be WRAP_TAB_LAYOUT or SCROLL_TAB_LAYOUT");
        }
        Settings.tabLayoutPolicy = tabLayoutPolicy;
    }

    /**
     * Check if the active dockable highlighter is enabled
     *
     * @return Is the ActiveDockableHighlighter enabled?
     */
    public static boolean isActiveHighlighterEnabled() {
        return enableActiveHighlighter;
    }

    /**
     * Enable or disable the ActiveDockableHighlighter
     *
     * @param enabled New flag state
     */
    public static void setActiveHighlighterEnabled(boolean enabled) {
        enableActiveHighlighter = enabled;
    }
}
