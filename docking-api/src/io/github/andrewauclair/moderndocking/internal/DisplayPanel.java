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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.settings.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * The internal JPanel used to display the Dockable plus its header
 */
public class DisplayPanel extends JPanel {
	/**
	 * Dockable contained in this display panel
	 */
	private final DockableWrapper wrapper;

	/**
	 * Create a new internal display panel for the dockable
	 *
	 * @param wrapper Wrapper for the dockable that this panel will represent
	 */
	public DisplayPanel(DockableWrapper wrapper) {
		this.wrapper = wrapper;

		setLayout(new GridBagLayout());

		buildUI();
	}

	private void buildUI() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		if (!Settings.alwaysDisplayTabsMode() || wrapper.isHidden()) {
			if (!(wrapper.getParent() instanceof DockedTabbedPanel) || ((DockedTabbedPanel) wrapper.getParent()).isUsingBottomTabs()) {
				add((Component) wrapper.getHeaderUI(), gbc);
				gbc.gridy++;
			}
		}

		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		if (wrapper.getDockable().isWrappableInScrollpane()) {
			add(new JScrollPane((Component) wrapper.getDockable()), gbc);
		}
		else {
			add((Component) wrapper.getDockable(), gbc);
		}
	}

	/**
	 * Get the wrapper used in this panel
	 *
	 * @return Wrapper for this panel
	 */
	public DockableWrapper getWrapper() {
		return wrapper;
	}

	public void parentChanged() {
		buildUI();
	}
}
