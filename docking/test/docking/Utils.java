package docking;

import floating.DockingHandlesFrame;
import org.uitest4j.core.api.swing.SwingRobot;
import org.uitest4j.swing.edt.GuiActionRunner;

import javax.swing.*;
import java.awt.*;

public class Utils {
	public static Point dragCenter(Dockable dockable) {
		JComponent dragSource = dockable.dragSource();

		Point point = dragSource.getLocation();

		point.x += dragSource.getWidth() / 2;
		point.y += dragSource.getHeight() / 2;

		return point;
	}

	public static Point handlePoint(RootDockingPanel root, DockingRegion region) {
		Point location = root.getLocation();
		return location;
	}

	public static Point handlePoint(Dockable dockable, DockingRegion region) {
		Point location = GuiActionRunner.execute(() -> ((Component) dockable).getLocation());
		Dimension size = GuiActionRunner.execute(() -> ((Component) dockable).getSize());

		location.x += size.width / 2;
		location.y += size.height / 2;

		switch (region) {
			case CENTER -> {
				return location;
			}
			case NORTH -> location.y -= DockingHandlesFrame.HANDLE_ICON_SIZE;
			case EAST -> location.x += DockingHandlesFrame.HANDLE_ICON_SIZE;
			case SOUTH -> location.y += DockingHandlesFrame.HANDLE_ICON_SIZE;
			case WEST -> location.x -= DockingHandlesFrame.HANDLE_ICON_SIZE;
		}
		return location;
	}

	// get the handle point and then fudge it so we're not over the handle
	// example: get point of north region handle and go up a little
	public static Point regionPoint(Dockable dockable, DockingRegion region) {
		Point location = ((Component) dockable).getLocation();
		Dimension size = ((Component) dockable).getSize();

		switch (region) {

			case CENTER -> {
				return new Point(location.x + DockingHandlesFrame.HANDLE_ICON_SIZE + 4, location.y + DockingHandlesFrame.HANDLE_ICON_SIZE + 4);
			}
			case NORTH -> {
				return new Point(location.x, (int) (location.y - DockingHandlesFrame.HANDLE_ICON_SIZE * 1.5));
			}
			case EAST -> {
				return new Point((int) (location.x + (DockingHandlesFrame.HANDLE_ICON_SIZE * 1.5)), location.y);
			}
			case SOUTH -> {
				return new Point(location.x, (int) (location.y + DockingHandlesFrame.HANDLE_ICON_SIZE * 1.5));
			}
			case WEST -> {
				return new Point((int) (location.x - DockingHandlesFrame.HANDLE_ICON_SIZE * 1.5), location.y);
			}
		}
		return new Point(0, 0);
	}

	public static void pressDrag(Dockable dockable, SwingRobot robot) {
		Point point = GuiActionRunner.execute(() -> dragCenter(dockable));

		robot.pressMouse(dockable.dragSource(), point);
	}
}
