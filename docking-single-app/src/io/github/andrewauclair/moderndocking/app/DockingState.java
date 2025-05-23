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
package io.github.andrewauclair.moderndocking.app;

import io.github.andrewauclair.moderndocking.api.DockingStateAPI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import java.awt.Window;

public class DockingState {
    private static final DockingStateAPI instance = new DockingStateAPI(Docking.getSingleInstance()){};

    /**
     * This class should not be instantiated
     */
    private DockingState() {
    }

    /**
     * Get the current window layout of a window
     *
     * @param window The window to get a layout for
     *
     * @return The window layout
     */
    public static WindowLayout getWindowLayout(Window window) {
        return instance.getWindowLayout(window);
    }

    /**
     * Get the current application layout of the application
     *
     * @return Layout of the application
     */
    public static ApplicationLayout getApplicationLayout() {
        return instance.getApplicationLayout();
    }

    /**
     * Restore the application layout, creating any necessary windows
     *
     * @param layout Application layout to restore
     */
    public static void restoreApplicationLayout(ApplicationLayout layout) {
        instance.restoreApplicationLayout(layout);
    }

    /**
     * Restore the layout of a single window
     *
     * @param window Window to restore the layout onto
     * @param layout The layout to restore
     */
    public static void restoreWindowLayout(Window window, WindowLayout layout) {
        instance.restoreWindowLayout(window, layout);
    }

    /**
     * Restore the layout of a single window, preserving the current size and position of the window
     *
     * @param window Window to restore the layout onto
     * @param layout The layout to restore
     */
    public static void restoreWindowLayout_PreserveSizeAndPos(Window window, WindowLayout layout) {
        instance.restoreWindowLayout_PreserveSizeAndPos(window, layout);
    }
}
