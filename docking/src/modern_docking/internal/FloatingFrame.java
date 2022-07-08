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
package modern_docking.internal;

import modern_docking.Dockable;
import modern_docking.Docking;
import modern_docking.RootDockingPanel;
import modern_docking.floating.TempFloatingFrame;

import javax.swing.*;
import java.awt.*;

import static modern_docking.internal.DockingInternal.getWrapper;

public class FloatingFrame extends JFrame {
	// create a new floating frame. this is used when calling Docking.newWindow or when restoring the layout from a file
	public FloatingFrame(Point location, Dimension size, int state) {
		setLocation(location);
		setSize(size);
		setExtendedState(state);

		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// create, add and register the root
		RootDockingPanel root = new RootDockingPanel(this);
		add(root, BorderLayout.CENTER);

		Docking.registerDockingPanel(root, this);

		// allow pinning for this frame
		Docking.configurePinning(this, JLayeredPane.MODAL_LAYER, true);

		setVisible(true);
	}

	// create a floating frame from a temporary frame as a result of docking
	public FloatingFrame(Dockable dockable, TempFloatingFrame floatingFrame) {
		setLayout(new BorderLayout());

		// size the frame to the dockable size + the border size of the frame
		Dimension size = getWrapper(dockable).getDisplayPanel().getSize();

		size.width += Docking.frameBorderSizes.left + Docking.frameBorderSizes.right;
		size.height += Docking.frameBorderSizes.top + Docking.frameBorderSizes.bottom;

		setSize(size);

		// dispose this frame when it closes
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// set the location of this frame to the floating frame location - the frame border size
		// do this because the undecorated frame is a different size
		Point location = floatingFrame.getLocation();
		location.x -= Docking.frameBorderSizes.left;
		location.y -= Docking.frameBorderSizes.top;

		setLocation(location);

		// create, add and register the root
		RootDockingPanel root = new RootDockingPanel(this);
		add(root, BorderLayout.CENTER);

		Docking.registerDockingPanel(root, this);

		// allow pinning on this frame
		Docking.configurePinning(this, JLayeredPane.MODAL_LAYER, true);

		// finally, dock the dockable and show this frame
		Docking.dock(dockable, this);

		setVisible(true);
	}

	@Override
	public void dispose() {
		// deregister the root panel now that we're disposing this frame
		Docking.deregisterDockingPanel(this);

		super.dispose();
	}
}
