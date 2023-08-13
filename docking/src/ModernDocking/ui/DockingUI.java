package ModernDocking.ui;

import javax.swing.*;
import java.awt.*;

public class DockingUI {
    /*
     * colors:
     *      handle background
     *      handle foreground
     *      overlay background
     *      active highlighter
     *      header background
     *      header foreground
     *
     *  all of these colors should have default names that they pull from the look and feel when using flatlaf. defaults they use for system
     *  look and feels. options to change the colors that it pulls from in flatlaf. Ability to change the default colors, the names used to
     * pull from FlatLaf and a separate set of ModernDocking specific properties that can be added to look and feel properties.
     *
     * other:
     *      borders on titles
     *      shape of docking handles
     *          square corners vs rounded corners
     *      size of docking handles
     *
     */
    private static final String handleBackground = "ModernDocking.handleBackground";
    private static final String handleForeground = "ModernDocking.handleForeground";
    private static final String overlayBackground = "ModernDocking.overlayBackground";
    private static final String highlighterColor = "ModernDocking.highlighterColor";
    private static final String headerBackground = "ModernDocking.headerBackground";
    private static final String headerForeground = "ModernDocking.headerForeground";

    private static final String themeHandleBackground = "TableHeader.background";
    private static final String themeHandleForeground = "TableHeader.foreground";
    private static final String themeHighlighterColor = "TableHeader.foreground";
    private static final String themeHeaderBackground = "TableHeader.background";
    private static final String themeHeaderForeground = "TableHeader.foreground";

    private static String currentHandleBackground = themeHandleBackground;
    private static String currentHandleForeground = themeHandleForeground;

    public static void setHandleBackgroundProperty(String property) {
        currentHandleBackground = property;
    }

    public static Color getHandleBackground() {
        if (UIManager.get(handleBackground) != null) {
            return UIManager.getColor(handleBackground);
        }
        return UIManager.getColor(currentHandleBackground);
    }

    public static void setHandleForegroundProperty(String property) {
        currentHandleForeground = property;
    }
    public static Color getHandleForeground() {
        if (UIManager.get(handleForeground) != null) {
            return UIManager.getColor(handleForeground);
        }
        return UIManager.getColor(currentHandleForeground);
    }
}
