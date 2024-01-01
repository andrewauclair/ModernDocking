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
package ModernDocking.exception;

import ModernDocking.api.RootDockingPanelAPI;

import java.awt.*;

/**
 * This exception is thrown when the docking framework fails to register a RootDockingPanel because one already exists for the window
 */
public class RootDockingPanelRegistrationFailureException extends RuntimeException {
    private final RootDockingPanelAPI panel;
    private final Window window;

    /**
     * Create a new instance of this exception
     *
     * @param panel The RootDockingPanel being registered
     * @param window The window the root is being registered for
     */
    public RootDockingPanelRegistrationFailureException(RootDockingPanelAPI panel, Window window) {
        super("RootDockingPanel already registered for frame: " + window);
        this.panel = panel;
        this.window = window;
    }

    /**
     * Retrieve the panel that failed to register
     *
     * @return The RootDockingPanel being registered
     */
    public RootDockingPanelAPI getPanel() {
        return panel;
    }

    /**
     * Retrieve the window the root was being registered for
     *
     * @return The window the root is being registered for
     */
    public Window getWindow() {
        return window;
    }
}
