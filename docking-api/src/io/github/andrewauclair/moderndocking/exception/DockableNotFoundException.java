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

/**
 * This exception is thrown when the docking framework tries to lookup a dockable and fails
 */
public class DockableNotFoundException extends RuntimeException {
	/**
	 * The persistent ID that we were unable to find a dockable registered with
	 */
	private final String persistentID;

	/**
	 * Create a new DockableNotFoundException
	 *
	 * @param persistentID The persistentID of the non-existent dockable
	 */
	public DockableNotFoundException(String persistentID) {
		super("Dockable with persistent ID '" + persistentID + "' not found.");
		this.persistentID = persistentID;
	}

	/**
	 * Retrieve the persistentID of the dockable that was not found
	 * @return The persistentID of the non-existent dockable
	 */
	public String getPersistentID() {
		return persistentID;
	}
}
