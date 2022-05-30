import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TestMain {
	private static class Panel extends JPanel {
		public final JPanel titlePanel = new JPanel();

		public Panel() {
//			setBackground(Color.blue.brighter().brighter());
			setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;

			titlePanel.add(new JLabel("Title goes here"));
			titlePanel.setBackground(new Color(78, 78, 247, 255));

			add(titlePanel, gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;

			add(new JPanel(), gbc);
		}

		public Component getDraggable() {
			return titlePanel;
		}
	}

	static JFrame parent = null;
//	static JFrame
	static boolean mouseDown = false;
	static boolean mouseDragging = false;
	static boolean floating = false;

	static Timer floatMouseTimer = null;

	static Point dragOffset = new Point(0, 0);

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(new Dimension(200, 200));


		Panel p = new Panel();
		frame.add(p, BorderLayout.CENTER);

		parent = frame;

		centerWindow(frame);
		frame.setVisible(true);

		SwingUtilities.invokeLater(() -> {
			Dimension size = p.getSize();
			Point location = p.getLocation();

			SwingUtilities.convertPointToScreen(location, p);

			size.height -= 10;
			size.width -= 10;

			location.x += 5;
			location.y += 5;

			JFrame frame_dock_highlight = new JFrame();
			frame_dock_highlight.setSize(size);
			frame_dock_highlight.setLocation(location);

			frame_dock_highlight.setUndecorated(true);
			frame_dock_highlight.setType(Window.Type.UTILITY);
			frame_dock_highlight.setBackground(new Color(45, 214, 206, 90));
			frame_dock_highlight.setVisible(true);
		});

		p.getDraggable().addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseDown = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {


//				boolean insideFrame = frame.getBounds().contains(e.getPoint());
//
//				System.out.println("Mouse was released in frame: " + insideFrame);
//
//				if (!insideFrame) {
//					// make a new frame where we dropped the panel and move the panel to it
//					JFrame newFrame = new JFrame();
//					newFrame.setSize(new Dimension(200, 200));
//
//					Point newPoint = e.getPoint();
//
//					SwingUtilities.convertPointToScreen(newPoint, p);
//
//					newFrame.setLocation(newPoint);
//
//					parent.remove(p);
//					parent.repaint();
//					newFrame.add(p);
//
//					parent = newFrame;
//
//					newFrame.setVisible(true);
//				}

				mouseDown = false;
				mouseDragging = false;
				floating = false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// we just floated the panel and ended up here. we need to manaully move the panel when the mouse moves
				if (floating) {
					System.out.println("Floating, so move it manually");

					floatMouseTimer = new Timer(50, e1 -> {
						SwingUtilities.invokeLater(() -> {
							if (floating) {
//								System.out.println("Move panel to mouse");
								Point point = MouseInfo.getPointerInfo().getLocation();
								point.x -= dragOffset.x;
								point.y -= dragOffset.y;
//							SwingUtilities.convertPointToScreen(point, p);
								parent.setLocation(point);
							}
						});
					});
					floatMouseTimer.start();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});

		p.getDraggable().addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (!mouseDragging) {
					dragOffset = e.getPoint();

					// as soon as drag starts "undock" the panel and float it in a hidden frame
					JFrame newFrame = new JFrame();
					newFrame.setSize(p.getSize());

					Point newPoint = e.getPoint();

					SwingUtilities.convertPointToScreen(newPoint, p);

					newPoint.x -= dragOffset.x;
					newPoint.y -= dragOffset.y;

					newFrame.setLocation(newPoint);

					parent.remove(p);
					parent.repaint();
					newFrame.add(p);

					parent = newFrame;

					parent.setUndecorated(true);


					newFrame.setVisible(true);

					floating = true;

//					try {
//						Robot robot = new Robot();
//						robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//					}
//					catch (AWTException ex) {
//						throw new RuntimeException(ex);
//					}
				}
				else {
					Point newPoint = e.getPoint();

					SwingUtilities.convertPointToScreen(newPoint, p);

					parent.setLocation(newPoint);
				}
				mouseDragging = true;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (floatMouseTimer != null) {
					System.out.println("Stop timer");
					floatMouseTimer.stop();
					floatMouseTimer = null;
				}

				if (floating) {
					floating = false;
					mouseDragging = false;
					mouseDown = false;

					// TODO check if we released the mouse over an existing frame, then destroy the temporary undecorated frame and put the panel in the other frame
					// other wise, we keep the frame, decorate it and stop floating
					System.out.println("Stopped floating");

					JFrame newFrame = new JFrame();
					newFrame.setSize(new Dimension(200, 200));

					Point newPoint = e.getPoint();

					SwingUtilities.convertPointToScreen(newPoint, p);

					newPoint.x -= dragOffset.x;
					newPoint.y -= dragOffset.y;

					newFrame.setLocation(newPoint);

					parent.remove(p);

					parent.setVisible(false);
					parent.dispose();

					newFrame.add(p);

					parent = newFrame;

//					parent.setUndecorated(true);


					newFrame.setVisible(true);
				}
			}
		});

		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent e) {
				int id = e.getID();

				switch (id) {
					case 501 -> System.out.println("MOUSE_DOWN");
					case 502 -> System.out.println("MOUSE_UP");
					case 503 -> System.out.println("MOUSE_MOVE");
					case 504 -> System.out.println("MOUSE_ENTER");
					case 505 -> System.out.println("MOUSE_EXIT");
					case 506 -> System.out.println("MOUSE_DRAG");
					default -> System.out.println(id);
				}
			}
		}, eventMask);
	}

	public static void centerWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
}
