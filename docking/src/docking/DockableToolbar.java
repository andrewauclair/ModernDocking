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
package docking;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DockableToolbar extends JPanel {
	public enum Location {
		WEST,
		SOUTH,
		EAST
	}

	private final RootDockingPanel root;
	private final boolean vertical;

	private static class Entry {
		private final Dockable dockable;
		private final JButton button;
		private final DockedUnpinnedPanel panel;

		public Entry(Dockable dockable, JButton button, DockedUnpinnedPanel panel) {
			this.dockable = dockable;
			this.button = button;
			this.panel = panel;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Entry panel = (Entry) o;
			return Objects.equals(dockable, panel.dockable);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dockable);
		}
	}

	private final List<Entry> dockables = new ArrayList<>();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	public DockableToolbar(RootDockingPanel root, boolean vertical) {
		super(new GridBagLayout());
		this.root = root;
		this.vertical = vertical;
	}

	public boolean isVertical() {
		return vertical;
	}

	private void createContents() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;

		for (Entry dockable : dockables) {
			add(dockable.button, gbc);

			if (vertical) {
				gbc.gridy++;
			}
			else {
				gbc.gridx++;
			}
		}

		if (vertical) {
			gbc.weighty = 1.0;
		}
		else {
			gbc.weightx = 1.0;
		}
		add(new JLabel(""), gbc);
	}

	public void addDockable(Dockable dockable) {
		if (!hasDockable(dockable)) {
			JButton button = new JButton(dockable.tabText());
			DockedUnpinnedPanel panel = new DockedUnpinnedPanel(dockable, root, this);

			button.addActionListener(e -> {
				boolean isSelected = button.isSelected();

				if (isSelected && buttonGroup.getSelection() == button.getModel()) {
					buttonGroup.clearSelection();
				}
				button.setSelected(!isSelected);
				panel.setVisible(!isSelected);
			});

			buttonGroup.add(button);

			dockables.add(new Entry(dockable, button, panel));

			JLayeredPane layeredPane = root.getFrame().getLayeredPane();

			layeredPane.add(panel, root.getPinningLayer());

			createContents();
		}
	}

	public void removeDockable(Dockable dockable) {
		dockables.removeIf(panel -> panel.dockable.equals(dockable));
	}

	public boolean hasDockable(Dockable dockable) {
		return dockables.stream()
				.anyMatch(panel -> panel.dockable.equals(dockable));
	}

	public boolean shouldDisplay() {
		return dockables.size() > 0;
	}
}
