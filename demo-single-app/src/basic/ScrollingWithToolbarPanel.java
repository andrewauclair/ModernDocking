package basic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class ScrollingWithToolbarPanel extends BasePanel {
    public ScrollingWithToolbarPanel() {
        super("scrolling", "Scrolling With Toolbar", "scroll-with-toolbar");

        setLayout(new GridBagLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JButton("Add"));
        toolBar.add(new JButton("Remove"));

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < 30; i++) {
            panel.add(new JLabel("label " + i), gbc);
            gbc.gridy++;
        }

        gbc.gridy = 0;
        add(toolBar, gbc);

        gbc.gridy++;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        add(new JScrollPane(panel), gbc);
    }

    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}
