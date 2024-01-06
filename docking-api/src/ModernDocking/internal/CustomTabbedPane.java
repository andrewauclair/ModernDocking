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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CustomTabbedPane extends JTabbedPane {
    public CustomTabbedPane() {
        setFocusable(false);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK),
                "press-left"
        );


        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK),
                "press-right"
        );

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.ALT_GRAPH_DOWN_MASK),
                "press-left"
        );

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.ALT_GRAPH_DOWN_MASK),
                "press-right"
        );

        getActionMap().put("press-left",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int newIndex = getSelectedIndex() - 1;

                        if (newIndex < 0) {
                            newIndex = getTabCount() - 1;
                        }

                        setSelectedIndex(newIndex);
                    }
                }
        );

        getActionMap().put("press-right",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int newIndex = getSelectedIndex() + 1;

                        if (newIndex >= getTabCount()) {
                            newIndex = 0;
                        }

                        setSelectedIndex(newIndex);
                    }
                }
        );
    }

    public int getTargetTabIndex(Point mousePosOnScreen, boolean ignoreY) {
        SwingUtilities.convertPointFromScreen(mousePosOnScreen, this);

        String guess;

        for (int p = 0, lengthGues = guess.length(); p < lengthGues; p++) {

        }
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
