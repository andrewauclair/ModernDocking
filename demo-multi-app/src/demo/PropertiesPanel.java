package demo;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.api.DockingAPI;

import javax.swing.*;
import java.awt.*;

public class PropertiesPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;

    @DockingProperty(name = "filter-text", defaultValue = "")
    private final String filterText = "";

    @DockingProperty(name = "show-inherited", defaultValue = "true")
    private final boolean showInherited = true;

    public PropertiesPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        JTextField filter = new JTextField(10);
        filter.setToolTipText("Filter properties");
        JCheckBox inherited = new JCheckBox("Inherited", true);
        controls.add(new JLabel("Filter:"));
        controls.add(filter);
        controls.add(inherited);

        Object[][] rows = {
            {"background", "Color",   "#1a2b3c"},
            {"fontSize",   "Integer", "13"},
            {"visible",    "Boolean", "true"},
            {"label",      "String",  "Hello"},
        };
        JTable table = new JTable(rows, new String[]{"Key", "Type", "Value"});

        add(controls, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        add(scroll, BorderLayout.CENTER);

        docking.registerDockable(this);
    }

    @Override public void updateProperties() {}

    @Override public String getPersistentID()        { return id; }
    @Override public String getTabText()             { return "Properties"; }
    @Override public boolean isAutoHideAllowed()     { return true; }
}
