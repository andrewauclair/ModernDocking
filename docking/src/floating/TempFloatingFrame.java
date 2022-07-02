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
package floating;

import docking.Dockable;
import docking.Docking;

import javax.swing.*;
import java.awt.*;

// this is a frame used temporarily when floating a panel
public class TempFloatingFrame extends JFrame {
	private static final int BORDER_SIZE = 2;

	public TempFloatingFrame(Dockable dockable, JComponent dragSrc) {
		setLayout(new BorderLayout()); // keep it simple, just use border layout
		setUndecorated(true); // hide the frame
		setType(Type.UTILITY); // keeps the frame from appearing in the task bar frames
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); //  this frame is only showing while moving

		// size the frame to the dockable size
		Dimension size = ((JComponent) dockable).getSize();
		size.width += Docking.frameBorderSizes.right;
		size.height += Docking.frameBorderSizes.bottom;
		setSize(size);

		// set the frame position to match the current dockable position
		Point newPoint = new Point(dragSrc.getLocation());
		SwingUtilities.convertPointToScreen(newPoint, dragSrc.getParent());

		setLocation(newPoint);

		// put the dockable in a panel with a border around it to make it look better
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		// set a border around the panel in the component focus color. this lets us distinguish the dockable panel from other windows.
		Color color = UIManager.getColor("Component.focusColor");
		panel.setBorder(BorderFactory.createLineBorder(color, BORDER_SIZE));
		panel.add((Component) dockable, gbc);

		add(panel, BorderLayout.CENTER);

		setVisible(true);
	}
}
