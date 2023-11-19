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
package ModernDocking.internal;

import javax.swing.*;
import java.awt.*;

public class CustomTabbedPane extends JTabbedPane {
    public int getTargetTabIndex(Point mousePosOnScreen, boolean ignoreY) {
        SwingUtilities.convertPointFromScreen(mousePosOnScreen, this);

        Point d = isTopBottomTabPlacement(getTabPlacement()) ? new Point(1, 0) : new Point(0, 1);

        for (int i = 0; i < getTabCount(); i++) {
            Rectangle tab = getBoundsAt(i);

            if (ignoreY) {
                // we only care to check the x value
                mousePosOnScreen.y = tab.y;
            }

            if (tab.contains(mousePosOnScreen)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isTopBottomTabPlacement(int tabPlacement) {
        return tabPlacement == TOP || tabPlacement == BOTTOM;
    }
}
