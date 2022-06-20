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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uitest4j.core.api.swing.SwingRobot;
import org.uitest4j.swing.core.BasicComponentFinder;
import org.uitest4j.swing.core.ComponentFinder;
import org.uitest4j.swing.edt.FailOnThreadViolationRepaintManager;
import org.uitest4j.swing.edt.GuiActionRunner;
import org.uitest4j.swing.fixture.FrameFixture;
import test_app.MainFrame;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/*

everything to test:

docking:
	root
		center
		west
		north
		east
		south
	dockable (simple)
		center
		west
		north
		east
		south
	dockable (split)
		west
		north
		east
		south
	dockable (tabbed)
		center
		west
		north
		east
		south



floating:
	handles
		root handles
			center when empty
			west, east, north, south when not empty
		dockable
			center
			west, east, north, south when not restricted
	overlay
		displayed in correct region
		displays properly with region restrictions
	floating frame
		not decorated
		follows mouse

pinned:
	pinned tabs are part of root layout
	unpinned tabs are in toolbar on side of screen and open when pressed
		clicking else where will rehide the dockable

persistence (save, in memory):
	state is able to be saved to instance

persistence (load, in memory):
	state is able to be restored from instance
	error handling of dockables that don't exist

persistence (save, to file):
	state is able to be saved to file from state instance

persistence (load, from file):
	state is able to be restored from file
	error handling of dockables that don't exist

layouts (pre-scripted persistence):
	states can be created programmatically and applied
	error handling of dockables that don't exist






 */
public class FloatingTest extends BaseUITest {
	@Override
	JFrame createFrame() {
		return new MainFrame();
	}

	@Test
	void dockables_exist() {
		Dockable one = Docking.getDockable("one");

		assertNotNull(one);
	}

	@Test
	void dock_one_to_three() {
		ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();
		Component one = finder.findByName("one");

		Dockable oneDockable = Docking.getDockable("one");
		Dockable threeDockable = Docking.getDockable("three");

		SwingRobot robot = fixture.robot();
		robot.settings().delayBetweenEvents(600);

		Point point = oneDockable.dragSource().getLocation();
		point.x += oneDockable.dragSource().getWidth() / 2;
		point.y += oneDockable.dragSource().getHeight() / 2;

		robot.pressMouse(oneDockable.dragSource(), point);

		Component three = finder.findByName("three");

		point = three.getLocation();
		point.x += three.getWidth() / 2;
		point.y += three.getHeight() / 2;

		robot.moveMouse(three, point);

		robot.releaseMouseButtons();

		assertEquals(one.getParent().getClass(), JTabbedPane.class);
	}
}
