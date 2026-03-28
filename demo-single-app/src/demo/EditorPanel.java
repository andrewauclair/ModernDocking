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
import io.github.andrewauclair.moderndocking.app.Docking;

import javax.swing.*;
import java.awt.*;

/**
 * General-purpose editor panel. Demonstrates a normal, fully-featured dockable:
 * closable, floatable, auto-hide allowed, maximize allowed.
 */
public class EditorPanel extends JPanel implements Dockable {

    private final String id;
    private String tabText;

    public EditorPanel(String id, String tabText) {
        this.id      = id;
        this.tabText = tabText;
        setLayout(new BorderLayout());
        JTextArea area = new JTextArea("// " + tabText + "\n\npublic class Example {\n\n}");
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        Docking.registerDockable(this);
    }

    public void setTabText(String text) {
        this.tabText = text;
    }

    @Override public String getPersistentID() { return id; }
    @Override public String getTabText()      { return tabText; }
    @Override public String getTitleText()    { return tabText; }
    @Override public boolean isMinMaxAllowed()    { return true; }
    @Override public boolean isAutoHideAllowed()  { return true; }
}
