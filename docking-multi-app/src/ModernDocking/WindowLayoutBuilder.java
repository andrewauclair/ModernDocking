package ModernDocking;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.WindowLayoutBuilderAPI;

public class WindowLayoutBuilder extends WindowLayoutBuilderAPI {
    public WindowLayoutBuilder(DockingAPI docking, String firstID) {
        super(docking, firstID);
    }
}
