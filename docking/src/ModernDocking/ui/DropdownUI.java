package ModernDocking.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class DropdownUI extends BasicComboBoxUI {
	@Override
	protected JButton createArrowButton() {
		JButton button = super.createArrowButton();
		button.setBackground(comboBox.getBackground());
		return button;
	}
}
