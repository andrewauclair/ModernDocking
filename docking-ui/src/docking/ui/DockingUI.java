package docking.ui;

import ModernDocking.internal.DockingInternal;

public class DockingUI {
    public static void initialize() {
        DockingInternal.createHeaderUI = FlatLafHeaderUI::new;
    }
}
