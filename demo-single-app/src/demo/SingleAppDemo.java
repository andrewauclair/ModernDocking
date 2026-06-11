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

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.DockableMenuItem;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import javax.swing.JMenuItem;
import java.io.File;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import picocli.CommandLine;

@CommandLine.Command
public class SingleAppDemo implements Callable<Integer> {
    @CommandLine.Mixin
    private DemoOptions options;

    @Override
    public Integer call() {
        options.apply();

        CommonDemoFrame frame = new CommonDemoFrame(
                "Modern Docking — Single App Demo",
                new File("single_app_demo_layout.xml")) {
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

            @Override
            protected JMenuItem createViewMenuItem(Dockable dockable) {
                return new DockableMenuItem(dockable);
            }
        };

        frame.setVisible(true);
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CommandLine(new SingleAppDemo()).execute(args));
    }
}
