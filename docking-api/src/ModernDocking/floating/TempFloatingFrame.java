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
package ModernDocking.floating;

import ModernDocking.internal.DockableWrapper;
import ModernDocking.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * this is a frame used temporarily when floating a panel
 */
public class TempFloatingFrame extends JFrame {
	private static final int BORDER_SIZE = 2;
	private final List<DockableWrapper> dockables;
	private final int selectedIndex;

	public TempFloatingFrame(DockableWrapper dockable, JComponent dragSrc, Dimension size) {
		dockables = Collections.emptyList();
		selectedIndex = 0;

		build(dockable.getDisplayPanel(), dragSrc, size);
	}

	public TempFloatingFrame(List<DockableWrapper> dockables, int selectedIndex, JComponent dragSrc, Dimension size) {
		this.dockables = dockables;
		this.selectedIndex = selectedIndex;

		JTabbedPane tabs = new JTabbedPane();

		// we only support tabs on top if we have FlatLaf because we can add a trailing component for our menu
		boolean usingFlatLaf = tabs.getUI().getClass().getPackageName().startsWith("com.formdev.flatlaf");

		if (Settings.alwaysDisplayTabsMode() && usingFlatLaf) {
			tabs.setTabPlacement(JTabbedPane.TOP);
		}
		else {
			tabs.setTabPlacement(JTabbedPane.BOTTOM);
		}

		for (DockableWrapper dockable : dockables) {
			tabs.add(dockable.getDockable().getTabText(), dockable.getDisplayPanel());
		}
		tabs.setSelectedIndex(selectedIndex);

		build(tabs, dragSrc, size);
	}

	/**
	 * Create a new temporary floating frame to contain a dockable that has started to float
	 *
	 * @param dockable Dockable in the floating frame
	 * @param dragSrc The source of the drag
	 */
	private void build(JComponent dockable, JComponent dragSrc, Dimension size) {
		setLayout(new BorderLayout()); // keep it simple, just use border layout
		setUndecorated(true); // hide the frame
		setType(Type.UTILITY); // keeps the frame from appearing in the task bar frames
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); //  this frame is only showing while moving

		// size the frame to the dockable size
		setSize(size);

		// set the frame position to match the current dockable position
		Point newPoint = new Point(dragSrc.getLocation());

		// when dragging from a tab there is no parent for the drag source
		if (dragSrc.getParent() != null) {
			SwingUtilities.convertPointToScreen(newPoint, dragSrc.getParent());
		}

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
		panel.add(dockable, gbc);

		add(panel, BorderLayout.CENTER);

		setVisible(true);
	}

	public List<DockableWrapper> getDockables() {
		return dockables;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}
}
