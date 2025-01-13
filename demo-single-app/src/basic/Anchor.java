package basic;

import ModernDocking.Dockable;

import javax.swing.*;
import java.awt.*;

public class Anchor extends JPanel implements Dockable {
    public Anchor() {
        setBorder(BorderFactory.createLineBorder(Color.RED));

        add(new JLabel("This is an anchor"));
    }

    @Override
    public String getPersistentID() {
        return "anchor";
    }

    @Override
    public String getTabText() {
        return "";
    }
}
