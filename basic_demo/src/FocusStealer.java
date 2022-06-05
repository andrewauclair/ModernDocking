import javax.swing.*;
import java.awt.*;

public class FocusStealer extends JFrame {
	FocusStealer() {
		JTextField time = new JTextField();
		JButton steal = new JButton("Steal");

		steal.addActionListener(e -> {
			new Timer(Integer.parseInt(time.getText()) * 1000, e1 -> {
				System.out.println("Stealing focus");
				SwingUtilities.invokeLater(() -> {
					toFront();
					setState(Frame.NORMAL);
					requestFocus();

					setAlwaysOnTop(true);
					setAlwaysOnTop(false);
				});
			}).start();
		});
		add(time, BorderLayout.NORTH);
		add(steal, BorderLayout.SOUTH);

		pack();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FocusStealer focusStealer = new FocusStealer();
			focusStealer.setAutoRequestFocus(true);
			focusStealer.setVisible(true);
		});
	}
}
