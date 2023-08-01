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

import ModernDocking.Dockable;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ThemesPanel extends BasePanel implements Dockable {
    public ThemesPanel() {
        super("Themes", "themes");

        JTable table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setTableHeader(null);

        DefaultTableModel model = new DefaultTableModel(0, 1);

        model.addRow(new Object[] { "FlatLaf Light"});
        model.addRow(new Object[] { "FlatLaf Dark"});
        model.addRow(new Object[] { "Github Dark"});
        model.addRow(new Object[] { "Solarized Dark"});
        table.setModel(model);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        add(new JScrollPane(table), gbc);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (table.getSelectedRow() == -1) {
                return;
            }
            String lookAndFeel = (String) model.getValueAt(table.getSelectedRow(), 0);

            SwingUtilities.invokeLater(() -> {
            try {
                switch (lookAndFeel) {
                    case "FlatLaf Light":
                        UIManager.setLookAndFeel(new FlatLightLaf());
                        break;
                    case "FlatLaf Dark":
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                        break;
                    case "Github Dark":
                        UIManager.setLookAndFeel(new FlatGitHubDarkIJTheme());
                        break;
                    case "Solarized Dark":
                        UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
                        break;
                }
                FlatLaf.updateUI();
            } catch (UnsupportedLookAndFeelException ex) {
                throw new RuntimeException(ex);
            }
            });
        });
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public boolean isFloatingAllowed() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return false;
    }
}
