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

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.AppStateAPI;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import java.io.File;

/**
 * Handle persistence and restoration of Application layouts
 */
public class AppState {
    private static final AppStateAPI instance = new AppStateAPI(Docking.getSingleInstance()){};

    /**
     * This class should not be instantiated
     */
    private AppState() {
    }

    /**
     * Set whether the framework should auto persist the application layout to a file when
     * docking changes, windows resize, etc.
     *
     * @param autoPersist Should the framework auto persist the application layout to a file?
     */
    public static void setAutoPersist(boolean autoPersist) {
        instance.setAutoPersist(autoPersist);
    }

    /**
     * Are we currently auto persisting to a file?
     *
     * @return True - we are auto persisting, False - we are not auto persisting
     */
    public static boolean isAutoPersist() {
        return instance.isAutoPersist();
    }

    /**
     * Set the file that should be used for auto persistence. This will be written as an XML file.
     *
     * @param file File to persist layout to
     */
    public static void setPersistFile(File file) {
        instance.setPersistFile(file);
    }

    /**
     * Retrieve the file that we are persisting the application layout into
     *
     * @return The file we are currently persisting to
     */
    public static File getPersistFile() {
        return instance.getPersistFile();
    }

    /**
     * Sets the pause state of the auto persistence
     *
     * @param paused Whether auto persistence should be enabled
     */
    public static void setPaused(boolean paused) {
        instance.setPaused(paused);
    }

    /**
     * Gets the pause state of the auto persistence
     *
     * @return Whether auto persistence is enabled
     */
    public static boolean isPaused() {
        return instance.isPaused();
    }

    /**
     * Used to persist the current app layout to the layout file.
     * This is a no-op if auto persistence is turned off, it's paused or there is no file
     */
    public static void persist() {
        instance.persist();
    }

    /**
     * Restore the application layout from the auto persist file.
     *
     * @return true if and only if a layout is restored from a file. Restoring from the default layout will return false.
     * @throws DockingLayoutException Thrown for any issues with the layout file.
     */
    public static boolean restore() throws DockingLayoutException {
        return instance.restore();
    }

    /**
     * Set the default layout used by the application. This layout is restored after the application has loaded
     * and there is no persisted layout or the persisted layout fails to load.
     *
     * @param layout Default layout
     */
    public static void setDefaultApplicationLayout(ApplicationLayout layout) {
        instance.setDefaultApplicationLayout(layout);
    }

    /**
     * Get the property for a dockable by name
     *
     * @param dockable The dockable to get a property for
     * @param propertyName The property to search for
     *
     * @return The property instance of the dockable, or null if not found
     */
    public static Property getProperty(Dockable dockable, String propertyName) {
        return instance.getProperty(dockable, propertyName);
    }

    /**
     * Set the value of a property on the dockable. If the property does not exist, it will be created
     *
     * @param dockable The dockable to set a property for
     * @param propertyName The name of the property we're setting
     * @param value The value of the property
     */
    public static void setProperty(Dockable dockable, String propertyName, String value) {
        instance.setProperty(dockable, propertyName, new Property.StringProperty(propertyName, value));
    }

    /**
     * Remove the property from the dockable
     *
     * @param dockable The dockable to remove the property from
     * @param propertyName The property to remove
     */
    public static void removeProperty(Dockable dockable, String propertyName) {
        instance.removeProperty(dockable, propertyName);
    }
}
