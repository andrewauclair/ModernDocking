package basic;

import ModernDocking.AppState;
import ModernDocking.DockableStyle;
import ModernDocking.DockingProperty;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.*;

public class OutputPanel extends ToolPanel {
    @DockingProperty(name = "first-column-name", defaultValue = "one")
    private String firstColumnName;

    private final JTable table = new JTable(new DefaultTableModel(new String[] { "one", "two"}, 0));

    private final Map<String, String> properties = new HashMap<>();

    public OutputPanel(String title, String persistentID, DockableStyle style, Icon icon) {
        super(title, persistentID, style, icon);

        add(new JScrollPane(table));

        updateColumnsProp();
        updateColumnSizesProp();

        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                updateColumnsProp();

                AppState.persist();
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                updateColumnSizesProp();

                AppState.persist();
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
    }

    private void updateColumnsProp() {
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();

        StringBuilder prop = new StringBuilder();

        while (columns.hasMoreElements()) {
            prop.append(columns.nextElement().getHeaderValue().toString());
            prop.append(",");
        }

        properties.put("columns", prop.toString());
    }

    private void updateColumnSizesProp() {
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();

        StringBuilder prop = new StringBuilder();

        while (columns.hasMoreElements()) {
            prop.append(columns.nextElement().getWidth());
            prop.append(",");
        }

        properties.put("column-sizes", prop.toString());
    }

    @Override
    public boolean getHasMoreOptions() {
        return true;
    }

    @Override
    public void addMoreOptions(JPopupMenu menu) {
        JMenuItem rename = new JMenuItem();
        rename.addActionListener(e -> {
            firstColumnName = "changed";
            table.getColumnModel().getColumn(0).setHeaderValue(firstColumnName);
        });
        menu.add(rename);
    }

    @Override
    public void updateProperties() {
        // properties have now been loaded, use them
        table.getColumnModel().getColumn(0).setHeaderValue(firstColumnName);
    }

    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}
