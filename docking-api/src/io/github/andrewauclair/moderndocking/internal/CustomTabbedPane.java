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
package io.github.andrewauclair.moderndocking.internal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Custom JTabbedPane to be used by Modern Docking in order to add keyboard shortcuts for moving between tabs
 */
public class CustomTabbedPane extends JTabbedPane {
    /**
     * Create a new instance and configure the keyboard shortcuts
     */
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

    /**
     * Find the index of the tab that our mouse is over
     *
     * @param mousePosOnScreen The position of the mouse on screen
     * @param ignoreY Only check if we're within the X bounds
     *
     * @return The index of the tab at the mouse position, or -1 if none is found
     */
    public int getTargetTabIndex(Point mousePosOnScreen, boolean ignoreY) {
        // convert the screen mouse position to a position on the tabbed pane
        Point newPoint = new Point(mousePosOnScreen);
        SwingUtilities.convertPointFromScreen(newPoint, this);

        Point d = isTopBottomTabPlacement(getTabPlacement()) ? new Point(1, 0) : new Point(0, 1);

        for (int i = 0; i < getTabCount(); i++) {
            Rectangle tab = getBoundsAt(i);

            if (ignoreY) {
                // we only care to check the x value
                newPoint.y = tab.y;
            }

            if (tab.contains(newPoint)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if the tab preference is TOP or BOTTOM
     *
     * @param tabPlacement The tab placement
     * @return true if tab is TOP or BOTTOM
     */
    public static boolean isTopBottomTabPlacement(int tabPlacement) {
        return tabPlacement == TOP || tabPlacement == BOTTOM;
    }
}
