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
package io.github.andrewauclair.moderndocking.exception;

import java.awt.Window;

/**
 * Exception thrown when a RootDockingPanel is not found for the given window
 */
public class RootDockingPanelNotFoundException extends RuntimeException {
    /**
     * The window that we wouldn't find a registered root docking panel for
     */
    private final Window window;

    /**
     * Create a new instance of this exception
     *
     * @param window The window we didn't find a root for
     */
    public RootDockingPanelNotFoundException(Window window) {
        super("No root panel for window has been registered.");
        this.window = window;
    }

    /**
     * Retrieve the window we didn't find a root for
     *
     * @return A Window, either a JFrame or JDialog
     */
    public Window getWindow() {
        return window;
    }
}
