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
import java.util.HashMap;
import java.util.Map;

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
	int getType();

	enum TabStyle
	{
		TAB_ON_BOTTOM, // default
		TAB_ON_TOP,
		DROP_DOWN
	}

	default TabStyle getTabStyle() {
		return TabStyle.TAB_ON_BOTTOM;
	}

	// provide the tab text to the docking framework
	// the tab text to be displayed when Dockable is in a tabbed pane. Does not need to be unique
	// NOTE: this text should be static. If it needs to change, then the Dockable needs to be undocked and docked again.
	String getTabText();

	/**
	 * provide the dockable icon to the docking framework
	 * used in the default header UI, when displayed on in a JTabbedPane and when unpinned
	 *
	 * @return Icon of the dockable
	 */
	default Icon getIcon() {
		return null;
	}

	//
	//
	//
	//

	/**
	 * indicates that this dockable is allowed to be floated as its own new window.
	 * if floating is not allowed and an attempt is made to float the dockable, it will be returned to where it was undocked.
	 * <p>
	 * Note that this is independent of limitToRoot(). Returning false for floatingAllowed() and false for limitToRoot() will still
	 *    allow the dockable to be moved between roots, but it can't be used to start a new floating root.
	 *
	 * @return True, if floating is allowed
	 */
	boolean isFloatingAllowed();

	/**
	 * force the dockable to remain in the root it started in.
	 * this is useful for having a new floating frame with many dockables that are only allowed in that one frame.
	 *
	 * @return Should this dockable be limited to the root it starts in
	 */
	default boolean shouldLimitToRoot() {
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
	 * helper function to determine if the header close option should be enabled
	 *
	 * @return Can this dockable be closed?
	 */
	boolean canBeClosed();

	/**
	 * helper function to determine if the header pin option should be enabled
	 * NOTE: this is a suggestion. If the parent frame of the dockable does not support pinning then the button will be hidden regardless.
	 * pinning is supported on all Modern Docking FloatingFrames and can be enabled for other frames with configurePinning in Docking
	 *
	 * @return True if pinning is allowed
	 */
	default boolean allowPinning() {
		return false;
	}

	/**
	 * helper function to determine if the header min/max option should be enabled
	 *
	 * @return Is min/max allowed for this dockable
	 */
	default boolean allowMinMax() {
		return false;
	}

	/**
	 * helper function to determine if the header 'more' option should be enabled
	 * NOTE: allowPinning() = true results in more options regardless of this return value
	 *
	 * @return True if there are more options to display on the context menu
	 */
	default boolean hasMoreOptions() {
		return false;
	}

	/**
	 * add the more options to the popup menu. defaults to an empty block to handle the case of hasMoreOptions() = false
	 *
	 * @param menu The JPopupMenu to add options to
	 */
	default void addMoreOptions(JPopupMenu menu) {
	}

	/**
	 * create the header for the panel. default action is to create an instance of DefaultHeaderUI.
	 * this can be replaced using the docking-ui package and the FlatLafHeaderUI
	 *
	 * @param headerController Header controller for this dockable
	 * @param headerModel Header model for this dockable
	 * @return A new header UI that uses the provided controller and model
	 */
	default DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return new DefaultHeaderUI(headerController, headerModel);
	}

	/**
	 * called when the Docking framework is about to dock this Dockable. Allows the action to be overridden.
	 * return true if the docking action has been performed separately.
	 *
	 * @return True if docking was handled separately. False if the framework should proceed with docking
	 */
	default boolean onDocking() {
		return false;
	}

	/**
	 * called after the Dockable has been docked
	 */
	default void onDocked() {
	}

	/**
	 * called after the Dockable has been undocked
	 */
	default void onUndocked() {
	}

	/**
	 * called when the dockable is shown
	 */
	default void shown() {
	}

	/**
	 * called when the dockable is hidden
	 */
	default void hidden() {
	}

	/**
	 * Get the properties of the dockable
	 *
	 * @return map of the dockable properties
	 */
    default Map<String, String> getProperties() {
		return new HashMap<>();
	}

	/**
	 * Set the properties of the dockable
	 *
	 * @param properties map of the dockable properties
	 */
	default void setProperties(Map<String, String> properties) {
	}
}
