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

import ModernDocking.DockableStyle;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;
import docking.ui.FlatLafHeaderUI;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SimplePanel extends BasePanel {
	private String tabText = "";

	public boolean limitToRoot = false;

	private Color color = Color.WHITE;

	public SimplePanel(String title, String persistentID) {
		super(title, persistentID);
		tabText = "";
	}

	public void setTitleBackground(Color color) {
		this.color = color;
	}

	@Override
	public DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
		return new FlatLafHeaderUI(headerController, headerModel) {
			@Override
			public void setBackground(Color bg) {
				super.setBackground(color);
			}
		};
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
	public DockableStyle getStyle() {
		return DockableStyle.BOTH;
	}

	@Override
	public boolean canBeClosed() {
		return true;
	}

	@Override
	public boolean allowPinning() {
		return false;
	}

	@Override
	public boolean allowMinMax() {
		return true;
	}

	@Override
	public boolean shouldLimitToRoot() {
		return limitToRoot;
	}

	@Override
	public String getTabText() {
		if (tabText == null || tabText.isEmpty()) {
			return super.getTabText();
		}
		return tabText;
	}

	public void setTabText(String tabText) {
		Objects.requireNonNull(tabText);
		this.tabText = tabText;
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> props = new HashMap<>();
		props.put("test", "one");
		props.put("test1", "two");
		props.put("test2", "three");
		props.put("test3", "four");
		props.put("test4", "yak");
		props.put("test5", "six");
		props.put("test6", "seven");
		props.put("test7", "eight");
		props.put("test8", "nine");
		props.put("test9", "ten");
		props.put("test10", "eleven");
		props.put("test11", "twelve");
		return props;
	}
}
