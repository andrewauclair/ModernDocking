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

import java.awt.Color;
import javax.swing.UIManager;

/**
 * Settings for theme related configuration items
 */
public class DockingSettings {
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
    private static final String highlighterSelectedBorder = "ModernDocking.highlighterSelectedBorder";
    private static final String highlighterNotSelectedBorder = "ModernDocking.highlighterNotSelectedBorder";
    private static final String headerBackground = "ModernDocking.headerBackground";
    private static final String headerForeground = "ModernDocking.headerForeground";

    private static final String themeHandleBackground = "TableHeader.background";
    private static final String themeHandleForeground = "TableHeader.foreground";
    private static final String themeHighlighterSelectedBorder = "Component.focusColor";
    private static final String themeHighlighterNotSelectedBorder = "Component.borderColor";
    private static final String themeHeaderBackground = "TableHeader.background";
    private static final String themeHeaderForeground = "TableHeader.foreground";

    private static final Color defaultHandleBackground = Color.white;
    private static final Color defaultHandleForeground = Color.black;
    private static final Color defaultHighlightColor = Color.BLUE;
    private static final Color overlayBackgroundOpaque = new Color(0x42c0ff);
    private static final Color defaultOverlayBackground = new Color(overlayBackgroundOpaque.getRed() / 255f, overlayBackgroundOpaque.getGreen() / 255f, overlayBackgroundOpaque.getBlue() / 255f, 85 / 255f);
    private static final Color defaultHeaderBackground = Color.white;
    private static final Color defaultHeaderForeground = Color.black;

    private static String currentHandleBackground = themeHandleBackground;
    private static String currentHandleForeground = themeHandleForeground;
    private static String currentHighlightSelectedBorder = themeHighlighterSelectedBorder;
    private static String currentHighlightNotSelectedBorder = themeHighlighterNotSelectedBorder;
    private static String currentOverlayBackground = overlayBackground;
    private static String currentHeaderBackground = themeHeaderBackground;
    private static String currentHeaderForeground = themeHeaderForeground;

    /**
     * Unused. All methods are static
     */
    private DockingSettings() {
    }

    /**
     * Set a new property to use for the background color on Docking Handles
     *
     * @param property The new background property name
     */
    public static void setHandleBackgroundProperty(String property) {
        currentHandleBackground = property;
    }

    /**
     * Get the current handle background color
     *
     * @return Handle background color
     */
    public static Color getHandleBackground() {
        if (UIManager.get(handleBackground) != null) {
            return UIManager.getColor(handleBackground);
        }
        if (UIManager.get(currentHandleBackground) != null) {
            return UIManager.getColor(currentHandleBackground);
        }
        // TODO I wonder if we should attempt the theme property again here, assuming the user has changd the current property
        return defaultHandleBackground;
    }

    /**
     * Set a new property to use for the foreground color on Docking Handles
     *
     * @param property The new foreground property name
     */
    public static void setHandleForegroundProperty(String property) {
        currentHandleForeground = property;
    }

    /**
     * Get the current handle foreground color
     *
     * @return Handle foreground color
     */
    public static Color getHandleForeground() {
        if (UIManager.get(handleForeground) != null) {
            return UIManager.getColor(handleForeground);
        }
        if (UIManager.get(currentHandleForeground) != null) {
            return UIManager.getColor(currentHandleForeground);
        }
        return defaultHandleForeground;
    }

    /**
     * Set a new property to use for the background color of the floating overlay display
     *
     * @param property The new background color property
     */
    public static void setOverlayBackgroundProperty(String property) {
        currentOverlayBackground = property;
    }

    /**
     * Get the overlay background color
     *
     * @return Overlay background color
     */
    public static Color getOverlayBackground() {
        if (UIManager.get(currentOverlayBackground) != null) {
            return UIManager.getColor(currentOverlayBackground);
        }
        if (UIManager.get(overlayBackground) != null) {
            return UIManager.getColor(overlayBackground);
        }
        return defaultOverlayBackground;
    }

    /**
     * Set a new property to use for the border color used by the active dockable highlighter
     *
     * @param property The new selected border color property
     */
    public static void setHighlighterSelectedBorderProperty(String property) {
        currentHighlightSelectedBorder = property;
    }

    /**
     * Get the selected border color for the active dockable highlighter
     *
     * @return Selected border color
     */
    public static Color getHighlighterSelectedBorder() {
        if (UIManager.get(currentHighlightSelectedBorder) != null) {
            return UIManager.getColor(currentHighlightSelectedBorder);
        }
        if (UIManager.get(highlighterSelectedBorder) != null) {
            return UIManager.getColor(highlighterSelectedBorder);
        }
        return defaultHighlightColor;
    }

    /**
     * Define a color property to use when a panel is not highlighted by the active dockable highlighter
     *
     * @param property UIManager property name to use for not selected border color
     */
    public static void setHighlighterNotSelectedBorderProperty(String property) {
        currentHighlightNotSelectedBorder = property;
    }

    /**
     * Get the not selected border color for the active dockable highlighter
     *
     * @return Highlighter not selected border color
     */
    public static Color getHighlighterNotSelectedBorder() {
        if (UIManager.get(currentHighlightNotSelectedBorder) != null) {
            return UIManager.getColor(currentHighlightNotSelectedBorder);
        }
        if (UIManager.get(highlighterNotSelectedBorder) != null) {
            return UIManager.getColor(highlighterNotSelectedBorder);
        }
        return defaultHighlightColor;
    }

    /**
     * Set a new property to use for the default background color of docking headers
     *
     * @param property The new background color property
     */
    public static void setHeaderBackgroundProperty(String property) {
        currentHeaderBackground = property;
    }

    /**
     * Get the header background color
     *
     * @return Current header background color
     */
    public static Color getHeaderBackground() {
        if (UIManager.get(currentHeaderBackground) != null) {
            return UIManager.getColor(currentHeaderBackground);
        }
        if (UIManager.get(headerBackground) != null) {
            return UIManager.getColor(headerBackground);
        }
        return defaultHeaderBackground;
    }

    /**
     * Set a new property to use for the default foreground color of docking headers
     *
     * @param property The new foreground color property
     */
    public static void setHeaderForegroundProperty(String property) {
        currentHeaderForeground = property;
    }

    /**
     * Get the header foreground color
     *
     * @return Current header foreground color
     */
    public static Color getHeaderForeground() {
        if (UIManager.get(currentHeaderForeground) != null) {
            return UIManager.getColor(currentHeaderForeground);
        }
        if (UIManager.get(headerForeground) != null) {
            return UIManager.getColor(headerForeground);
        }
        return defaultHeaderForeground;
    }
}
