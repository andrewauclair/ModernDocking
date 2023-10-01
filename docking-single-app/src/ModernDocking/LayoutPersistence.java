package ModernDocking;

import ModernDocking.api.LayoutPersistenceAPI;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.WindowLayout;

import java.io.File;

public class LayoutPersistence {
    private static final LayoutPersistenceAPI instance = new LayoutPersistenceAPI(Docking.getSingleInstance()){};

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
