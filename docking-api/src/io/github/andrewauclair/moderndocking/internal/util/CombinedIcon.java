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
package io.github.andrewauclair.moderndocking.internal.util;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Special Icon to combine multiple Icons.
 * <p>
 * We use this to combine an image icon and rotated text icon on West/East docking toolbars
 */
public class CombinedIcon implements Icon {
    /**
     * The amount of padding to add between the two icons
     */
    private static final int PADDING = 2;

    private final Icon top;
    private final Icon bottom;

    /**
     * Create a new CombinedIcon
     *
     * @param top The icon to display on the top
     * @param bottom The icon to display on the bottom
     */
    public CombinedIcon(Icon top, Icon bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    @Override
    public int getIconHeight() {
        return top.getIconHeight() + PADDING + bottom.getIconHeight();
    }

    @Override
    public int getIconWidth() {
        return Math.max(top.getIconWidth(), bottom.getIconWidth());
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        top.paintIcon(c, g, x, y);
        bottom.paintIcon(c, g, x, y + PADDING + top.getIconHeight());
    }
}
