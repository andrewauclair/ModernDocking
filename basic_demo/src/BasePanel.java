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
import modern_docking.Dockable;
import modern_docking.Docking;
import docking.ui.FlatLafHeaderUI;
import modern_docking.ui.DockingHeaderUI;
import modern_docking.ui.HeaderController;
import modern_docking.ui.HeaderModel;

import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel implements Dockable {
	private final String title;
	private final String persistentID;

	public BasePanel(String title, String persistentID) {
		super(new BorderLayout());

		this.title = title;
		this.persistentID = persistentID;

		Docking.registerDockable(this);

		JPanel panel = new JPanel();

		add(panel, BorderLayout.CENTER);
	}

	@Override
	public String persistentID() {
		return persistentID;
	}

	@Override
	public String tabText() {
		return title;
	}

	@Override
	public Icon tabIcon() {
		return null;
	}

	@Override
	public DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return new FlatLafHeaderUI(headerController, headerModel);
	}
}
