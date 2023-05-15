package basic;/*
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

import ModernDocking.*;
import ModernDocking.persist.AppState;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Example extends JFrame {
	public Example() {
		// common Java Swing setup
		setTitle("Basic Modern Docking basic.Example");
		setSize(300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create the single Docking instance for the life of the program
		Docking.initialize(this);

		// set the auto persist file
		// auto persist is off by default and we will leave it that way until we're done configuring the default layout
		// that way we're not persisting the default layout and overwriting the file before we load the file
		AppState.setPersistFile(new File("example_layout.xml"));

		// restore the layout from the auto persist file after the UI has loaded
		SwingUtilities.invokeLater(() -> {
			AppState.restore();
			// now that we've restored the layout we can turn on auto persist
			AppState.setAutoPersist(true);
		});

		Panel panel1 = new Panel("one");
		Panel panel2 = new Panel("two");
		Panel panel3 = new Panel("three");
		Panel panel4 = new Panel("four");

		// create the root panel and add it to the frame
		RootDockingPanel rootPanel = new RootDockingPanel(this);

		add(rootPanel, BorderLayout.CENTER);

		// dock our first panel into the center of the frame
		Docking.dock(panel1, this);

		// dock the second panel to the center of the first panel
		Docking.dock(panel2, panel1, DockingRegion.CENTER);

		// dock the third panel to the east of the second panel
		Docking.dock(panel3, panel2, DockingRegion.EAST);

		// dock the fourth panel to the frame south
		Docking.dock(panel4, this, DockingRegion.SOUTH);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Example example = new Example();
			example.setVisible(true);
		});
	}

	// all dockables must be at least Component
	public static class Panel extends JPanel implements Dockable {
		private final String name;

		public Panel(String name) {
			this.name = name;

			// register the dockable with the Docking framework in order to use it
			Docking.registerDockable(this);
		}

		@Override
		public String getPersistentID() {
			return name;
		}

		@Override
		public int getType() {
			return 0;
		}

		@Override
		public String getTabText() {
			return name;
		}

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public boolean isFloatingAllowed() {
			return false;
		}

		@Override
		public boolean shouldLimitToRoot() {
			return false;
		}

		@Override
		public DockableStyle getStyle() {
			return DockableStyle.BOTH;
		}

		@Override
		public boolean canBeClosed() {
			return false;
		}

		@Override
		public boolean allowPinning() {
			return false;
		}

		@Override
		public boolean allowMinMax() {
			return false;
		}

		@Override
		public boolean hasMoreOptions() {
			return false;
		}
	}
}
