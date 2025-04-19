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
package io.github.andrewauclair.moderndocking.ui;

import java.awt.Color;
import javax.swing.JButton;

/**
 * Interface used for defining Docking header UIs that can be updated
 */
public interface DockingHeaderUI {
	/**
	 * Update the UI
	 */
	void update();

	/**
	 * Display the settings menu
	 *
	 * @param settings Settings button to display menu at
	 */
	void displaySettingsMenu(JButton settings);

	/**
	 * Set the background color override of the header UI
	 *
	 * @param color Background color. null to clear override
	 */
	void setBackgroundOverride(Color color);

	/**
	 * Set the foreground color override of the header UI
	 *
	 * @param color Foreground color. null to clear override
	 */
	void setForegroundOverride(Color color);
}
