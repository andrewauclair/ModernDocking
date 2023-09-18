package ModernDocking.settings;

import javax.swing.*;

public class Settings {
    private static boolean alwaysDisplayTabsMode = false;

    private static int tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT;

    private static boolean experimentalProperties = false;

    public static boolean alwaysDisplayTabsMode() {
        return alwaysDisplayTabsMode;
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

    public static boolean getExperimentalPropertyMode() {
        return experimentalProperties;
    }

    public static void setExperimentalPropertyMode(boolean enabled) {
        experimentalProperties = enabled;
    }
}
