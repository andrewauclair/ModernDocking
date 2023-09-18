package ModernDocking;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.DockingStateAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DockingComponentUtils;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.WindowLayout;
import ModernDocking.persist.RootDockState;

import java.awt.*;

public class DockingState {
    private static final DockingStateAPI instance = new DockingStateAPI(Docking.getSingleInstance()){};

    public static RootDockState getRootState(Window window) {
        return instance.getRootState(window);
    }

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

    public static void restoreWindowLayout_PreserveSizeAndPos(Window window, WindowLayout layout) {
        instance.restoreWindowLayout_PreserveSizeAndPos(window, layout);
    }

    public static void restoreState(Window window, RootDockState state) {
        instance.restoreState(window, state);
    }
}
