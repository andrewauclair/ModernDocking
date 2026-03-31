package demo;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class TestHarnessPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;
    private final StateTableModel stateModel;
    private final Timer refreshTimer;

    public TestHarnessPanel(DockingAPI docking, String id, List<Dockable> dockables) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());

        stateModel = new StateTableModel(docking, dockables);
        JTable stateTable = new JTable(stateModel);
        stateTable.setRowHeight(22);
        stateTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        stateTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        stateTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        stateTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        stateTable.getColumnModel().getColumn(3).setPreferredWidth(45);

        stateTable.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                                t, value, sel, focus, row, col);
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        if ("\u2705".equals(value)) {
                            lbl.setForeground(new Color(0, 150, 0));
                        }
                        else if ("\u274c".equals(value)) {
                            lbl.setForeground(new Color(200, 0, 0));
                        }
                        else {
                            lbl.setForeground(UIManager.getColor("Label.foreground"));
                        }
                        return lbl;
                    }
                });

        stateTable.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JCheckBox()));
        stateTable.getColumnModel().getColumn(2).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                        JCheckBox cb = new JCheckBox("", Boolean.TRUE.equals(value));
                        cb.setHorizontalAlignment(JLabel.CENTER);
                        cb.setOpaque(true);
                        cb.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
                        return cb;
                    }
                });

        JButton refreshBtn = new JButton("Refresh Now");
        refreshBtn.addActionListener(e -> stateModel.refresh());

        JCheckBox autoRefreshCb = new JCheckBox("Auto (500 ms)", true);

        JButton resetExpected = new JButton("Reset Expected to false");
        resetExpected.addActionListener(e -> stateModel.resetExpected());

        JPanel stateButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        stateButtons.add(refreshBtn);
        stateButtons.add(autoRefreshCb);
        stateButtons.add(resetExpected);

        JPanel stateWrapper = new JPanel(new BorderLayout());
        stateWrapper.setBorder(BorderFactory.createTitledBorder("API State Checks"));
        stateWrapper.add(new JScrollPane(stateTable), BorderLayout.CENTER);
        stateWrapper.add(stateButtons, BorderLayout.SOUTH);

        refreshTimer = new Timer(500, e -> {
            if (autoRefreshCb.isSelected()) {
                stateModel.refresh();
            }
        });
        refreshTimer.start();

        JPanel checklistPanel = new JPanel();
        checklistPanel.setLayout(new BoxLayout(checklistPanel, BoxLayout.Y_AXIS));
        buildChecklist(checklistPanel);

        JButton uncheckAll = new JButton("Uncheck All");
        uncheckAll.addActionListener(e -> {
            for (Component c : checklistPanel.getComponents()) {
                if (c instanceof JCheckBox) {
                    ((JCheckBox) c).setSelected(false);
                }
            }
        });

        JPanel checklistWrapper = new JPanel(new BorderLayout());
        checklistWrapper.setBorder(BorderFactory.createTitledBorder("Manual Visual Checks"));
        checklistWrapper.add(new JScrollPane(checklistPanel), BorderLayout.CENTER);
        checklistWrapper.add(uncheckAll, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stateWrapper, checklistWrapper);
        split.setResizeWeight(0.55);

        add(split, BorderLayout.CENTER);

        docking.registerDockable(this);
    }

    private void buildChecklist(JPanel panel) {
        section(panel, "Core Drag & Drop");
        check(panel, "NORTH handle → correct top split");
        check(panel, "SOUTH handle → correct bottom split");
        check(panel, "EAST / WEST handles → correct side splits");
        check(panel, "CENTER handle → tab group formed");
        check(panel, "Root NORTH / SOUTH / EAST / WEST handles work");
        check(panel, "Divider is draggable after docking");

        section(panel, "Tab Groups");
        check(panel, "Tab text matches getTabText() for each dockable");
        check(panel, "Dragging tab out of group makes it standalone");

        section(panel, "Float");
        check(panel, "Drag off window → FloatingFrame with correct title");
        check(panel, "Drop floating dockable back → docks correctly");
        check(panel, "Dockables do NOT cross between frame 1 and frame 2");

        section(panel, "Auto-Hide");
        check(panel, "WEST: tab button appears, slides out/back on click");
        check(panel, "SOUTH: tab button appears, slides out/back on click");
        check(panel, "EAST: tab button appears, slides out/back on click");

        section(panel, "Focused Mode (1.5.0)");
        check(panel, "Enter: target fills container, all others hidden");
        check(panel, "Exit: all panels restored to correct positions");
        check(panel, "Frame 2 focused mode independent of frame 1");

        section(panel, "Layout Persistence");
        check(panel, "Frame 1 and frame 2 persist to separate files");
        check(panel, "Save + restore: all panels in correct positions");
        check(panel, "Save + restore: split proportions match");

        section(panel, "LAF Switch");
        check(panel, "Both frames update when LAF is switched");
    }

    private void section(JPanel panel, String title) {
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
    }

    private void check(JPanel panel, String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cb);
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return "Test Harness";
    }

    static class StateTableModel extends AbstractTableModel {

        private static final String[] COLS = {"Query", "Live Value", "Expected (true = pass)", "Pass?"};

        private final List<String> labels = new ArrayList<>();
        private final List<Supplier<Boolean>> queries = new ArrayList<>();
        private final List<Boolean> expected = new ArrayList<>();
        private final List<Boolean> live = new ArrayList<>();
        private final List<String> passState = new ArrayList<>();

        StateTableModel(DockingAPI docking, List<Dockable> dockables) {
            for (Dockable d : dockables) {
                add("isDocked(" + d.getPersistentID() + ")", () -> docking.isDocked(d));
            }
            for (Dockable d : dockables) {
                add("isHidden(" + d.getPersistentID() + ")", () -> docking.isHidden(d));
            }
            for (Dockable d : dockables) {
                // 1.5.0
                add("inFocusedMode(" + d.getPersistentID() + ")", () -> docking.inFocusedMode(d));
            }
            refresh();
        }

        private void add(String label, Supplier<Boolean> query) {
            labels.add(label);
            queries.add(query);
            expected.add(Boolean.FALSE);
            live.add(Boolean.FALSE);
            passState.add("—");
        }

        void refresh() {
            for (int i = 0; i < queries.size(); i++) {
                boolean liveVal = queries.get(i).get();
                live.set(i, liveVal);
                passState.set(i, (liveVal == expected.get(i)) ? "\u2705" : "\u274c");
            }
            fireTableDataChanged();
        }

        void resetExpected() {
            for (int i = 0; i < expected.size(); i++) {
                expected.set(i, Boolean.FALSE);
            }
            refresh();
        }

        @Override
        public int getRowCount() {
            return labels.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return labels.get(row);
                case 1:
                    return live.get(row).toString();
                case 2:
                    return expected.get(row);
                case 3:
                    return passState.get(row);
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 2) {
                expected.set(row, Boolean.TRUE.equals(value));
                boolean liveVal = live.get(row);
                passState.set(row, (liveVal == expected.get(row)) ? "\u2705" : "\u274c");
                fireTableRowsUpdated(row, row);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 2;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 2 ? Boolean.class : String.class;
        }
    }
}
