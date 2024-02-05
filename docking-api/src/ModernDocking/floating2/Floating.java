package ModernDocking.floating2;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.floating.DockingUtilsFrame;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Floating {
    private static final Map<Window, FloatUtilsFrame> utilFrames = new HashMap<>();
    private static boolean isFloating = false;

    public static void registerDockingWindow(DockingAPI docking, Window window, RootDockingPanelAPI root) {
        utilFrames.put(window, new FloatUtilsFrame(docking, window, root));
    }

    public static void deregisterDockingWindow(Window window) {
        utilFrames.remove(window);
    }

    public static FloatUtilsFrame frameForWindow(Window window) {
        return utilFrames.get(window);
    }

    public static boolean isFloating() { return isFloating; }

    static void setFloating(boolean floating) {
        isFloating = floating;
    }
}
