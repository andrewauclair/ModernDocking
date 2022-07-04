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
package modern_docking;

import javax.swing.*;
import java.util.List;

public interface Dockable {
	// provide the drag source to the docking framework
	// this is usually a title bar JPanel
	// return null if the dockable should not be relocatable
	JComponent dragSource();

	// provide the persistent ID to the docking framework
	// this should be unique in the application (will be verified when adding dockable)
	String persistentID();

	// provide the tab text to the docking framework
	// the tab text to be displayed when Dockable is in a tabbed pane. Does not need to be unique
	// NOTE: this text should be static. If it needs to change, then the Dockable needs to be undocked and docked again.
	String tabText();

	// indicates that this dockable is allowed to be floated as its own new window.
	// if floating is not allowed and an attempt is made to float the dockable, it will be returned to where it was undocked.
	// Note that this is independent of limitToRoot(). Returning false for floatingAllowed() and false for limitToRoot() will still
	//    allow the dockable to be moved between roots, but it can't be used to start a new floating root.
	boolean floatingAllowed();

	// force the dockable to remain in the root it started in.
	// this is useful for having a new floating frame with many dockables that are only allowed in that one frame.
	boolean limitToRoot();

	// list of regions where the dockable is not allowed to be docked.
	// often used to prevent "vertical" style dockables from being dockabled horizontally (i.e. disallow "North" and "South" regions)
	List<DockingRegion> disallowedRegions();

	// helper function to determine if the header close button should be displayed
	boolean allowClose();

	// helper function to determine if the header pin button should be displayed
	// NOTE: this is a suggestion. If the parent frame of the dockable does not support pinning then the button will be hidden regardless
	boolean allowPinning();

	// helper function to determine if the header min/max button should be displayed
	boolean allowMinMax();

	// helper function to determine if the header 'more' button should be displayed
	// NOTE: allowPinning() = true results in more options regardless of this return value
	boolean hasMoreOptions();

	// add the more options to the popup menu. defaults to an empty block to handle the case of hasMoreOptions() = false
	default void addMoreOptions(JPopupMenu menu) {
	}
}
