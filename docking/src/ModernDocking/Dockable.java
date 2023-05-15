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
package ModernDocking;

import ModernDocking.ui.DefaultHeaderUI;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;

// this is the main interface for a Dockable. Any panel that an application wishes to be dockable must implement
// this interface and provide the appropriate values.

// along with implementing this interface, the application will need to call Docking.registerDockable and Docking.dock
// to use the dockable.
public interface Dockable {
	// provide the persistent ID to the docking framework
	// this should be unique in the application (will be verified when adding dockable)
	String getPersistentID();

	// tells the docking framework what type of dockable this is
	// provided as a simple int, but it's best to create your own enum and return the ordinal
	int getType();

	// provide the tab text to the docking framework
	// the tab text to be displayed when Dockable is in a tabbed pane. Does not need to be unique
	// NOTE: this text should be static. If it needs to change, then the Dockable needs to be undocked and docked again.
	String getTabText();

	// provide the dockable icon to the docking framework
	// used in the default header UI, when displayed on in a JTabbedPane and when unpinned
	default Icon getIcon() {
		return null;
	}

	// indicates that this dockable is allowed to be floated as its own new window.
	// if floating is not allowed and an attempt is made to float the dockable, it will be returned to where it was undocked.
	// Note that this is independent of limitToRoot(). Returning false for floatingAllowed() and false for limitToRoot() will still
	//    allow the dockable to be moved between roots, but it can't be used to start a new floating root.
	boolean isFloatingAllowed();

	// force the dockable to remain in the root it started in.
	// this is useful for having a new floating frame with many dockables that are only allowed in that one frame.
	default boolean shouldLimitToRoot() {
		return false;
	}

	// style of the dockable. vertical will disallow the east and west regions. horizontal will disallow the north and south regions.
	// both will allow all 4 regions
	default DockableStyle getStyle() {
		return DockableStyle.BOTH;
	}

	// helper function to determine if the header close option should be enabled
	boolean canBeClosed();

	// helper function to determine if the header pin option should be enabled
	// NOTE: this is a suggestion. If the parent frame of the dockable does not support pinning then the button will be hidden regardless.
	// pinning is supported on all Modern Docking FloatingFrames and can be enabled for other frames with configurePinning in Docking
	default boolean allowPinning() {
		return false;
	}

	// helper function to determine if the header min/max option should be enabled
	default boolean allowMinMax() {
		return false;
	}

	// helper function to determine if the header 'more' option should be enabled
	// NOTE: allowPinning() = true results in more options regardless of this return value
	default boolean hasMoreOptions() {
		return false;
	}

	// add the more options to the popup menu. defaults to an empty block to handle the case of hasMoreOptions() = false
	default void addMoreOptions(JPopupMenu menu) {
	}

	// create the header for the panel. default action is to create an instance of DefaultHeaderUI.
	// this can be replaced using the docking-ui package and the FlatLafHeaderUI
	default DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return new DefaultHeaderUI(headerController, headerModel);
	}

	default DockingStrategy strategy() {
		return null;
	}

	// called when the Docking framework is about to dock this Dockable. Allows the action to be overridden.
	// return true if the docking action has been performed separately.
	default boolean onDocking() {
		return false;
	}

	// called after the Dockable has been docked
	default void onDocked() {
	}

	// called after the Dockable has been undocked
	default void onUndocked() {
	}
}
