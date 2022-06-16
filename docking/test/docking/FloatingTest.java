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

public class FloatingTest {
	MainFrame frame;
	FrameFixture fixture;

	@BeforeAll
	public static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		frame = GuiActionRunner.execute(MainFrame::new);
		fixture = new FrameFixture(frame);

		fixture.show(new Dimension(800, 600));
	}

	@AfterEach
	void teardown() {
		fixture.cleanUp();
	}

	@Test
	void dockables_exist() {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);

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
//		robot.settings().delayBetweenEvents(600);

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
