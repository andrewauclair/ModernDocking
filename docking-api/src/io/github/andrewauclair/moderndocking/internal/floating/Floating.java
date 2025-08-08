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
package io.github.andrewauclair.moderndocking.internal.floating;

import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Small utility class for floating feature
 */
public class Floating {
    private static final Map<Window, FloatUtilsFrame> utilFrames = new HashMap<>();
    private static boolean isFloating = false;
    private static boolean isFloatingTabbedPane = false;

    /**
     * Unused. All methods are static
     */
    private Floating() {
    }

    /**
     * Register a new docking window and create a utility frame for it
     *
     * @param docking The docking instance this window is for
     * @param window The window being registered
     * @param root The internal root in the window
     */
    public static void registerDockingWindow(DockingAPI docking, Window window, InternalRootDockingPanel root) {
        SwingUtilities.invokeLater(() -> utilFrames.put(window, new FloatUtilsFrame(docking, window, root)));
    }

    /**
     * Deregister a window. Used when a window is closed
     *
     * @param window The window to deregister
     */
    public static void deregisterDockingWindow(Window window) {
        utilFrames.remove(window);
    }

    /**
     * Lookup the utility frame for a window
     *
     * @param window The window to search for
     *
     * @return Utility frame or null
     */
    public static FloatUtilsFrame frameForWindow(Window window) {
        return utilFrames.get(window);
    }

    /**
     * Check if we have a dockable actively floating
     *
     * @return Are we floating a dockable?
     */
    public static boolean isFloating() { return isFloating; }

    /**
     * Set the floating state
     *
     * @param floating New floating state
     */
    public static void setFloating(boolean floating) {
        isFloating = floating;
    }

    /**
     * Check if we're floating a tabbed pane instead of a simple panel
     *
     * @return Are we floating a tabbed pane?
     */
    public static boolean isFloatingTabbedPane() { return isFloatingTabbedPane; }

    /**
     * Set the floating tabbed pane flag
     *
     * @param floating New floating tabbed pane state
     */
    public static void setFloatingTabbedPane(boolean floating) {
        isFloatingTabbedPane = floating;
    }
}