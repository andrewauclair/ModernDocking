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

import io.github.andrewauclair.moderndocking.api.LayoutPersistenceAPI;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import java.io.File;

/**
 * Persist and restore Application and Window layouts to/from files
 */
public class LayoutPersistence {
    private static final LayoutPersistenceAPI instance = new LayoutPersistenceAPI(Docking.getSingleInstance()){};

    private LayoutPersistence() {
    }

    /**
     * saves a docking layout to the given file
     *
     * @param file File to save the docking layout into
     * @param layout The layout to save
     * @throws DockingLayoutException Thrown if we failed to save the layout to the file
     */
    public static void saveLayoutToFile(File file, ApplicationLayout layout) throws DockingLayoutException {
        instance.saveLayoutToFile(file, layout);
    }

    /**
     * Load an ApplicationLayout from the specified file
     *
     * @param file File to load the ApplicationLayout from
     * @return ApplicationLayout loaded from the file
     * @throws DockingLayoutException Thrown if we failed to read from the file or something went wrong with loading the layout
     */
    public static ApplicationLayout loadApplicationLayoutFromFile(File file) throws DockingLayoutException {
        return instance.loadApplicationLayoutFromFile(file);
    }

    public static boolean saveWindowLayoutToFile(File file, WindowLayout layout) {
        return instance.saveWindowLayoutToFile(file, layout);
    }

    /**
     * Load a WindowLayout from an XML file
     *
     * @param file File to load WindowLayout from
     * @return The loaded WindowLayout
     */
    public static WindowLayout loadWindowLayoutFromFile(File file) {
        return instance.loadWindowLayoutFromFile(file);
    }
}
