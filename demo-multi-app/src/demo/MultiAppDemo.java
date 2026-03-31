package demo;

import com.formdev.flatlaf.FlatDarkLaf;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import java.io.File;
import javax.swing.SwingUtilities;

public class MultiAppDemo {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            DockingUI.initialize();

            CommonDemoFrame frame1 = new AppDemoFrame(
                    "Modern Docking \u2014 Comprehensive Demo (Frame 1)",
                    new File("comprehensive_demo_layout_1.xml"),
                    100, 100);

            CommonDemoFrame frame2 = new AppDemoFrame(
                    "Modern Docking \u2014 Comprehensive Demo (Frame 2)",
                    new File("comprehensive_demo_layout_2.xml"),
                    800, 100);

            frame1.setVisible(true);
            frame2.setVisible(true);
        });
    }

    private static class AppDemoFrame extends CommonDemoFrame {

        AppDemoFrame(String title, File persistFile, int defaultX, int defaultY) {
            super(title, persistFile);
        }

        @Override
        protected DockingAPI createDocking() {
            return new Docking(this);
        }

        @Override
        protected RootDockingPanelAPI createRoot(DockingAPI docking) {
            return new RootDockingPanel(docking, this);
        }

        @Override
        protected WindowLayoutBuilderAPI createLayoutBuilder(DockingAPI docking, String firstId) {
            return new WindowLayoutBuilder(docking, firstId);
        }
    }
}
