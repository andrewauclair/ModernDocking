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
import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.DockingStrategy;

import javax.swing.*;

public class ToolPanel extends BasePanel {
	private final DockableStyle style;
	private final Icon icon;

	public boolean limitToRoot = false;

	public ToolPanel(String title, String persistentID, DockableStyle style) {
		super(title, persistentID);
		this.style = style;
		this.icon = null;
	}

	public ToolPanel(String title, String persistentID, DockableStyle style, Icon icon) {
		super(title, persistentID);
		this.style = style;
		this.icon = icon;


	}

	@Override
	public int type() {
		return 0;
	}

	@Override
	public Icon icon() {
		return icon;
	}

	@Override
	public boolean floatingAllowed() {
		return false;
	}

	@Override
	public boolean limitToRoot() {
		return limitToRoot;
	}

	@Override
	public DockableStyle style() {
		return style;
	}

	@Override
	public boolean allowClose() {
		return true;
	}

	@Override
	public boolean allowPinning() {
		return true;
	}

	@Override
	public boolean allowMinMax() {
		return false;
	}

	@Override
	public boolean hasMoreOptions() {
		return true;
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		menu.add(new JMenuItem("Something"));
		menu.add(new JMenuItem("Else"));
	}

	@Override
	public DockingStrategy strategy() {
		return () -> {
			if (style == DockableStyle.VERTICAL) {
				Docking.dock(this, Docking.getInstance().getMainWindow(), DockingRegion.EAST);
			}
			else if (style == DockableStyle.HORIZONTAL) {
				Docking.dock(this, Docking.getInstance().getMainWindow(), DockingRegion.SOUTH);
			}
			else {
				Docking.dock(this, Docking.getInstance().getMainWindow(), DockingRegion.NORTH);
			}
		};
	}
}
