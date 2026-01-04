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
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class EditorExample {
    public static class FilePanel extends JPanel implements Dockable, DockingListener {
        private File file;

        @DockingProperty(name = "file-path", required = true, defaultValue = "")
        private String filePath;
        private DynamicDockableParameters parameters;

        private final JTextArea area = new JTextArea();

        public FilePanel(File file) {
            parameters = new DynamicDockableParameters(file.getName(), file.getName(), file.getName());

            Docking.registerDockable(this);

            this.file = file;
            filePath = file.getAbsolutePath();

            createContents();

            displayFileContents();
        }

        public FilePanel(DynamicDockableParameters parameters) {
            this.parameters = parameters;

            Docking.registerDockable(this);

            createContents();
        }

        private void createContents() {
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;

            add(new JScrollPane(area), gbc);
        }

        private void displayFileContents() {
            try (FileInputStream input = new FileInputStream(file)) {
                Scanner scanner = new Scanner(input);

                while (scanner.hasNext()) {
                    area.append(scanner.nextLine() + '\n');
                }
            }
            catch (IOException e) {
            }
        }

        @Override
        public void updateProperties() {
            file = new File(filePath);
            displayFileContents();
        }

        @Override
        public String getPersistentID() {
            return parameters.getPersistentID();
        }

        @Override
        public String getTabText() {
            return parameters.getTabText();
        }

        @Override
        public void dockingChange(DockingEvent e) {
            // TODO what's the right way to deregister when we close a dockable?
//            if (e.getID() == DockingEvent.ID.UNDOCKED) {
//                Docking.deregisterDockable(this);
//                Docking.removeDockingListener(this);
//            }
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
                    Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                    if (comp instanceof JLabel) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                        File file = (File) node.getUserObject();
                        if (file != null) {
                            ((JLabel) comp).setText(file.getName());
                        }
                    }

                    if(sel && !hasFocus) {
                        setBackgroundSelectionColor(UIManager.getColor("Panel.background"));
                        setTextSelectionColor(UIManager.getColor("Panel.foreground"));
                    }
                    else {
                        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
                        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
                    }
                    return comp;
                }
            });

            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        TreePath path = tree.getPathForRow(row);
                        if (path != null) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                            File file = (File) node.getUserObject();
                            if (file.isFile()) {
                                FilePanel panel = new FilePanel(file);
                                Docking.dock(panel, "files", DockingRegion.CENTER);
                            }
                        }
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
                if (file.isDirectory()) {
                    addFiles(file, fileNode);
                }
                else {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
                    fileNode.add(child);
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

            RootDockingPanel root = new RootDockingPanel();

            JFrame mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

//            AppState.setDefaultApplicationLayout(
//                    new WindowLayoutBuilder("info")
//                    .dock("folder", "info", DockingRegion.NORTH)
//                    .dock("files", "folder", DockingRegion.EAST)
//                    .buildApplicationLayout()
//            );

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
