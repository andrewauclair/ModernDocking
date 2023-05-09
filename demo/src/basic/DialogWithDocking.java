/*
Copyright (c) 2023 Andrew Auclair

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

import ModernDocking.DockableStyle;
import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;

import javax.swing.*;

public class DialogWithDocking extends JDialog {
	DialogWithDocking() {
		RootDockingPanel root = new RootDockingPanel(this);

		setModalityType(ModalityType.TOOLKIT_MODAL);

		add(root);

		ToolPanel output = new ToolPanel("output", "output-dialog", DockableStyle.HORIZONTAL);
		ToolPanel explorer = new ToolPanel("explorer", "explorer-dialog", DockableStyle.VERTICAL);

		SimplePanel one = new SimplePanel("one", "one-dialog");

		Docking.dock(one, this);
		Docking.dock(explorer, one, DockingRegion.EAST);
		Docking.dock(output, this, DockingRegion.SOUTH);

		setSize(500, 500);
		pack();
	}
}
