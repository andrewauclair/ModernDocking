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

import ModernDocking.internal.DockingInternal;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;

/**
 * this is the main interface for a Dockable. Any panel that an application wishes to be dockable must implement
 * this interface and provide the appropriate values.
 * <p>
 * along with implementing this interface, the application will need to call Docking.registerDockable and Docking.dock
 * to use the dockable.
 */
public interface Dockable {
	/**
	 * provide the persistent ID to the docking framework
	 * this should be unique in the application (will be verified when adding dockable)
	 *
	 * @return Persistent ID of the dockable
	 */
	String getPersistentID();

	/**
	 * Provides the type of this dockable. Uses an int so that the user code can decide if they're just ints
	 * or an enum (using the ordinal values)
	 *
	 * @return Type as an int, user defined
	 */
	default int getType() {
		return 0;
	}

	/**
	 * provide the tab text to the docking framework
	 * the tab text to be displayed when Dockable is in a tabbed pane. Does not need to be unique
	 * <p>
	 * NOTE: If this text changes, you will need to call Dockable.updateTabInfo()
	 *
	 * @return Tab text of the dockable
	 */
	String getTabText();

	/**
	 * provide the tab tooltip to the docking framework
	 * <p>
	 * NOTE: If this text changes, you will need to call Dockable.updateTabInfo()
	 */
	default String getTabTooltip() {
		return null;
	}

	/**
	 * provide the dockable icon to the docking framework
	 * used in the default header UI, when displayed on in a JTabbedPane and when unpinned
	 *
	 * @return Icon of the dockable
	 */
	default Icon getIcon() {
		return null;
	}

	/**
	 * indicates that this dockable is allowed to be floated as its own new window.
	 * if floating is not allowed and an attempt is made to float the dockable, it will be returned to where it was undocked.
	 * <p>
	 * Note that this is independent of limitToRoot(). Returning false for floatingAllowed() and false for limitToRoot() will still
	 *    allow the dockable to be moved between roots, but it can't be used to start a new floating root.
	 *
	 * @return True, if floating is allowed
	 */
	default boolean isFloatingAllowed() {
		return true;
	}

	/**
	 * force the dockable to remain in the root it started in.
	 * this is useful for having a new floating frame with many dockables that are only allowed in that one frame.
	 *
	 * @return Should this dockable be limited to the root it starts in
	 */
	default boolean isLimitedToRoot() {
		return false;
	}

	/**
	 * style of the dockable. vertical will disallow the east and west regions. horizontal will disallow the north and south regions.
	 * both will allow all 4 regions
	 *
	 * @return The style of this dockable
	 */
	default DockableStyle getStyle() {
		return DockableStyle.BOTH;
	}

	/**
	 * pinning style of the dockable. separate from getStyle, which is used for docking. this option allows the dockable to have different
	 * preferences on pinning than it does on docking.
	 *
	 * @return The pinning style of this dockable
	 */
	default DockableStyle getPinningStyle() {
		return DockableStyle.BOTH;
	}

	/**
	 * helper function to determine if the header close option should be enabled
	 *
	 * @return Can this dockable be closed?
	 */
	default boolean isClosable() {
		return true;
	}

	/**
	 * helper function to determine if the header pin option should be enabled
	 * NOTE: this is a suggestion. If the parent frame of the dockable does not support pinning then the button will be hidden regardless.
	 * pinning is supported on all Modern Docking FloatingFrames and can be enabled for other frames with configurePinning in Docking
	 *
	 * @return True if pinning is allowed
	 */
	default boolean isPinningAllowed() {
		return false;
	}

	/**
	 * helper function to determine if the header min/max option should be enabled
	 *
	 * @return Is min/max allowed for this dockable
	 */
	default boolean isMinMaxAllowed() {
		return false;
	}

	/**
	 * Dockables can be displayed with less room than they need. By default, this interface will tell the docking framework
	 * to wrap the Dockable in a JScrollPane. To disable this functionality, override this method and return false.
	 *
	 * @return True if this dockable should be wrapped in a JScrollPane internally in the docking framework when displayed
	 */
	default boolean isWrappableInScrollpane() {
		return true;
	}

	/**
	 * helper function to determine if the header 'more' option should be enabled
	 * NOTE: allowPinning() = true results in more options regardless of this return value
	 *
	 * @return True if there are more options to display on the context menu
	 */
	default boolean getHasMoreOptions() {
		return false;
	}

	default int getTabPosition() {
		return SwingConstants.BOTTOM;
	}

	/**
	 * add the more options to the popup menu. defaults to an empty block to handle the case of hasMoreOptions() = false
	 *
	 * @param menu The JPopupMenu to add options to
	 */
	default void addMoreOptions(JPopupMenu menu) {
	}

	/**
	 * create the header for the panel. By default, the framework will create a DefaultHeaderUI for non-flatlaf
	 *
	 * @param headerController Header controller for this dockable
	 * @param headerModel Header model for this dockable
	 * @return A new header UI that uses the provided controller and model
	 */
	default DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return DockingInternal.createDefaultHeaderUI(headerController, headerModel);
	}

	/**
	 * The member variables with the DockingProperty annotation have been updated from the layout file
	 */
	default void updateProperties() {
	}
}
