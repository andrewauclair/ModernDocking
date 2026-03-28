package demo;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.api.DockingAPI;

import javax.swing.*;
import java.awt.*;

public class OutputPanel extends JPanel implements Dockable {

    private final String id;
    private final DockingAPI docking;

    public OutputPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());
        JTextArea out = new JTextArea(
                "[INFO] Build started\n" +
                "[INFO] Compiling sources...\n" +
                "[INFO] BUILD SUCCESS\n" +
                "[INFO] Total time: 1.234 s");
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        out.setEditable(false);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> out.setText(""));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(clear);
        JScrollPane scroll = new JScrollPane(out);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        docking.registerDockable(this);
    }

    @Override public String getPersistentID()    { return id; }
    @Override public String getTabText()         { return "Output"; }
    @Override public DockableStyle getStyle()    { return DockableStyle.HORIZONTAL; }
    @Override public boolean isAutoHideAllowed() { return true; }
}
