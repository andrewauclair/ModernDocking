package basic;

import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class OutputPanel extends ToolPanel {
    @DockingProperty(name = "first-column-name", defaultValue = "one")
    private String firstColumnName;

    private JTable table = new JTable(new DefaultTableModel(new String[] { "one", "two"}, 0));

    private Map<String, String> properties = new HashMap<>();

    public OutputPanel(DockingAPI docking, String title, String persistentID, DockableStyle style, Icon icon) {
        super(docking, title, persistentID, style, icon);

        table.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane comp = new JScrollPane(table);
        comp.setBorder(BorderFactory.createEmptyBorder());
        add(comp);

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

                docking.getAppState().persist();
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                updateColumnSizesProp();

                docking.getAppState().persist();
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
    }

    private void updateColumnsProp() {
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();

        String prop = "";

        while (columns.hasMoreElements()) {
            prop += columns.nextElement().getHeaderValue().toString();
            prop += ",";
        }

        properties.put("columns", prop);
    }

    private void updateColumnSizesProp() {
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();

        String prop = "";

        while (columns.hasMoreElements()) {
            prop += columns.nextElement().getWidth();
            prop += ",";
        }

        properties.put("column-sizes", prop);
    }

//    @Override
//    public Map<String, String> getProperties() {
//        return properties;
//    }
//
//    @Override
//    public void setProperties(Map<String, String> properties) {
//        if (properties.get("columns") != null && properties.get("column-sizes") != null) {
//            String[] columns = properties.get("columns").split(",");
//            String[] columnSizes = properties.get("column-sizes").split(",");
//
//
//            List<TableColumn> tableColumns = Collections.list(table.getColumnModel().getColumns());
//
//            for (int i = 0; i < columns.length; i++) {
//                int location = table.getColumnModel().getColumnIndex(columns[i]);
//
//                table.getColumnModel().moveColumn(location, i);
//                final int index = i;
//                SwingUtilities.invokeLater(() -> {
//                    table.getColumnModel().getColumn(index).setPreferredWidth(Integer.parseInt(columnSizes[index]));
//                });
//            }
//        }
//    }

    @Override
    public boolean hasMoreMenuOptions() {
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
