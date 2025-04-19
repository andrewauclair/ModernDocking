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
package io.github.andrewauclair.moderndocking;

/**
 * Specify the tab preference of dockables and applications
 */
public enum DockableTabPreference {
    /**
     * Dockable has no tab preference
     */
    NONE,
    /**
     * Dockable prefers tabs on the bottom
     */
    BOTTOM,
    /**
     * Dockable prefers tabs on the top
     */
    TOP,

    // use these options with Settings.setDefaultTabPreference to force tabs to always display, even with a single dockable
    /**
     * Application wants tabs always on the top
     */
    BOTTOM_ALWAYS,
    /**
     * Application wants tabs always on the bottom
     */
    TOP_ALWAYS
}
