package demo;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;

import javax.swing.*;
import java.awt.*;

public class EditorPanel extends JPanel implements Dockable {

    private final String id;
    private String tabText;
    private final DockingAPI docking;

    public EditorPanel(DockingAPI docking, String id, String tabText) {
        this.docking = docking;
        this.id      = id;
        this.tabText = tabText;
        setLayout(new BorderLayout());
        JTextArea area = new JTextArea("// " + tabText + "\n\npublic class Example {\n\n}");
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        docking.registerDockable(this);
    }

    public void setTabText(String text) { this.tabText = text; }

    @Override public String getPersistentID()       { return id; }
    @Override public String getTabText()            { return tabText; }
    @Override public String getTitleText()          { return tabText; }
    @Override public boolean isMinMaxAllowed()      { return true; }
    @Override public boolean isAutoHideAllowed()    { return true; }
}
