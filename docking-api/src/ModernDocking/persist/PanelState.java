/*
Copyright (c) 2022 Andrew Auclair

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
package ModernDocking.persist;

/**
 * DockableState that stores a single simple panel
 */
@Deprecated(since = "0.12.1", forRemoval = true)
public class PanelState implements DockableState {
	private final String persistentID;
	private final String className;

	/**
	 * Create a new PanelState
	 *
	 * @param persistentID ID
	 */
	public PanelState(String persistentID, String className) {
		this.persistentID = persistentID;
		this.className = className;
	}

	/**
	 * Get ID
	 * @return Persistent ID
	 */
	public String getPersistentID() {
		return persistentID;
	}

	public String getClassName() {
		return className;
	}
}
