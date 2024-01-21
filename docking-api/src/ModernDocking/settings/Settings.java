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
package ModernDocking.settings;

import ModernDocking.Dockable;

import javax.swing.*;

public class Settings {
    private static boolean alwaysDisplayTabsMode = false;

    private static int tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT;

    private static boolean enableActiveHighlighter = true;

    public static boolean alwaysDisplayTabsMode() {
        return alwaysDisplayTabsMode;
    }

    public static boolean alwaysDisplayTabsMode(Dockable dockable) {
        return alwaysDisplayTabsMode || dockable.getTabPosition() == SwingConstants.TOP;
    }

    public static void setAlwaysDisplayTabMode(boolean alwaysDisplayTabsMode) {
        Settings.alwaysDisplayTabsMode = alwaysDisplayTabsMode;
    }

    public static int getTabLayoutPolicy() {
        return tabLayoutPolicy;
    }

    public static void setTabLayoutPolicy(int tabLayoutPolicy) {
        if (tabLayoutPolicy != JTabbedPane.WRAP_TAB_LAYOUT && tabLayoutPolicy != JTabbedPane.SCROLL_TAB_LAYOUT) {
            throw new IllegalArgumentException("illegal tab layout policy: must be WRAP_TAB_LAYOUT or SCROLL_TAB_LAYOUT");
        }
        Settings.tabLayoutPolicy = tabLayoutPolicy;
    }

    public static boolean isActiveHighlighterEnabled() {
        return enableActiveHighlighter;
    }

    public static void setActiveHighlighterEnabled(boolean enabled) {
        enableActiveHighlighter = enabled;
    }
}
