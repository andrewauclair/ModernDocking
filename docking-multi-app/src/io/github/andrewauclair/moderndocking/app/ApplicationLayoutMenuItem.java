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
package io.github.andrewauclair.moderndocking.app;

import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Special JMenuItem which will restore an ApplicationLayout when selected
 */
public class ApplicationLayoutMenuItem extends JMenuItem implements ActionListener {
    private final DockingAPI docking;
    /**
     * The name of the ApplicationLayout, used to get the layout from DockingLayouts
     */
    private final String layoutName;

    /**
     * Create a new ApplicationLayoutMenuItem for the specified layout. The layout name will be used as the display text.
     *
     * @param layoutName Name of the ApplicationLayout this ApplicationLayoutMenuItem will restore
     */
    public ApplicationLayoutMenuItem(DockingAPI docking, String layoutName) {
        super(layoutName);
        this.docking = docking;

        this.layoutName = layoutName;
        addActionListener(this);
    }

    /**
     * Create a new ApplicationLayoutMenuItem for the specified layout
     *
     * @param layoutName Name of the ApplicationLayout this ApplicationLayoutMenuItem will restore
     * @param text Display text of the JMenuItem
     */
    public ApplicationLayoutMenuItem(DockingAPI docking, String layoutName, String text) {
        super(text);
        this.docking = docking;

        this.layoutName = layoutName;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationLayout layout = DockingLayouts.getLayout(layoutName);

        if (layout == null) {
            JOptionPane.showMessageDialog(this, "Layout " + layoutName + " does not exist.");
        }
        else {
            docking.getDockingState().restoreApplicationLayout(layout);
        }
    }
}
