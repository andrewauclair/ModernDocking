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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ColorChanger extends JFrame implements ChangeListener {
	JSpinner red = new JSpinner();
	JSpinner green = new JSpinner();
	JSpinner blue = new JSpinner();
	public ColorChanger() {


		red.addChangeListener(this);
		green.addChangeListener(this);
		blue.addChangeListener(this);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;

		setLayout(new GridBagLayout());

		add(red, gbc);
		gbc.gridy++;

		add(green, gbc);
		gbc.gridy++;

		add(blue, gbc);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == red) {

		}
		else if (e.getSource() == green) {

		}
		else if (e.getSource() == blue) {

		}

		Color color = new Color((Integer) red.getValue(), (Integer) green.getValue(), (Integer) blue.getValue());

		UIManager.put("Docking.titlebar.background", color);

		try {
			LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(lookAndFeel);
		}
		catch (
				UnsupportedLookAndFeelException ex) {
			throw new RuntimeException(ex);
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		}
		catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}
