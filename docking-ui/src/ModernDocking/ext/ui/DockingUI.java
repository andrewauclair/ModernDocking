/*
Copyright (c) 2023 Andrew Auclair

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
package ModernDocking.ext.ui;

import ModernDocking.internal.DockedTabbedPanel;
import ModernDocking.internal.DockingInternal;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class DockingUI {
    private static boolean initialized = false;

    /**
     * Initialize the FlatLaf UI extension. This reconfigures the docking framework to use FlatLaf SVG icons and color filters.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        DockingInternal.createHeaderUI = FlatLafHeaderUI::new;

        FlatSVGIcon settingsIcon = new FlatSVGIcon("ui_ext_icons/settings.svg");

        DockedTabbedPanel.setSettingsIcon(settingsIcon);

        Color foreground = UIManager.getColor("TableHeader.foreground");

        settingsIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> foreground));

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                SwingUtilities.invokeLater(() -> {
                    Color newForeground = UIManager.getColor("TableHeader.foreground");
                    settingsIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> newForeground));
                });
            }
        });
    }
}
