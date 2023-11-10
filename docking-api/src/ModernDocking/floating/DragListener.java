package ModernDocking.floating;

import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.internal.DockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.HashMap;
import java.util.Map;

public class DragListener extends DragSourceAdapter implements DragSourceListener, DragSourceMotionListener {
    private final DragSource dragSource = new DragSource();
    private final Transferable transferable = new StringSelection("");

    private DockingUtilsFrame activeUtilsFrame = null;
    private static final Map<Window, DockingUtilsFrame> utilFrames = new HashMap<>();

    public static void registerDockingWindow(DockingAPI docking, Window window, RootDockingPanelAPI root) {
        utilFrames.put(window, new DockingUtilsFrame(docking, window, root));
    }

    public static void deregisterDockingWindow(Window window) {
        utilFrames.remove(window);
    }

    public DragListener(DockingAPI docking, Component dragSource, DockingPanel panel) {
        this.dragSource.addDragSourceMotionListener(DragListener.this);

        this.dragSource.createDefaultDragGestureRecognizer(dragSource, DnDConstants.ACTION_MOVE, dge -> {
            this.dragSource.startDrag(dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), transferable, DragListener.this);

            docking.undock(panel);

//            activeUtilsFrame = new DockingUtilsFrame();
//            mouseDragStarted(dge.getDragOrigin());
//
//            if (originalWindow instanceof JDialog) {
//                modalityType = ((JDialog) originalWindow).getModalityType();
//
//                ((JDialog) originalWindow).setModalityType(Dialog.ModalityType.MODELESS);
//
//                // Set all of these as invokeLater to force the order they happen in
//                SwingUtilities.invokeLater(() -> {
//                    // check that the floating frame still exists since we invoked later and time might have passed
//                    if (floatingFrame != null) {
//                        floatingFrame.toFront();
//                    }
//                });
//                SwingUtilities.invokeLater(() -> {
//                    // check that the utils frame still exists since we invoked later and time might have passed
//                    if (activeUtilsFrame != null) {
//                        activeUtilsFrame.toFront();
//                    }
//                });
//            }
        });
    }
}
