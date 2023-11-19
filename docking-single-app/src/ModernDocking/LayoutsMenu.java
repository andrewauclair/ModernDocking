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

import ModernDocking.event.DockingLayoutEvent;
import ModernDocking.event.DockingLayoutListener;
import ModernDocking.layouts.DockingLayouts;

import javax.swing.*;

/**
 * Custom JMenu that displays all the layouts in DockingLayouts as menu items
 */
public class LayoutsMenu extends JMenu implements DockingLayoutListener {
	/**
	 * Create a new layouts menu. Add a listener for when layouts change.
	 */
	public LayoutsMenu() {
		super("Layouts");

		DockingLayouts.addLayoutsListener(this);

		rebuildOptions();
	}
	private void rebuildOptions() {
		removeAll();

		for (String name : DockingLayouts.getLayoutNames()) {
			JMenuItem item = new JMenuItem(name);
			item.addActionListener(e -> DockingState.restoreApplicationLayout(DockingLayouts.getLayout(name)));

			add(item);
		}
	}

	@Override
	public void layoutChange(DockingLayoutEvent e) {
		rebuildOptions();
	}
}
