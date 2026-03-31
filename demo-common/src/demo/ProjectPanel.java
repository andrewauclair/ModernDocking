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
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class ProjectPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;

    public ProjectPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project");
        DefaultMutableTreeNode src = new DefaultMutableTreeNode("src/main/java");
        src.add(new DefaultMutableTreeNode("Main.java"));
        src.add(new DefaultMutableTreeNode("Utils.java"));
        src.add(new DefaultMutableTreeNode("Config.java"));
        DefaultMutableTreeNode test = new DefaultMutableTreeNode("src/test/java");
        test.add(new DefaultMutableTreeNode("MainTest.java"));
        root.add(src);
        root.add(test);
        root.add(new DefaultMutableTreeNode("build.gradle"));
        JTree tree = new JTree(root);
        tree.setRootVisible(true);
        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return "Project";
    }

    @Override
    public DockableStyle getStyle() {
        return DockableStyle.VERTICAL;
    }
}
