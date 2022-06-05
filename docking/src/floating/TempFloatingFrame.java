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

import javax.swing.*;
import java.awt.*;

// this is a frame used temporarily when floating a panel
public class TempFloatingFrame extends JFrame {
	public TempFloatingFrame(Dockable dockable, JComponent dragSrc, Point mouseDragPos) {
		setLayout(new BorderLayout());

		setSize(((JComponent) dockable).getSize());

		Point newPoint = new Point(mouseDragPos);
		SwingUtilities.convertPointToScreen(newPoint, dragSrc);

		newPoint.x -= mouseDragPos.x;
		newPoint.y -= mouseDragPos.y;

		setLocation(newPoint);

		add((JComponent) dockable, BorderLayout.CENTER);

		setUndecorated(true);

		setVisible(true);
	}
}
