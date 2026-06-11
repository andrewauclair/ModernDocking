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
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

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
                {"background", "Color", "#1a2b3c"},
                {"fontSize", "Integer", "13"},
                {"visible", "Boolean", "true"},
                {"label", "String", "Hello"},
        };
        JTable table = new JTable(rows, new String[]{"Key", "Type", "Value"});

        add(controls, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        add(scroll, BorderLayout.CENTER);

        docking.registerDockable(this);
    }

    @Override
    public void updateProperties() {
    }

    @Override
    public String getPersistentID() {
        return id;
    }

    @Override
    public String getTabText() {
        return "Properties";
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
