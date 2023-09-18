package ModernDocking;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.WindowLayoutBuilderAPI;

public class WindowLayoutBuilder extends WindowLayoutBuilderAPI {
    public WindowLayoutBuilder(String firstID) {
        super(Docking.getSingleInstance(), firstID);
    }
}
