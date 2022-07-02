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

import util.RotatedIcon;
import util.TextIcon;
import util.UnselectableButtonGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DockableToolbar extends JPanel implements ComponentListener {
	public enum Location {
		WEST,
		SOUTH,
		EAST
	}

	private final JFrame frame;
	private final RootDockingPanel root;
	private final Location location;

	private static class Entry {
		private final Dockable dockable;
		private final JToggleButton button;
		private final DockedUnpinnedPanel panel;

		public Entry(Dockable dockable, JToggleButton button, DockedUnpinnedPanel panel) {
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
	private final UnselectableButtonGroup buttonGroup = new UnselectableButtonGroup();

	public DockableToolbar(JFrame frame, RootDockingPanel root, Location location) {
		super(new GridBagLayout());
		this.frame = frame;
		this.root = root;
		this.location = location;

		addComponentListener(this);
	}

	public Location getDockedLocation() {
		return location;
	}

	public boolean isVertical() {
		return location == Location.EAST || location == Location.WEST;
	}

	private void createContents() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;

		for (Entry dockable : dockables) {
			add(dockable.button, gbc);

			if (isVertical()) {
				gbc.gridy++;
			}
			else {
				gbc.gridx++;
			}
		}

		if (isVertical()) {
			gbc.weighty = 1.0;
		}
		else {
			gbc.weightx = 1.0;
		}
		add(new JLabel(""), gbc);
	}

	private void updateButtons() {
		for (Entry entry : dockables) {
			// set only a single panel visible
			entry.panel.setVisible(buttonGroup.getSelection() == entry.button.getModel());
		}
	}

	public void addDockable(Dockable dockable) {
		if (!hasDockable(dockable)) {
			DockableWrapper wrapper = Docking.getWrapper(dockable);

			JToggleButton button = new JToggleButton();

			if (isVertical()) {
				TextIcon textIcon = new TextIcon(button, dockable.tabText(), TextIcon.Layout.HORIZONTAL);
				RotatedIcon rotatedIcon = new RotatedIcon(textIcon, location == Location.WEST ? RotatedIcon.Rotate.UP : RotatedIcon.Rotate.DOWN);
				button.setIcon(rotatedIcon);

				Insets insets = UIManager.getInsets("Button.margin");
				// purposefully putting them in this order to set the margins of a vertical button
				//noinspection SuspiciousNameCombination
				Insets margin = new Insets(insets.left, insets.top, insets.left, insets.top);
				button.setMargin(margin);
			}
			else {
				button.setText(dockable.tabText());
			}

			DockedUnpinnedPanel panel = new DockedUnpinnedPanel(dockable, root, this);

			wrapper.setFrame(frame);

			// update all the buttons and panels
			button.addActionListener(e -> updateButtons());

			buttonGroup.add(button);

			dockables.add(new Entry(dockable, button, panel));

			JLayeredPane layeredPane = frame.getLayeredPane();

			layeredPane.add(panel, root.getPinningLayer());

			createContents();
		}
	}

	public void removeDockable(Dockable dockable) {
		for (Entry entry : dockables) {
			if (entry.dockable == dockable) {
				JLayeredPane layeredPane = frame.getLayeredPane();

				layeredPane.remove(entry.panel);
				break;
			}
		}
		if (dockables.removeIf(panel -> panel.dockable.equals(dockable))) {
			createContents();
		}
	}

	public boolean hasDockable(Dockable dockable) {
		return dockables.stream()
				.anyMatch(panel -> panel.dockable.equals(dockable));
	}

	public boolean shouldDisplay() {
		return dockables.size() > 0;
	}

	public void hideAll() {
		buttonGroup.setSelected(buttonGroup.getSelection(), false);

		updateButtons();
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
		dockables.forEach(dockable -> dockable.panel.componentResized(e));
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
