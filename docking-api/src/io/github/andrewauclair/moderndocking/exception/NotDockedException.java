/*
Copyright (c) 2022-2024 Andrew Auclair

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

import io.github.andrewauclair.moderndocking.Dockable;

/**
 * Exception that is thrown when a dockable is not already docked
 */
public class NotDockedException extends RuntimeException {
	private final Dockable dockable;

	/**
	 * Create a new exception with the given dockable
	 *
	 * @param message Extra message information to display more detail about the problem
	 * @param dockable Dockable that is not docked
	 */
	public NotDockedException(String message, Dockable dockable) {
		super(message + " because dockable with persistent ID '" + dockable.getPersistentID() + "' is not docked.");
		this.dockable = dockable;
	}

	/**
	 * Retrieve the dockable
	 *
	 * @return The dockable that is not docked
	 */
	public Dockable getDockable() {
		return dockable;
	}
}
