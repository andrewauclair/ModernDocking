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
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class ProjectPanel extends JPanel implements Dockable {

    private static final Set<String> EXCLUDED_NAMES = Set.of(
            ".git", ".gradle", ".idea", "build", "out", ".DS_Store"
    );

    private final String id;
    private final DockingAPI docking;
    private final String anchorId;

    public ProjectPanel(DockingAPI docking, String id, File rootDir, String anchorId) {
        this.docking = docking;
        this.id = id;
        this.anchorId = anchorId;
        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDir);
        buildTree(rootDir, root);

        JTree tree = new JTree(root);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof File) {
                        setText(((File) userObj).getName());
                    }
                }
                return this;
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object userObj = node.getUserObject();
                        if (userObj instanceof File) {
                            File file = (File) userObj;
                            if (file.isFile()) {
                                openFile(file);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        docking.registerDockable(this);
    }

    private void buildTree(File dir, DefaultMutableTreeNode node) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator
                .<File, Boolean>comparing(f -> !f.isDirectory())
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        for (File file : files) {
            if (EXCLUDED_NAMES.contains(file.getName())) {
                continue;
            }
            if (file.getName().startsWith(".")) {
                continue;
            }
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
            node.add(child);
            if (file.isDirectory()) {
                buildTree(file, child);
            }
        }
    }

    private void openFile(File file) {
        String editorId = "editor-" + file.getName();
        if (docking.isDockableRegistered(editorId)) {
            if (docking.isDocked(editorId)) {
                docking.bringToFront(editorId);
            } else {
                dockToEditorArea(editorId);
            }
        } else {
            new EditorPanel(docking, editorId, file.getName(), file.getAbsolutePath());
            dockToEditorArea(editorId);
        }
    }

    private void dockToEditorArea(String editorId) {
        if (docking.isDocked(anchorId)) {
            docking.dock(editorId, anchorId, DockingRegion.CENTER);
        } else {
            docking.getDockables().stream()
                    .filter(d -> d.getType() == EditorPanel.TYPE && docking.isDocked(d))
                    .findFirst()
                    .ifPresent(sibling -> docking.dock(editorId, sibling.getPersistentID(), DockingRegion.CENTER));
        }
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

    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}
