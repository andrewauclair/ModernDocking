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
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

public class DockableInspectorPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;

    private Dockable lastDockable = null;
    private boolean manualMode = false;
    private boolean repopulating = false;

    private final JCheckBox manualCheckBox;
    private final JComboBox<Dockable> dockableChooser;
    private final DefaultTableModel tableModel;

    private static final String[] COLUMNS = {"Property", "Value"};

    public DockableInspectorPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());

        // ---- toolbar --------------------------------------------------------
        manualCheckBox = new JCheckBox("Manual");
        dockableChooser = new JComboBox<>();
        dockableChooser.setVisible(false);
        dockableChooser.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Dockable) {
                    setText(((Dockable) value).getTabText());
                }
                return this;
            }
        });

        dockableChooser.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                repopulateChooser();
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        dockableChooser.addActionListener(e -> {
            if (repopulating) {
                return;
            }
            Dockable selected = (Dockable) dockableChooser.getSelectedItem();
            if (selected != null) {
                lastDockable = selected;
                refresh(selected);
            }
        });

        manualCheckBox.addActionListener(e -> {
            manualMode = manualCheckBox.isSelected();
            dockableChooser.setVisible(manualMode);
            if (manualMode) {
                repopulateChooser();
                if (lastDockable != null) {
                    dockableChooser.setSelectedItem(lastDockable);
                }
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        toolbar.add(manualCheckBox);
        toolbar.add(dockableChooser);
        add(toolbar, BorderLayout.NORTH);

        // ---- table ----------------------------------------------------------
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        add(scroll, BorderLayout.CENTER);

        // ---- hover listener -------------------------------------------------
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (manualMode || event.getID() != MouseEvent.MOUSE_MOVED) {
                    return;
                }
                MouseEvent me = (MouseEvent) event;
                Component source = me.getComponent();
                if (source == null) {
                    return;
                }

                Component deepest = SwingUtilities.getDeepestComponentAt(source, me.getX(), me.getY());
                if (deepest == null) {
                    return;
                }

                Dockable found = findDockableUnderMouse(source, me.getX(), me.getY(), deepest);

                if (found != null) {
                    if (found != lastDockable) {
                        lastDockable = found;
                        refresh(lastDockable);
                    }
                } else if (lastDockable != null) {
                    if (!docking.isDockableRegistered(lastDockable.getPersistentID())
                            || (!docking.isDocked(lastDockable) && !docking.isHidden(lastDockable))) {
                        lastDockable = null;
                        clearTable();
                    }
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

        // ---- live-refresh timer ---------------------------------------------
        Timer timer = new Timer(500, e -> {
            if (lastDockable == null) {
                return;
            }
            // In manual mode the user explicitly chose a dockable, so never auto-clear it —
            // show it even if it becomes undocked or hidden.  Only clear if it has been
            // fully deregistered (e.g. an editor panel that was closed).
            if (manualMode) {
                if (docking.isDockableRegistered(lastDockable.getPersistentID())) {
                    refresh(lastDockable);
                } else {
                    lastDockable = null;
                    clearTable();
                }
            } else {
                if (!docking.isDockableRegistered(lastDockable.getPersistentID())) {
                    lastDockable = null;
                    clearTable();
                } else if (docking.isDocked(lastDockable) || docking.isHidden(lastDockable)) {
                    refresh(lastDockable);
                } else {
                    lastDockable = null;
                    clearTable();
                }
            }
        });
        timer.start();

        docking.registerDockable(this);
    }

    private void repopulateChooser() {
        Dockable current = (Dockable) dockableChooser.getSelectedItem();
        List<Dockable> all = new ArrayList<>(docking.getDockables());
        all.sort(Comparator.comparing(Dockable::getTabText));

        repopulating = true;
        try {
            dockableChooser.removeAllItems();
            for (Dockable d : all) {
                dockableChooser.addItem(d);
            }
            dockableChooser.setSelectedItem(current != null ? current : (!all.isEmpty() ? all.get(0) : null));
        }
        finally {
            repopulating = false;
        }

        Dockable selected = (Dockable) dockableChooser.getSelectedItem();
        if (selected != null) {
            lastDockable = selected;
            refresh(selected);
        }
    }

    /**
     * Locate the dockable under the mouse using three strategies in order:
     *
     * <ol>
     *   <li>Walk up the component tree: if any ancestor is itself a {@link Dockable}, return it.
     *       This covers hovering over a dockable's content area.</li>
     *   <li>Walk up to a {@link JTabbedPane}: tab labels are painted by the L&F with no child
     *       components, so {@code getDeepestComponentAt} returns the pane itself.
     *       {@code indexAtLocation} finds the tab under the cursor; if the cursor is over the
     *       content area (returns -1) fall back to the selected index.  Then scan the tab's
     *       content subtree for the {@link Dockable}.</li>
     *   <li>Walk up to a {@link JPanel} whose direct children include a {@link Dockable}: this
     *       matches the framework's internal {@code DisplayPanel}, which contains both the header
     *       UI and the dockable side-by-side.  Hovering over the header triggers this path.</li>
     * </ol>
     */
    private Dockable findDockableUnderMouse(Component source, int x, int y, Component deepest) {
        Component c = deepest;
        while (c != null) {
            // Strategy 1: ancestor is directly a Dockable
            if (c instanceof Dockable) {
                Dockable d = (Dockable) c;
                if (docking.isDockableRegistered(d.getPersistentID())) {
                    return d;
                }
            }

            // Strategy 2: ancestor is a JTabbedPane — covers tab labels and tab content headers
            if (c instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) c;
                Point local = SwingUtilities.convertPoint(source, x, y, tabs);
                int tabIdx = tabs.indexAtLocation(local.x, local.y);
                if (tabIdx < 0) {
                    // Mouse is over the content area, not a tab label
                    tabIdx = tabs.getSelectedIndex();
                }
                if (tabIdx >= 0) {
                    return findDockableInSubtree(tabs.getComponentAt(tabIdx));
                }
                return null;
            }

            // Strategy 3: ancestor is a JPanel whose direct children include a Dockable — this
            // matches the framework's DisplayPanel when hovering over the dockable's header.
            if (c instanceof JPanel) {
                Dockable d = findDockableDirectChild((JPanel) c);
                if (d != null) {
                    return d;
                }
            }

            c = c.getParent();
        }
        return null;
    }

    /** Scan {@code panel}'s immediate children (and one JScrollPane level) for a registered Dockable. */
    private Dockable findDockableDirectChild(JPanel panel) {
        for (Component child : panel.getComponents()) {
            if (child instanceof Dockable) {
                Dockable d = (Dockable) child;
                if (docking.isDockableRegistered(d.getPersistentID())) {
                    return d;
                }
            }
            if (child instanceof JScrollPane) {
                Component view = ((JScrollPane) child).getViewport().getView();
                if (view instanceof Dockable) {
                    Dockable d = (Dockable) view;
                    if (docking.isDockableRegistered(d.getPersistentID())) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    /** Depth-first search of {@code root}'s subtree for the first registered Dockable. */
    private Dockable findDockableInSubtree(Component root) {
        if (root == null) {
            return null;
        }
        if (root instanceof Dockable) {
            Dockable d = (Dockable) root;
            if (docking.isDockableRegistered(d.getPersistentID())) {
                return d;
            }
        }
        if (root instanceof JScrollPane) {
            return findDockableInSubtree(((JScrollPane) root).getViewport().getView());
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                Dockable found = findDockableInSubtree(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void refresh(Dockable d) {
        if (!docking.isDockableRegistered(d.getPersistentID())) {
            lastDockable = null;
            clearTable();
            return;
        }
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"getPersistentID()", d.getPersistentID()});
        tableModel.addRow(new Object[]{"getTabText()", d.getTabText()});
        tableModel.addRow(new Object[]{"getTitleText()", d.getTitleText()});
        tableModel.addRow(new Object[]{"isDocked", docking.isDocked(d)});
        tableModel.addRow(new Object[]{"isHidden", docking.isHidden(d)});
        tableModel.addRow(new Object[]{"inFocusedMode", docking.inFocusedMode(d)});
        tableModel.addRow(new Object[]{"isClosable()", d.isClosable()});
        tableModel.addRow(new Object[]{"isFloatingAllowed()", d.isFloatingAllowed()});
        tableModel.addRow(new Object[]{"isAutoHideAllowed()", d.isAutoHideAllowed()});
        tableModel.addRow(new Object[]{"isMinMaxAllowed()", d.isMinMaxAllowed()});
        tableModel.addRow(new Object[]{"isLimitedToWindow()", d.isLimitedToWindow()});
        tableModel.addRow(new Object[]{"getType()", d.getType()});
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return "Inspector";
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
