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
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class EditorPanel extends JPanel implements Dockable {

    private final String id;
    private final String tabText;
    private final DockingAPI docking;

    @DockingProperty(name = "file-path", required = true, defaultValue = "")
    private String filePath;

    private final JTextArea area = new JTextArea();

    public EditorPanel(DockingAPI docking, String id, String tabText, String filePath) {
        this.docking = docking;
        this.id = id;
        this.tabText = tabText;
        this.filePath = filePath;
        setLayout(new BorderLayout());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        loadContent();
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        docking.registerDockable(this);
    }

    private void loadContent() {
        area.setText("");
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    area.setText(new String(Files.readAllBytes(file.toPath())));
                    area.setCaretPosition(0);
                } catch (IOException e) {
                    area.setText("// Could not read: " + filePath + "\n// " + e.getMessage());
                }
                return;
            }
        }
        area.setText("// " + tabText + "\n\npublic class Example {\n\n}");
    }

    @Override
    public void updateProperties() {
        loadContent();
    }

    @Override
    public boolean requestClose() {
        docking.deregisterDockable(this);
        return false;
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return tabText;
    }

    @Override
    public String getTitleText() {
        return tabText;
    }

    @Override
    public boolean isMinMaxAllowed() {
        return true;
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
