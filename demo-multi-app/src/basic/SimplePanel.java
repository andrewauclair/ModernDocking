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

import ModernDocking.api.DockingAPI;
import ModernDocking.DockingProperty;
import ModernDocking.ui.DefaultHeaderUI;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class SimplePanel extends BasePanel {
	private String tabText = "";

	public boolean limitToWindow = false;

	private Color backgroundColor = null;
	private Color foregroundColor = null;

	@DockingProperty(name = "test", defaultValue = "one")
	private String test;

	@DockingProperty(name = "test1", defaultValue = "two")
	private String test1;
	@DockingProperty(name = "test2", defaultValue = "three")
	private String test2;
	@DockingProperty(name = "test3", defaultValue = "four")
	private String test3;
	@DockingProperty(name = "test4", defaultValue = "five")
	private String test4;
	@DockingProperty(name = "test5", defaultValue = "six")
	private String test5;
	@DockingProperty(name = "test6", defaultValue = "seven")
	private String test6;
	@DockingProperty(name = "test7", defaultValue = "eight")
	private String test7;
	@DockingProperty(name = "test8", defaultValue = "nine")
	private String test8;
	@DockingProperty(name = "test9", defaultValue = "ten")
	private String test9;
	@DockingProperty(name = "test10", defaultValue = "eleven")
	private String test10;
	@DockingProperty(name = "test11", defaultValue = "twelve")
	private String test11;

	@DockingProperty(name = "test_int_1", defaultValue = "5")
	private int test_int_1;

	private static final Random rand = new Random();

	public SimplePanel(DockingAPI docking, String title, String persistentID) {
		super(docking, title, persistentID);
		tabText = title;

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		int numberOfControls = rand.nextInt(15);

		for (int i = 0; i < numberOfControls; i++) {
			int randControl = rand.nextInt(4);

			switch (randControl) {
				case 0:
					add(new JLabel("Label Here"), gbc);
					break;
				case 1:
					add(new JTextField(), gbc);
					break;
				case 2:
					add(new JCheckBox(), gbc);
					break;
				case 3:
					add(new JRadioButton(), gbc);
					break;
			}

			if (rand.nextBoolean()) {
				gbc.gridy++;
			}
			else {
				gbc.gridx++;
			}
		}
	}

	public void setTitleBackground(Color color) {
		this.backgroundColor = color;
	}

	public void setTitleForeground(Color color) {
		this.foregroundColor = color;
	}

	@Override
	public DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) {
			return new DefaultHeaderUI(headerController, headerModel) {
				@Override
				public void setBackground(Color bg) {
					if (backgroundColor != null) {
						super.setBackground(backgroundColor);
					} else {
						super.setBackground(bg);
					}
				}

				@Override
				public void setForeground(Color bg) {
					if (foregroundColor != null) {
						super.setForeground(foregroundColor);
					} else {
						super.setForeground(bg);
					}
				}
			};
		}
		else {
			return new DefaultHeaderUI(headerController, headerModel) {
				@Override
				public void setBackground(Color bg) {
					if (backgroundColor != null) {
						super.setBackground(backgroundColor);
					}
					else {
						super.setBackground(bg);
					}
				}

				@Override
				public void setForeground(Color bg) {
					if (foregroundColor != null) {
						super.setForeground(foregroundColor);
					}
					else {
						super.setForeground(bg);
					}
				}
			};
		}
	}

	@Override
	public int getType() {
		return 1;
	}

	@Override
	public boolean isFloatingAllowed() {
		return true;
	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public boolean isMinMaxAllowed() {
		return true;
	}

	@Override
	public boolean isLimitedToWindow() {
		return limitToWindow;
	}

	@Override
	public String getTabText() {
		if (tabText == null || tabText.isEmpty()) {
			return super.getTabText();
		}
		return tabText;
	}

	@Override
	public String getTabTooltip() {
		return tabText;
	}

	public void setTabText(String tabText) {
		Objects.requireNonNull(tabText);
		this.tabText = tabText;
	}

	@Override
	public void updateProperties() {
		System.out.println("Set props for " + getPersistentID());
		System.out.println(test);
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);
		System.out.println(test4);
		System.out.println(test5);
		System.out.println(test6);
		System.out.println(test7);
		System.out.println(test8);
		System.out.println(test9);
		System.out.println(test10);
		System.out.println(test11);
		System.out.println(test_int_1);
	}

//	@Override
//	public void setProperties(Map<String, String> properties) {
//		System.out.println("Set props for " + getPersistentID());
//	}
}
