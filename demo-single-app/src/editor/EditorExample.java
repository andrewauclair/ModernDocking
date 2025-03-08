package editor;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.DynamicDockableParameters;
import io.github.andrewauclair.moderndocking.app.AppState;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class EditorExample {
    public static class FilePanel extends JPanel implements Dockable {
        private File file;

        @DockingProperty(name = "file-path", required = true, defaultValue = "")
        private String filePath;
        private DynamicDockableParameters parameters;

        public FilePanel(File file) {
            parameters = new DynamicDockableParameters(file.getName(), file.getName(), file.getName());

            Docking.registerDockable(this);

            this.file = file;
            filePath = file.getAbsolutePath();
        }

        public FilePanel(DynamicDockableParameters parameters) {
            this.parameters = parameters;

            Docking.registerDockable(this);
        }

        @Override
        public void updateProperties() {
            file = new File(filePath);
        }

        @Override
        public String getPersistentID() {
            return parameters.getPersistentID();
        }

        @Override
        public String getTabText() {
            return parameters.getTabText();
        }
    }

    public static class FileAnchor extends JPanel implements Dockable {
        public FileAnchor() {
            Docking.registerDockingAnchor(this);

            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;

            add(new JLabel("Dock files here"), gbc);
        }

        @Override
        public String getPersistentID() {
            return "files";
        }

        @Override
        public String getTabText() {
            return "";
        }
    }

    public static class FolderPanel extends JPanel implements Dockable {
        private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        public FolderPanel(File folder) {
            Docking.registerDockable(this);

            addFiles(folder, root);

            JTree tree = new JTree(root);
            tree.setEditable(false);
            tree.expandPath(new TreePath(root));

            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    return super.getTreeCellRendererComponent(tree, ((File) value).getName(), sel, expanded, leaf, row, hasFocus);
                }
            });

            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        TreePath path = tree.getPathForRow(row);
                        new FilePanel((File) path.getLastPathComponent());
                    }
                }
            });
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;

            add(tree, gbc);
        }

        private void addFiles(File folder, DefaultMutableTreeNode parent) {
            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(folder);

            parent.add(fileNode);

            if (!folder.isDirectory() || folder.listFiles() == null) {
                return;
            }

            for (File file : folder.listFiles()) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
                fileNode.add(child);
                if (file.isDirectory()) {
                    addFiles(file, child);
                }
            }
        }

        @Override
        public String getPersistentID() {
            return "folder";
        }

        @Override
        public String getTabText() {
            return "Folder";
        }
    }

    public static class InfoPanel extends JPanel implements Dockable {
        public InfoPanel() {
            Docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return "info";
        }

        @Override
        public String getTabText() {
            return "Info";
        }

        @Override
        public String getTitleText() {
            return "File Information";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.setup(new FlatLightLaf());

            JFrame mainFrame = new JFrame();
            RootDockingPanel root = new RootDockingPanel();
            mainFrame.add(root);
            mainFrame.setSize(300, 300);
            mainFrame.setVisible(true);

            Docking.initialize(mainFrame);
            Docking.registerDockingPanel(root, mainFrame);

            new InfoPanel();
            new FileAnchor();
            new FolderPanel(new File("."));

            AppState.setAutoPersist(true);
            AppState.setPersistFile(new File(System.getProperty("java.io.tmpdir") + "/editor-example-layout.xml"));

            AppState.setDefaultApplicationLayout(
                    new WindowLayoutBuilder("info")
                    .dock("folder", "info", DockingRegion.NORTH)
                    .dock("files", "folder", DockingRegion.EAST)
                    .buildApplicationLayout()
            );

            try {
                AppState.restore();
            } catch (DockingLayoutException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
