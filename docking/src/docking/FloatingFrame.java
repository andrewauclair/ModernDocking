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
package docking;

import floating.TempFloatingFrame;

import javax.swing.*;
import java.awt.*;

public class FloatingFrame extends JFrame {
	public FloatingFrame(Dockable dockable, TempFloatingFrame floatingFrame) {
		setLayout(new BorderLayout());
		setSize(((JComponent) dockable).getSize());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Point location = floatingFrame.getLocation();
		location.x -= Docking.frameBorderSize.width;
		location.y -= Docking.frameBorderSize.height;

		setLocation(location);

		RootDockingPanel root = new RootDockingPanel();
		add(root, BorderLayout.CENTER);

		Docking.registerDockingPanel(root, this);

		Docking.dock(this, dockable);

		setVisible(true);
	}

	@Override
	public void dispose() {
		// deregister the root panel now that we're disposing this frame
		Docking.deregisterDockingPanel(this);

		super.dispose();
	}
}
