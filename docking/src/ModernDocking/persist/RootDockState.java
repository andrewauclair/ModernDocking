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

import ModernDocking.RootDockingPanel;
import ModernDocking.internal.DockedSimplePanel;
import ModernDocking.internal.DockedSplitPanel;
import ModernDocking.internal.DockedTabbedPanel;

/**
 * Storage for the state of the root
 */
public class RootDockState {
	private final DockableState state;

	/**
	 * Create from a RootDockingPanel
	 *
	 * @param panel root panel
	 */
	public RootDockState(RootDockingPanel panel) {
		if (panel.getPanel() instanceof DockedSimplePanel) {
			state = new PanelState(((DockedSimplePanel) panel.getPanel()).getWrapper().getDockable().getPersistentID());
		}
		else if (panel.getPanel() instanceof DockedSplitPanel) {
			state = new SplitState((DockedSplitPanel) panel.getPanel());
		}
		else if (panel.getPanel() instanceof DockedTabbedPanel) {
			state = new TabState((DockedTabbedPanel) panel.getPanel());
		}
		else {
			throw new RuntimeException("Unknown panel");
		}
	}

	public DockableState getState() {
		return state;
	}
}
