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

/**
 * Test harness dockable — not in the default layout. Open it from the View menu.
 * <p>
 * Three areas:
 * 1. API State Checks  — live JTable, toggleable expected values, ✅/❌ pass column
 * 2. Manual Checklist  — checkboxes for visual checks that cannot be automated
 *
 * @param dockables All dockables to track in the state table.
 */
public class TestHarnessPanel extends JPanel implements Dockable {

    private final StateTableModel stateModel;
    private final Timer refreshTimer;

    public TestHarnessPanel(List<Dockable> dockables) {
        setLayout(new BorderLayout());

        // --- State check table ---
        stateModel = new StateTableModel(dockables);
        JTable stateTable = new JTable(stateModel);
        stateTable.setRowHeight(22);
        stateTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        stateTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        stateTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        stateTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        stateTable.getColumnModel().getColumn(3).setPreferredWidth(45);

        // Pass/fail column renderer
        stateTable.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                                t, value, sel, focus, row, col);
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        if ("✅".equals(value)) {
                            lbl.setForeground(new Color(0, 150, 0));
                        }
                        else if ("❌".equals(value)) {
                            lbl.setForeground(new Color(200, 0, 0));
                        }
                        else {
                            lbl.setForeground(UIManager.getColor("Label.foreground"));
                        }
                        return lbl;
                    }
                });

        // Expected column: checkbox renderer + editor
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

        // Auto-refresh timer
        refreshTimer = new Timer(500, e -> {
            if (autoRefreshCb.isSelected()) {
                stateModel.refresh();
            }
        });
        refreshTimer.start();

        // --- Manual visual checklist ---
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

        // Split: state checks on top, visual checklist on bottom
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stateWrapper, checklistWrapper);
        split.setResizeWeight(0.55);

        add(split, BorderLayout.CENTER);

        Docking.registerDockable(this);
    }

    private void buildChecklist(JPanel panel) {
        section(panel, "Core Drag & Drop");
        check(panel, "NORTH handle → correct top split");
        check(panel, "SOUTH handle → correct bottom split");
        check(panel, "EAST / WEST handles → correct side splits");
        check(panel, "CENTER handle → tab group formed");
        check(panel, "Root NORTH / SOUTH / EAST / WEST handles work");
        check(panel, "Divider is draggable after docking");
        check(panel, "Window resize maintains split proportions");
        check(panel, "Double-click divider → resets to 50/50");

        section(panel, "Tab Groups");
        check(panel, "Tab text matches getTabText() for each dockable");
        check(panel, "Tab overflow dropdown appears when tabs overflow");
        check(panel, "Clicking overflowed tab from dropdown selects it");
        check(panel, "Dragging tab out of group makes it standalone");
        check(panel, "Dragging last tab out removes the tab header");

        section(panel, "Float");
        check(panel, "Drag off window → FloatingFrame with correct title");
        check(panel, "Floating window moves when dragged by header");
        check(panel, "Drop floating dockable back → docks correctly");
        check(panel, "Dock between two floating windows works");
        check(panel, "Last dockable in window cannot be floated (header drag blocked)");

        section(panel, "Auto-Hide");
        check(panel, "WEST: tab button appears, slides out/back on click");
        check(panel, "SOUTH: tab button appears, slides out/back on click");
        check(panel, "EAST: tab button appears, slides out/back on click");
        check(panel, "Only one panel visible at a time across all toolbars");
        check(panel, "Unpin returns dockable to layout");
        check(panel, "Slide-out panel has resize handle (SlideBorder)");

        section(panel, "Focused Mode (1.5.0)");
        check(panel, "Enter: target fills container, all others hidden");
        check(panel, "Exit: all panels restored to correct positions");
        check(panel, "Split proportions identical before and after");
        check(panel, "Floating windows unaffected by focused mode in main window");
        check(panel, "Tab group selection correct after exit");
        check(panel, "No spurious DOCKED/UNDOCKED events (check Event Log counters)");
        check(panel, "XML: max-dockable attribute read correctly on load");
        check(panel, "XML: focused-dockable written (not max-dockable) on save");

        section(panel, "FloatUtilsLayer (1.5.0)");
        check(panel, "Overlay enabled: drag proxy appears and tracks mouse");
        check(panel, "Overlay enabled: NORTH/SOUTH/EAST/WEST handles highlight");
        check(panel, "Overlay enabled: drop onto handle docks correctly");
        check(panel, "Overlay enabled: drop to empty space floats correctly");
        check(panel, "Overlay disabled (FloatUtilsFrame): identical drag behavior");
        check(panel, "Tab target: proxy hides correctly over CENTER (known gap if not)");

        section(panel, "Frame Jostling Fix");
        check(panel, "No rapid toFront() flickering in overlap zone");
        check(panel, "Window switch happens once per boundary crossing (hysteresis)");
        check(panel, "Drop lands in correct committed window after hysteresis");

        section(panel, "Linux: Floating Frame Width Fix");
        check(panel, "Float width matches content — no stale large value");
        check(panel, "Window does not jump/resize after appearing");
        check(panel, "Correct size on second float of same dockable");

        section(panel, "isTabGroupAllowed (1.5.0)");
        check(panel, "CENTER handle absent when hovering over NoTabGroupPanel");
        check(panel, "NORTH/SOUTH/EAST/WEST handles still appear for NoTabGroupPanel");
        check(panel, "Dragging NoTabGroupPanel over normal dockable: CENTER shown");

        section(panel, "Splitter Changes");
        check(panel, "Horizontal split: divider drags through full range, no tearing");
        check(panel, "Vertical split: same");
        check(panel, "Nested splits: inner/outer dividers independent");
        check(panel, "Float + re-dock: divider returns to reasonable position");

        section(panel, "Layout Persistence");
        check(panel, "Save + restore: all panels in correct positions");
        check(panel, "Save + restore: floating window position/size correct");
        check(panel, "Save + restore: split proportions match");
        check(panel, "Save + restore: tab group selection correct");
        check(panel, "Save + restore: auto-hidden dockable on correct toolbar");
        check(panel, "Maximized window size not persisted (pre-max size restored)");

        section(panel, "LAF Switch");
        check(panel, "Switch to FlatLaf Light: headers, tabs, dividers update");
        check(panel, "Switch to FlatLaf Dark: same");
        check(panel, "No leftover visual artifacts after theme switch");
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
        return "test-harness";
    }

    @Override
    public String getTabText() {
        return "Test Harness";
    }

    // -----------------------------------------------------------------------
    // State table model — static nested class, needs only the dockable list
    // -----------------------------------------------------------------------

    static class StateTableModel extends AbstractTableModel {

        private static final String[] COLS = {"Query", "Live Value", "Expected (true = pass)", "Pass?"};

        private final List<String> labels = new ArrayList<>();
        private final List<Supplier<Boolean>> queries = new ArrayList<>();
        private final List<Boolean> expected = new ArrayList<>();
        private final List<Boolean> live = new ArrayList<>();
        private final List<String> passState = new ArrayList<>();

        StateTableModel(List<Dockable> dockables) {
            for (Dockable d : dockables) {
                add("isDocked(" + d.getPersistentID() + ")", () -> Docking.isDocked(d));
            }
            for (Dockable d : dockables) {
                add("isHidden(" + d.getPersistentID() + ")", () -> Docking.isHidden(d));
            }
            for (Dockable d : dockables) {
                // 1.5.0
                add("inFocusedMode(" + d.getPersistentID() + ")", () -> Docking.inFocusedMode(d));
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
                passState.set(i, (liveVal == expected.get(i)) ? "✅" : "❌");
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
                passState.set(row, (liveVal == expected.get(row)) ? "✅" : "❌");
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
