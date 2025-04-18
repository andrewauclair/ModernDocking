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
package basic;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.app.Docking;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public abstract class BasePanel extends JPanel implements Dockable {
	private final String tabText;
	private final String title;
	private final String persistentID;

	public BasePanel(String tabText, String title, String persistentID) {
		super(new BorderLayout());

		this.tabText = tabText;
		this.title = title;
		this.persistentID = persistentID;

		JPanel panel = new JPanel();

		add(panel, BorderLayout.CENTER);

		// the single call to register any docking panel extending this abstract class
		Docking.registerDockable(this);
	}

	@Override
	public String getPersistentID() {
		return persistentID;
	}

	@Override
	public String getTabText() {
		return tabText;
	}

	@Override
	public String getTitleText() {
		return title;
	}
}
