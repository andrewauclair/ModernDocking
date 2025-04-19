/*
Copyright (c) 2025 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.event;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import javax.swing.JFrame;

/**
 * Used to inform the application that a new floating frame has been created
 */
public interface NewFloatingFrameListener {
    /**
     * Called when a new floating frame is created when restoring a window layout or when dropping a tab group of dockables
     * <p>
     * NOTE: Modern Docking will close and dispose of all FloatingFrames it creates
     *
     * @param frame The new floating frame created by Modern Docking
     * @param root The root panel of the frame. Provided in case the application wishes to rebuild the layout
     */
    void newFrameCreated(JFrame frame, RootDockingPanelAPI root);

    /**
     * Called when a new floating frame is created by ModernDocking. The frame will already be visible
     * and the dockable will already be docked.
     * <p>
     * NOTE: ModernDocking will close and dispose of all FloatingFrames it creates
     *
     * @param frame The new floating frame created by Modern Docking
     * @param root The root panel of the frame. Provided in case the application wishes to rebuild the layout
     * @param dockable The dockable that this floating frame was created for and that is currently docked in the frame
     */
    void newFrameCreated(JFrame frame, RootDockingPanelAPI root, Dockable dockable);
}
