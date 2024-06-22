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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.api.DockingAPI;
import ModernDocking.api.RootDockingPanelAPI;
import ModernDocking.floating.TempFloatingFrame;

import javax.swing.*;
import java.awt.*;

public class FloatingFrame extends JFrame {
	private final DockingAPI docking;

	public FloatingFrame(DockingAPI docking) {
		this.docking = docking;
		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// create and add the root
		RootDockingPanelAPI root = new RootDockingPanelAPI(docking, this){};
		root.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		add(root, BorderLayout.CENTER);

		// allow pinning for this frame
		docking.configureAutoHide(this, JLayeredPane.MODAL_LAYER, true);

		setVisible(true);

		pack();
	}

	public FloatingFrame(DockingAPI docking, Point location, Dimension size, int state) {
		this.docking = docking;
		setLocation(location);
		setSize(size);
		setExtendedState(state);

		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// create and add the root
		RootDockingPanelAPI root = new RootDockingPanelAPI(docking, this){};
		root.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		add(root, BorderLayout.CENTER);

		// allow pinning for this frame
		docking.configureAutoHide(this, JLayeredPane.MODAL_LAYER, true);

		setVisible(true);

	}

	// create a new floating frame. this is used when calling Docking.newWindow or when restoring the layout from a file
	public FloatingFrame(DockingAPI docking, Dockable dockable, Point location, Dimension size, int state) {
		this.docking = docking;

		DisplayPanel displayPanel = DockingInternal.get(docking).getWrapper(dockable).getDisplayPanel();

		Point point = displayPanel.getLocation();
		SwingUtilities.convertPointToScreen(point, displayPanel.getParent());

		setLocation(location);
		setSize(size);
		setExtendedState(state);

		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// create and add the root
		RootDockingPanelAPI root = new RootDockingPanelAPI(docking, this){};
		root.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		add(root, BorderLayout.CENTER);

		// allow pinning for this frame
		docking.configureAutoHide(this, JLayeredPane.MODAL_LAYER, true);

		setVisible(true);

		finalizeSize(dockable, location, size);
	}

	// create a floating frame from a temporary frame as a result of docking
	public FloatingFrame(DockingAPI docking, Dockable dockable, TempFloatingFrame floatingFrame) {
		this.docking = docking;
		setLayout(new BorderLayout());

		// size the frame to the dockable size + the border size of the frame
		Dimension size = DockingInternal.get(docking).getWrapper(dockable).getDisplayPanel().getSize();

		setSize(size);

		// dispose this frame when it closes
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// set the location of this frame to the floating frame location
		setLocation(floatingFrame.getLocation());

		// create and add the root
		RootDockingPanelAPI root = new RootDockingPanelAPI(docking, this){};
		add(root, BorderLayout.CENTER);

		// allow pinning on this frame
		docking.configureAutoHide(this, JLayeredPane.MODAL_LAYER, true);

		// finally, dock the dockable and show this frame
		docking.dock(dockable, this);

		setVisible(true);

		Point onScreenPoint = floatingFrame.getLocation();
		Dimension onScreenSize = floatingFrame.getSize();

		finalizeSize(dockable, onScreenPoint, onScreenSize);
	}

	private void finalizeSize(Dockable dockable, Point onScreenPoint, Dimension onScreenSize) {
		SwingUtilities.invokeLater(() -> {
			// adjust the floating frame such that the dockable is in the correct location
			DisplayPanel displayPanel = DockingInternal.get(docking).getWrapper(dockable).getDisplayPanel();


			Point point = displayPanel.getLocation();
			SwingUtilities.convertPointToScreen(point, displayPanel.getParent());

			Point finalPoint = new Point(this.getX() - (point.x - onScreenPoint.x), FloatingFrame.this.getY() - (point.y - onScreenPoint.y));

			// make sure we keep the new frame on the screen
			finalPoint.y = Math.max(0, finalPoint.y);

			setLocation(finalPoint);

			Dimension currentPanelSize = displayPanel.getSize();
			Dimension currentFrameSize = getSize();

			Dimension newSize = new Dimension(currentFrameSize.width - currentPanelSize.width + onScreenSize.width, currentFrameSize.height - currentPanelSize.height + onScreenSize.height);
			setSize(newSize);
		});
	}

	@Override
	public void dispose() {
		// deregister the root panel now that we're disposing this frame
		docking.deregisterDockingPanel(this);

		super.dispose();
	}
}
