/*
Copyright (c) 2026 Andrew Auclair

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
package demo;

import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import java.io.File;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import picocli.CommandLine;

@CommandLine.Command
public class MultiAppDemo implements Callable<Integer> {
    @CommandLine.Mixin
    private DemoOptions options;

    @Override
    public Integer call() {
        options.apply();

        CommonDemoFrame frame1 = new AppDemoFrame(
                "Modern Docking \u2014 Multi-App Demo (Frame 1)",
                new File("multi_app_demo_layout_1.xml"));

        CommonDemoFrame frame2 = new AppDemoFrame(
                "Modern Docking \u2014 Multi-App Demo (Frame 2)",
                new File("multi_app_demo_layout_2.xml"));

        frame1.setVisible(true);
        frame2.setVisible(true);
        return 0;
    }

    private static class AppDemoFrame extends CommonDemoFrame {
        AppDemoFrame(String title, File persistFile) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CommandLine(new MultiAppDemo()).execute(args));
    }
}
