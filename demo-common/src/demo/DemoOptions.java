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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubDarkIJTheme;
import exception.FailOnThreadViolationRepaintManager;
import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import picocli.CommandLine;

public class DemoOptions {

    @CommandLine.Option(names = "--laf", defaultValue = "dark", description = "look and feel to use. one of: system, light, dark, github-dark or solarized-dark")
    String lookAndFeel;

    @CommandLine.Option(names = "--enable-edt-violation-detector", defaultValue = "false", description = "enable the Event Dispatch Thread (EDT) violation checker")
    boolean edtViolationDetector;

    @CommandLine.Option(names = "--ui-scale", defaultValue = "1", description = "scale to use for the FlatLaf.uiScale value")
    int uiScale;

    @CommandLine.Option(names = "--always-use-tabs", defaultValue = "false", description = "always use tabs, even when there is only 1 dockable in the tab group")
    boolean alwaysUseTabs;

    @CommandLine.Option(names = "--tab-location", defaultValue = "NONE", description = "Location to display tabs. values: ${COMPLETION-CANDIDATES}")
    DockableTabPreference tabLocation;

    @CommandLine.Option(names = "--use-layered-pane-overlay", defaultValue = "false", description = "force the JLayeredPane-based docking overlay instead of the transparent-window overlay")
    boolean useLayeredPaneOverlay;

    public void apply() {
        DockingSettings.setUseLayeredPaneOverlay(useLayeredPaneOverlay);

        if (alwaysUseTabs) {
            if (tabLocation == DockableTabPreference.TOP) {
                Settings.setDefaultTabPreference(DockableTabPreference.TOP_ALWAYS);
            } else {
                Settings.setDefaultTabPreference(DockableTabPreference.BOTTOM_ALWAYS);
            }
        } else {
            Settings.setDefaultTabPreference(tabLocation);
        }

        try {
            FlatLaf.registerCustomDefaultsSource("docking");
            System.setProperty("flatlaf.uiScale", String.valueOf(uiScale));

            switch (lookAndFeel) {
                case "light":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "dark":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "github-dark":
                    UIManager.setLookAndFeel(new FlatMTGitHubDarkIJTheme());
                    break;
                case "solarized-dark":
                    UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
                    break;
                default:
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                             UnsupportedLookAndFeelException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
            }
            FlatLaf.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (edtViolationDetector) {
            FailOnThreadViolationRepaintManager.install();
        }
    }
}
