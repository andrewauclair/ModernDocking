package docking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.uitest4j.swing.edt.FailOnThreadViolationRepaintManager;
import org.uitest4j.swing.edt.GuiActionRunner;
import org.uitest4j.swing.fixture.FrameFixture;
import test_app.MainFrame;

import javax.swing.*;
import java.awt.*;

public abstract class BaseUITest {
	JFrame frame;

	FrameFixture fixture;

	@BeforeAll
	public static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		frame = GuiActionRunner.execute(this::createFrame);
		fixture = new FrameFixture(frame);

		fixture.show(new Dimension(800, 600));
	}

	@AfterEach
	void teardown() {
		fixture.cleanUp();
	}

	abstract JFrame createFrame();
}
