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
import modern_docking.DockingRegion;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class ToolPanel extends BasePanel {
	private final boolean vertical;

	public ToolPanel(String title, String persistentID, boolean vertical) {
		super(title, persistentID);
		this.vertical = vertical;
	}

	@Override
	public boolean floatingAllowed() {
		return false;
	}

	@Override
	public boolean limitToRoot() {
		return false;
	}

	@Override
	public List<DockingRegion> disallowedRegions() {
		return vertical ? Arrays.asList(DockingRegion.NORTH, DockingRegion.SOUTH) : Arrays.asList(DockingRegion.WEST, DockingRegion.EAST);
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
}
