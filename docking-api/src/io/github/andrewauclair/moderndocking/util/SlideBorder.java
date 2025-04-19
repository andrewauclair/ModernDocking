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
package io.github.andrewauclair.moderndocking.util;

import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Panel that provides a slider for unpinned dockables when they are displayed
 */
public class SlideBorder extends JPanel {
	/**
	 * The location of the toolbar that this slide border is for. Used to determine the correct orientation of the panel
	 */
	private final ToolbarLocation location;

	/**
	 * Create a SlideBorder with the given toolbar location
	 *
	 * @param location Location of the toolbar
	 */
	public SlideBorder(ToolbarLocation location) {
		this.location = location;
		setCursor(location == ToolbarLocation.SOUTH ? Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) : Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension size = super.getMinimumSize();
		if (location == ToolbarLocation.SOUTH) {
			size.height = 6;
		}
		else {
			size.width = 6;
		}
		return size;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (location == ToolbarLocation.SOUTH) {
			size.height = 6;
		}
		else {
			size.width = 6;
		}
		return size;
	}
}
