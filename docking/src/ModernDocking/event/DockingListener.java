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
package ModernDocking.event;

/**
 * Listener interface used to listen for docking events
 */
public interface DockingListener {
	/**
	 * dockable has been docked
	 *
	 * @param persistentID Persistent ID of the dockable
	 */
	void onDocked(String persistentID);

	/**
	 * Dockable has been undocked
	 *
	 * @param persistentID Persistent ID of the dockable
	 */
	void onUndocked(String persistentID);

	/**
	 * dockable has been unpinned
	 *
	 * @param persistentID Persistent ID of the dockable
	 */
	void onUnpinned(String persistentID);

	/**
	 * dockable has been shown
	 *
	 * @param persistentID Persistent ID of the dockable
	 */
	void onShown(String persistentID);

	/**
	 * dockable has been hidden
	 *
	 * @param persistentID Persistent ID of the dockable
	 */
	void onHidden(String persistentID);
}
