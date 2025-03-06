/*
Copyright (c) 2023 Andrew Auclair

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

/**
 * Event used when a dockable is docked, undocked, shown, or hidden
 */
public class DockingEvent {
    /**
     * The ID of a docking event. Describes what type of event occurred
     */
    public enum ID {
        /**
         * Dockable has been docked
         */
        DOCKED,
        /**
         * Dockable has been undocked
         */
        UNDOCKED,

        /**
         * Dockable has been shown
         */
        SHOWN,
        /**
         * Dockable has been hidden
         */
        HIDDEN,

        /**
         * Dockable has been assigned to an Auto Hide toolbar
         */
        AUTO_HIDE_ENABLED,
        /**
         * Dockable has been removed from an Auto Hide toolbar
         */
        AUTO_HIDE_DISABLED
    }

    private final ID id;
    private final Dockable dockable;

    /**
     * Create a new docking event
     *
     * @param id The ID of the event
     * @param dockable The dockable which has been effected
     */
    public DockingEvent(ID id, Dockable dockable) {
        this.id = id;
        this.dockable = dockable;
    }

    /**
     * The type of event that has occurred
     *
     * @return Event ID
     */
    public ID getID() {
        return id;
    }

    /**
     * Get the dockable which has been effected
     *
     * @return Dockable for this event
     */
    public Dockable getDockable() {
        return dockable;
    }
}
