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
package ModernDocking.internal;

import ModernDocking.Dockable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;

import static ModernDocking.internal.DockingInternal.getWrapper;

/**
 * This class is responsible for adding a selected border around the dockable at the current mouse position.
 *
 * Using an AWT Event Listener we can listen for global MOUSE_ENTERED and MOUSE_EXITED to add/remove the border.
 */
public class ActiveDockableHighlighter {
	// the current active panel
	private DockingPanel activePanel = null;

	/**
	 * Default constructor to create the highlighter
	 */
	public ActiveDockableHighlighter() {
		// use an AWT event listener to set a border around the dockable that the mouse is currently over
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
			if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_EXITED) {
				DockingPanel dockable = DockingComponentUtils.findDockingPanelAtScreenPos(((MouseEvent) e).getLocationOnScreen());

				if (activePanel != null && dockable == null) {
					setNotSelectedBorder();
					activePanel = null;
				}

				if (activePanel != dockable && (dockable instanceof DockedSimplePanel || dockable instanceof DockedTabbedPanel)) {
					if (activePanel != null) {
						setNotSelectedBorder();
					}
					activePanel = dockable;
					setSelectedBorder();
				}

			}
			else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				Dockable dockable = DockingComponentUtils.findDockableAtScreenPos(((MouseEvent) e).getLocationOnScreen());

				if (dockable != null) {
					Window window = DockingComponentUtils.findWindowForDockable(dockable);

					if (!getWrapper(dockable).isUnpinned()) {
						DockingComponentUtils.rootForWindow(window).hideUnpinnedPanels();
					}
				}
			}
		}, AWTEvent.MOUSE_EVENT_MASK);

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(() -> {
					if (activePanel != null) {
						setSelectedBorder();
					}
				});

			}
		});
	}

	private void setSelectedBorder() {
		Color color = UIManager.getColor("Component.focusColor");
		activePanel.setBorder(BorderFactory.createLineBorder(color, 2));
	}

	private void setNotSelectedBorder() {
		Color color = UIManager.getColor("Component.borderColor");

		activePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 1),
						BorderFactory.createLineBorder(color, 1)
				)
		);
	}
}
