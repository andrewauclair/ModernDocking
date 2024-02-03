package ModernDocking.floating;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;

public class DockingOverlayInterface {
    public void setTargetDockable(Dockable target) {

    }

    public void setTargetRootRegion(DockingRegion rootRegion) {

    }

    public void setTargetDockableRegion(DockingRegion dockableRegion) {

    }

    public void setTargetPinRegion(ToolbarLocation pinningRegion) {

    }

    public void setFloatingDockable(JPanel floating) {

    }

    public void setOverTab(boolean overTab) {

    }

    public void update(Point screenPos) {

    }

    public void setActive(boolean active) {

    }

    public DockingRegion getRegion(Point screenPos) {
        return null;
    }

    public ToolbarLocation getToolbarLocation() {
        return null;
    }

    public boolean isDockingToRoot() {
        return false;
    }

    public boolean isDockingToPin() {
        return false;
    }

    public boolean isDockingToDockable() {
        return false;
    }

    public void paint(Graphics g) {

    }

    public void setTargetTab(Rectangle rect) {

    }

    public void setBeforeTab(boolean b) {

    }
}
