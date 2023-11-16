package ModernDocking.internal;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

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
