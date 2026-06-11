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
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;

    public OutputPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());
        JTextArea out = new JTextArea(
                "[INFO] Build started\n" +
                        "[INFO] Compiling sources...\n" +
                        "[INFO] BUILD SUCCESS\n" +
                        "[INFO] Total time: 1.234 s");
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        out.setEditable(false);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> out.setText(""));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(clear);
        JScrollPane scroll = new JScrollPane(out);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return "Output";
    }

    @Override
    public DockableStyle getStyle() {
        return DockableStyle.HORIZONTAL;
    }

    @Override
    public boolean isAutoHideAllowed() {
        return true;
    }

    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}
