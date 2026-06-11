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
package io.github.andrewauclair.moderndocking.internal.floating;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import java.awt.Point;
import java.awt.dnd.DragSource;
import javax.swing.JFrame;

/**
 * Common interface for the floating overlay implementations.
 * <p>
 * {@link FloatUtilsFrame} renders the docking handles and highlight overlay on a transparent
 * top-level window. {@code FloatUtilsLayer} renders the same content on a {@code JPanel} added to
 * the host window's {@code JLayeredPane}, which works on platforms that do not support per-pixel
 * window translucency.
 */
public interface FloatUtils {

    /**
     * Activate the overlay for a new drag operation.
     *
     * @param floatListener    The listener that started the drag
     * @param floatingFrame    The temporary frame containing the floating dockable
     * @param dragSource       The drag source for the operation
     * @param mousePosOnScreen Initial mouse position in screen coordinates
     */
    void activate(FloatListener floatListener, JFrame floatingFrame, DragSource dragSource, Point mousePosOnScreen);

    /**
     * Deactivate the overlay when dragging ends.
     */
    void deactivate();

    /**
     * Check whether the mouse is currently over a root docking handle.
     *
     * @return true if over a root handle
     */
    boolean isOverRootHandle();

    /**
     * Get the root docking region currently indicated by the mouse.
     *
     * @return Root region, or null if not over a root handle
     */
    DockingRegion rootHandleRegion();

    /**
     * Check whether the mouse is currently over an auto-hide (pin) handle.
     *
     * @return true if over a pin handle
     */
    boolean isOverPinHandle();

    /**
     * Get the auto-hide toolbar location currently indicated by the mouse.
     *
     * @return Pin region, or null if not over a pin handle
     */
    ToolbarLocation pinRegion();

    /**
     * Check whether the mouse is currently over a dockable region handle.
     *
     * @return true if over a dockable handle
     */
    boolean isOverDockableHandle();

    /**
     * Check whether the mouse is currently positioned for a tab drop.
     *
     * @return true if over a tab target
     */
    boolean isOverTab();

    /**
     * Get the dockable region handle currently indicated by the mouse.
     *
     * @return Region of the target dockable, or null
     */
    DockingRegion dockableHandle();

    /**
     * Determine the docking region for a free mouse position over a target dockable.
     *
     * @param targetDockable   The dockable the mouse is over
     * @param floatingDockable The dockable being dragged
     * @param mousePosOnScreen Mouse position in screen coordinates
     *
     * @return The docking region
     */
    DockingRegion getDockableRegion(Dockable targetDockable, Dockable floatingDockable, Point mousePosOnScreen);

    /**
     * Release all resources associated with this overlay.
     * Called when the host window is deregistered.
     */
    void dispose();
}
