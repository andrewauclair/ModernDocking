/*
Copyright (c) 2024 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.internal.CustomTabbedPane;
import io.github.andrewauclair.moderndocking.internal.DockingComponentUtils;
import io.github.andrewauclair.moderndocking.internal.InternalRootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A special invisible frame that's used to provide docking handles and overlays
 */
public class FloatUtilsFrame extends JFrame implements DragSourceMotionListener, ComponentListener, WindowListener {
    /**
     * The window this utility frame is tied to
     */
    private final Window referenceDockingWindow;
    /**
     * The internal root of the reference docking window
     */
    private final InternalRootDockingPanel root;
    /**
     * The root docking handles to render
     */
    private final RootDockingHandles rootHandles;
    /**
     * The overlay to render
     */
    private final FloatingOverlay overlay;

    /**
     * The active floating listener
     */
    private FloatListener floatListener;
    /**
     * The temporary frame used for floating
     */
    private JFrame floatingFrame;
    /**
     * The source of the drag that was started
     */
    private DragSource dragSource;
    /**
     * The currently floating dockable
     */
    private Dockable currentDockable;
    /**
     * Handles to display on target dockables
     */
    private DockableHandles dockableHandles;

    private List<Window> windowStack = new ArrayList<>();

    /**
     * create a strategy for multi-buffering.
     */
    BufferStrategy bs;
    /**
     * Render panel used to render the utilities. Do this so we're not painting directly onto the transparent frame
     */
    JPanel renderPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            rootHandles.paint(g2);

            if (dockableHandles != null) {
                dockableHandles.paint(g2);
            }

            overlay.paint(g);
            g2.dispose();
        }
    };

    /**
     * Create a new instance for a specific window
     *
     * @param docking The docking instance
     * @param referenceDockingWindow The window that this utils frame is tied to
     * @param root The root within the reference window
     */
    public FloatUtilsFrame(DockingAPI docking, Window referenceDockingWindow, InternalRootDockingPanel root) {
        this.referenceDockingWindow = referenceDockingWindow;
        this.root = root;
        this.rootHandles = new RootDockingHandles(this, root);
        this.overlay = new FloatingOverlay(docking, this);

        setTitle("Utils Frame");

        this.referenceDockingWindow.addComponentListener(this);
        SwingUtilities.invokeLater(this::setSizeAndLocation);

        orderFrames();

        referenceDockingWindow.addWindowListener(this);
        addWindowListener(this);

        setLayout(null); // don't use a layout manager for this custom painted frame
        setUndecorated(true); // don't want to see a frame border
        setType(Type.UTILITY); // hide this frame from the task bar

        setBackground(new Color(0, 0, 0, 0)); // don't want a background for this frame
        getRootPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the root pane either. Workaround for a FlatLaf macOS issue.
        getContentPane().setBackground(new Color(0, 0, 0, 0)); // don't want a background for the content frame either.

//        add(renderPanel);
//        renderPanel.setOpaque(false);
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferCapabilities bufferCapabilities = gc.getBufferCapabilities();

        if (bufferCapabilities.isMultiBufferAvailable())
        {
            add(renderPanel);
            renderPanel.setOpaque(false);
        }

        try {
            if (getContentPane() instanceof JComponent) {
                ((JComponent) getContentPane()).setOpaque(false);
            }
        }
        catch (IllegalComponentStateException e) {
            // TODO we need to handle platforms that don't support translucent display
            // this exception indicates that the platform doesn't support changing the opacity
        }
    }

    /**
     * Activate this utilities frame and display it
     *
     * @param floatListener The float listener that started the drag
     * @param floatingFrame The frame that contains the floating dockable
     * @param dragSource The source of the drag
     * @param mousePosOnScreen The mouse position on screen
     */
    public void activate(FloatListener floatListener, JFrame floatingFrame, DragSource dragSource, Point mousePosOnScreen) {
        this.floatListener = floatListener;
        this.floatingFrame = floatingFrame;
        this.dragSource = dragSource;
        dragSource.addDragSourceMotionListener(this);

        floatingFrame.addWindowListener(this);

        mouseMoved(mousePosOnScreen);

        if (floatListener instanceof DisplayPanelFloatListener) {
            Dockable floatingDockable = ((DisplayPanelFloatListener) floatListener).getDockable();
            rootHandles.setFloatingDockable(floatingDockable);
        }

        setVisible(true);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferCapabilities bufferCapabilities = gc.getBufferCapabilities();

        if (!bufferCapabilities.isMultiBufferAvailable()) {
            createBufferStrategy(2);
            bs = this.getBufferStrategy();
        }
        orderFrames();
    }

    /**
     * Floating has ended, deactivate the utilities
     */
    public void deactivate() {
        setVisible(false);

        if (dragSource != null) {
            dragSource.removeDragSourceMotionListener(this);
        }
        if (floatingFrame != null) {
            floatingFrame.removeWindowListener(this);
        }
        floatListener = null;
        floatingFrame = null;
        dragSource = null;
        currentDockable = null;
        dockableHandles = null;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent event) {
        SwingUtilities.invokeLater(() -> mouseMoved(event.getLocation()));
    }

    private void mouseMoved(Point mousePosOnScreen) {
        if (dragSource == null) {
            return;
        }

        rootHandles.mouseMoved(mousePosOnScreen);

        if (dockableHandles != null) {
            dockableHandles.mouseMoved(mousePosOnScreen);
        }

        boolean prevVisible = overlay.isVisible();

        // hide the overlay. it will be marked visible again if we update it
        overlay.setVisible(false);

        if (!referenceDockingWindow.getBounds().contains(mousePosOnScreen)) {
            return;
        }

        Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(mousePosOnScreen, referenceDockingWindow);

        Dockable floatingDockable = null;

        if (floatListener instanceof DisplayPanelFloatListener) {
            floatingDockable = ((DisplayPanelFloatListener) floatListener).getDockable();
        }

        if (dockable != currentDockable) {
            if (dockable == null) {
                dockableHandles = null;
            }
            else if (floatListener instanceof DisplayPanelFloatListener) {
                dockableHandles = new DockableHandles(this, dockable, floatingDockable);
            }
            else {
                dockableHandles = new DockableHandles(this, dockable);
            }
        }
        currentDockable = dockable;

        if (rootHandles.isOverHandle()) {
            if (!floatingFrame.isVisible()) {
                changeVisibility(floatingFrame, true);
            }
            overlay.updateForRoot(root, rootHandles.getRegion());
        }
        else if (dockableHandles != null) {
            if (!floatingFrame.isVisible()) {
                changeVisibility(floatingFrame, true);
            }
            overlay.updateForDockable(currentDockable, floatingDockable, mousePosOnScreen, dockableHandles.getRegion());
        }
        else if (currentDockable == null && floatListener instanceof DisplayPanelFloatListener) {
            CustomTabbedPane tabbedPane = DockingComponentUtils.findTabbedPaneAtPos(mousePosOnScreen, referenceDockingWindow);

            changeVisibility(floatingFrame, tabbedPane == null);

            if (tabbedPane != null) {
                overlay.updateForTab(tabbedPane, mousePosOnScreen);
            }
        }
        else if (!floatingFrame.isVisible()) {
            changeVisibility(floatingFrame, true);
        }
        repaint();
        if (overlay.requiresRedraw() || prevVisible != overlay.isVisible()) {
            repaint();
            overlay.clearRedraw();
        }
    }

    private void changeVisibility(JFrame frame, boolean visible) {
        if (frame.isVisible() != visible) {
            frame.setVisible(visible);

            orderFrames();
        }
    }

    private void orderFrames() {
        windowStack.clear();
        windowStack.add(this);
        if (floatingFrame != null) {
            windowStack.add(floatingFrame);
        }
        windowStack.add(referenceDockingWindow);

        SwingUtilities.invokeLater(referenceDockingWindow::toFront);
    }

    @Override
    public void paint(Graphics g) {
        if (bs == null) {
            super.paint(g);
            return;
        }

        g = bs.getDrawGraphics();
        Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();

        g2.clearRect(0, 0, getWidth(), getHeight());

        rootHandles.paint(g2);

        if (dockableHandles != null) {
            dockableHandles.paint(g2);
        }

        overlay.paint(g);

        g2.dispose();

        bs.show();
    }
    @Override
    public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(this::setSizeAndLocation);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        SwingUtilities.invokeLater(this::setSizeAndLocation);
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Check if the mouse is over a root handle
     *
     * @return Is the mouse over a root handle?
     */
    public boolean isOverRootHandle() {
        return rootHandles.isOverHandle();
    }

    /**
     * Get the root region that's selected
     *
     * @return Root region or null
     */
    public DockingRegion rootHandleRegion() {
        return rootHandles.getRegion();
    }

    /**
     * Check if the mouse is over an auto-hide handle
     *
     * @return Is the mouse over an auto-hide handle?
     */
    public boolean isOverPinHandle() {
        return rootHandles.isOverPinHandle();
    }

    /**
     * Get the auto-hide region
     *
     * @return Auto-hide region or null
     */
    public ToolbarLocation pinRegion() {
        return rootHandles.getPinRegion();
    }

    /**
     * Check if the mouse is over a dockable handle
     *
     * @return Is the mouse over a dockable handle?
     */
    public boolean isOverDockableHandle() {
        if (dockableHandles == null) {
            return false;
        }
        return dockableHandles.getRegion() != null;
    }

    /**
     * Check if the mouse is over a tab
     *
     * @return Is the mouse over a tab?
     */
    public boolean isOverTab() {
        return overlay.isOverTab();
    }

    /**
     * Get the handle the mouse is over in the target dockable
     *
     * @return Region in the target dockable or null
     */
    public DockingRegion dockableHandle() {
        if (dockableHandles == null) {
            return null;
        }
        return dockableHandles.getRegion();
    }

    /**
     * Get the current region the dockable is over
     *
     * @param targetDockable The dockable the mouse is over
     * @param floatingDockable The active floating dockable
     * @param mousePosOnScreen The mouse position on screen
     *
     * @return The docking region or null
     */
    public DockingRegion getDockableRegion(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen) {
        return overlay.getRegion(targetDockable, floatingDockable, mousePosOnScreen);
    }

    private void setSizeAndLocation() {
        // skip for now if the reference window is not showing
        // we can't get its location on screen if it's not on screen
        if (!referenceDockingWindow.isShowing()) {
            return;
        }

        int padding = (int) (DockingHandle.HANDLE_ICON_SIZE * 1.75);

        Point location = new Point(referenceDockingWindow.getLocationOnScreen());
        Dimension size = new Dimension(referenceDockingWindow.getSize());

        location.x -= padding;
        location.y -= padding;

        size.width += padding * 2;
        size.height += padding * 2;

        // set location and size based on the reference docking frame
        setLocation(location);
        setSize(size);

        renderPanel.setSize(size);

        rootHandles.updateHandlePositions();

        revalidate();
        repaint();
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {
//        System.out.println("windowActivated " + ((JFrame) e.getWindow()).getTitle());
        windowStack.remove(e.getWindow());
        windowStack.add(e.getWindow());

        // expected:
        //   reference window
        //   floating window
        //   utils window

        if (windowStack.size() == 3 && windowStack.get(0) == referenceDockingWindow &&
            windowStack.get(1) == floatingFrame && windowStack.get(2) == this) {
            // perfect
//            System.out.println("Windows are in proper order 1");
        }
        else if (windowStack.size() == 2 && windowStack.get(0) == referenceDockingWindow &&
            windowStack.get(1) == this) {
            // perfect
//            System.out.println("Windows are in proper order 2");
        }
        else if (windowStack.get(0) != referenceDockingWindow) {
            // reference window isn't at the bottom, figure out which frame is out of order
            if (windowStack.get(0) == floatingFrame) {
//                System.out.println("first frame is floating frame, bring to front");
                SwingUtilities.invokeLater(floatingFrame::toFront);
            }
            else if (windowStack.get(0) == this) {
//                System.out.println("First frame is utils frame, bring to front");
                SwingUtilities.invokeLater(this::toFront);
            }
        }
        else if (windowStack.size() > 1 && windowStack.get(1) != floatingFrame) {
//            System.out.println("Second frame is not floating frame, bring utils to front");
            SwingUtilities.invokeLater(this::toFront);
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}