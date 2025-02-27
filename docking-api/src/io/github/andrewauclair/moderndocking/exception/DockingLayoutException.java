/*
Copyright (c) 2023-2024 Andrew Auclair

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

import java.io.File;

/**
 * Exception wrapper for exceptions encountered while dealing loading or saving docking layouts
 */
public class DockingLayoutException extends Exception {
    public enum FailureType {
        LOAD,
        SAVE
    }
    private final File file;
    private final FailureType failureType;

    /**
     * Create a new instance
     *
     * @param file The layout file that was being saved or loaded
     * @param failureType The state we failed in, loading or saving
     * @param cause The root cause of the exception
     */
    public DockingLayoutException(File file, FailureType failureType, Exception cause) {
        initCause(cause);

        this.file = file;
        this.failureType = failureType;
    }

    /**
     * Retrieve the file being loaded or saved
     *
     * @return File the framework attempted to load or save
     */
    public File getFile() {
        return file;
    }

    /**
     * Retrieve the failure type
     *
     * @return Returns the failure type of this exception
     */
    public FailureType getFailureType() {
        return failureType;
    }
}
