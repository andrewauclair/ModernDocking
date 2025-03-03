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

import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;

/**
 * Event used when the application layout has changed
 */
public class DockingLayoutEvent {
    public enum ID {
        /**
         * Layout has been added
         */
        ADDED,
        /**
         * Layout has been removed
         */
        REMOVED,
        /**
         * Layout has been restored
         */
        RESTORED,
        /**
         * Layout has been saved to a file
         */
        PERSISTED
    }

    private final ID id;
    private final String layoutName;
    private final ApplicationLayout layout;

    public DockingLayoutEvent(ID id, String layoutName, ApplicationLayout layout) {
        this.id = id;
        this.layoutName = layoutName;
        this.layout = layout;
    }

    public ID getID() {
        return id;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public ApplicationLayout getLayout() {
        return layout;
    }
}
