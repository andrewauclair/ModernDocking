/*
Copyright (c) 2023 Andrew Auclair

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
package basic;

import ModernDocking.AppState;
import ModernDocking.DockingProperty;
import ModernDocking.DockingRegion;
import ModernDocking.internal.DockableProperties;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class PropertiesDemoPanel extends BasePanel {
    @DockingProperty(name = "sample_byte", defaultValue = "0")
    private byte byteValue;

    @DockingProperty(name = "sample_short", defaultValue = "0")
    private short shortValue;

    @DockingProperty(name = "sample_int", defaultValue = "0")
    private int intValue;

    @DockingProperty(name = "sample_long", defaultValue = "0")
    private long longValue;

    @DockingProperty(name = "sample_float")
    private float floatValue;

    @DockingProperty(name = "sample_double")
    private double doubleValue;

    @DockingProperty(name = "sample_char", defaultValue = "a")
    private char charValue;

    @DockingProperty(name = "sample_string")
    private String stringValue;

//    @DockingProperty(name = "sample_enum", defaultValue = "0")
//    private DockingRegion dockingRegion;

    private JTextField byteField = new JTextField();
    private JTextField shortField = new JTextField();
    private JTextField intField = new JTextField();
    private JTextField longField = new JTextField();
    private JTextField floatField = new JTextField();
    private JTextField doubleField = new JTextField();
    private JTextField charField = new JTextField();
    private JTextField stringField = new JTextField();
    private JComboBox<DockingRegion> enumField = new JComboBox<>();

    public PropertiesDemoPanel() {
        super("Properties Demo", "props-demo");

        PlainDocument doc = (PlainDocument) byteField.getDocument();
        doc.setDocumentFilter(new MyIntFilter());

        setLayout(new GridBagLayout());
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        add(new JLabel("Byte:"), gbc);
        gbc.gridx++;
        add(byteField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        JButton save = new JButton("Save");
        add(save);
        save.addActionListener(e -> {
            byteValue = Byte.parseByte(byteField.getText());
            AppState.persist();
        });
    }

    @Override
    public void updateProperties() {
        byteField.setText(Byte.toString(byteValue));
    }

    class MyIntFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            if (test(sb.toString())) {
                super.insertString(fb, offset, string, attr);
            } else {
                // warn the user and don't allow the insert
            }
        }

        private boolean test(String text) {
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            if (test(sb.toString())) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                // warn the user and don't allow the insert
            }

        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset + length);

            if (test(sb.toString())) {
                super.remove(fb, offset, length);
            } else {
                // warn the user and don't allow the insert
            }

        }
    }
}
