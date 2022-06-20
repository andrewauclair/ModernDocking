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

import org.junit.jupiter.api.Test;
import org.uitest4j.swing.edt.GuiActionRunner;
import persist.PanelState;
import persist.RootDockState;
import persist.SplitState;
import test_app.SimplePanel;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Docking_Dockable_Programmatic_Test extends BaseUITest {
	@Override
	JFrame createFrame() {
		return new TestFrame();
	}

	@Test
	void dock_to_dockable_north() {
		GuiActionRunner.execute(() -> Docking.dock(Docking.getDockable("two"), Docking.getDockable("one"), DockingRegion.NORTH));

		RootDockState state = Docking.getRootState(frame);

		assertEquals(SplitState.class, state.getState().getClass());

		SplitState split = (SplitState) state.getState();

		assertEquals(JSplitPane.VERTICAL_SPLIT, split.getOrientation());
		assertEquals(PanelState.class, split.getLeft().getClass());
		assertEquals(PanelState.class, split.getRight().getClass());

		PanelState left = (PanelState) split.getLeft();
		PanelState right = (PanelState) split.getRight();

		assertEquals("one", right.getPersistentID());
		assertEquals("two", left.getPersistentID());
	}

	@Test
	void dock_to_dockable_south() {
		GuiActionRunner.execute(() -> Docking.dock(Docking.getDockable("two"), Docking.getDockable("one"), DockingRegion.SOUTH));

		RootDockState state = Docking.getRootState(frame);

		assertEquals(SplitState.class, state.getState().getClass());

		SplitState split = (SplitState) state.getState();

		assertEquals(JSplitPane.VERTICAL_SPLIT, split.getOrientation());
		assertEquals(PanelState.class, split.getLeft().getClass());
		assertEquals(PanelState.class, split.getRight().getClass());

		PanelState left = (PanelState) split.getLeft();
		PanelState right = (PanelState) split.getRight();

		assertEquals("one", left.getPersistentID());
		assertEquals("two", right.getPersistentID());
	}

	@Test
	void dock_to_dockable_west() {
		GuiActionRunner.execute(() -> Docking.dock(Docking.getDockable("two"), Docking.getDockable("one"), DockingRegion.WEST));

		RootDockState state = Docking.getRootState(frame);

		assertEquals(SplitState.class, state.getState().getClass());

		SplitState split = (SplitState) state.getState();

		assertEquals(JSplitPane.HORIZONTAL_SPLIT, split.getOrientation());
		assertEquals(PanelState.class, split.getLeft().getClass());
		assertEquals(PanelState.class, split.getRight().getClass());

		PanelState left = (PanelState) split.getLeft();
		PanelState right = (PanelState) split.getRight();

		assertEquals("one", right.getPersistentID());
		assertEquals("two", left.getPersistentID());
	}

	@Test
	void dock_to_dockable_east() {
		GuiActionRunner.execute(() -> Docking.dock(Docking.getDockable("two"), Docking.getDockable("one"), DockingRegion.EAST));

		RootDockState state = Docking.getRootState(frame);

		assertEquals(SplitState.class, state.getState().getClass());

		SplitState split = (SplitState) state.getState();

		assertEquals(JSplitPane.HORIZONTAL_SPLIT, split.getOrientation());
		assertEquals(PanelState.class, split.getLeft().getClass());
		assertEquals(PanelState.class, split.getRight().getClass());

		PanelState left = (PanelState) split.getLeft();
		PanelState right = (PanelState) split.getRight();

		assertEquals("one", left.getPersistentID());
		assertEquals("two", right.getPersistentID());
	}

	private static class TestFrame extends JFrame {
		TestFrame() {
			new Docking(this);

			SimplePanel one = new SimplePanel("one", "one");
			SimplePanel two = new SimplePanel("two", "two");

			RootDockingPanel dockingPanel = new RootDockingPanel();
			Docking.registerDockingPanel(dockingPanel, this);

			add(dockingPanel, BorderLayout.CENTER);

			Docking.dock(one, this);
		}
	}
}
