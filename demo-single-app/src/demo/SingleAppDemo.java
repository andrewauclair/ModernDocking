package demo;

import com.formdev.flatlaf.FlatDarkLaf;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import java.io.File;
import javax.swing.SwingUtilities;

public class SingleAppDemo extends CommonDemoFrame {

    public SingleAppDemo() {
        super("Modern Docking \u2014 Comprehensive Demo",
                new File("comprehensive_demo_layout.xml")
        );
    }

    @Override
    protected DockingAPI createDocking() {
        Docking.initialize(this);
        return Docking.getSingleInstance();
    }

    @Override
    protected RootDockingPanelAPI createRoot(DockingAPI docking) {
        return new RootDockingPanel(this);
    }

    @Override
    protected WindowLayoutBuilderAPI createLayoutBuilder(DockingAPI docking, String firstId) {
        return new WindowLayoutBuilder(firstId);
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new SingleAppDemo().setVisible(true));
    }
}
