package modern_docking.util;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Vector;

// special form of ButtonGroup that allows the currently selected button to deselected
// found from this answer: https://stackoverflow.com/a/51867860
public class UnselectableButtonGroup extends ButtonGroup {

	// the list of buttons participating in this group
	protected final Vector<AbstractButton> buttons = new Vector<>();

	/**
	 * The current selection.
	 */
	ButtonModel selection = null;

	/**
	 * Creates a new <code>ButtonGroup</code>.
	 */
	public UnselectableButtonGroup() {
	}

	/**
	 * Adds the button to the group.
	 *
	 * @param b the button to be added
	 */
	public void add(AbstractButton b) {
		if (b == null) {
			return;
		}
		buttons.addElement(b);

		if (b.isSelected()) {
			if (selection == null) {
				selection = b.getModel();
			} else {
				b.setSelected(false);
			}
		}

		b.getModel().setGroup(this);
	}

	/**
	 * Removes the button from the group.
	 *
	 * @param b the button to be removed
	 */
	public void remove(AbstractButton b) {
		if (b == null) {
			return;
		}
		buttons.removeElement(b);
		if (b.getModel() == selection) {
			selection = null;
		}
		b.getModel().setGroup(null);
	}

	/**
	 * Clears the selection such that none of the buttons in the
	 * <code>ButtonGroup</code> are selected.
	 *
	 * @since 1.6
	 */
	public void clearSelection() {
		if (selection != null) {
			ButtonModel oldSelection = selection;
			selection = null;
			oldSelection.setSelected(false);
		}
	}

	/**
	 * Returns all the buttons that are participating in this group.
	 *
	 * @return an <code>Enumeration</code> of the buttons in this group
	 */
	public Enumeration<AbstractButton> getElements() {
		return buttons.elements();
	}
	/**
	 * Returns the model of the selected button.
	 *
	 * @return the selected button model
	 */
	public ButtonModel getSelection() {
		return selection;
	}

	/**
	 * Sets the selected value for the <code>ButtonModel</code>. Only one
	 * button in the group may be selected at a time.
	 *
	 * @param model the <code>ButtonModel</code>
	 * @param selected <code>true</code> if this button is to be selected,
	 * otherwise <code>false</code>
	 */
	public void setSelected(ButtonModel model, boolean selected) {
		if (selected) {
			if (model != selection) {
				ButtonModel oldSelection = selection;
				selection = model;
				if (oldSelection != null) {
					oldSelection.setSelected(false);
				}
				model.setSelected(true);
			}
		}
		else if (selection != null) {
			ButtonModel oldSelection = selection;
			selection = null;
			oldSelection.setSelected(false);
		}
	}

	/**
	 * Returns whether a <code>ButtonModel</code> is selected.
	 *
	 * @return <code>true</code> if the button is selected, otherwise
	 * returns <code>false</code>
	 */
	public boolean isSelected(ButtonModel model) {
		return model == selection;
	}

	/**
	 * Returns the number of buttons in the group.
	 *
	 * @return the button count
	 * @since 1.3
	 */
	public int getButtonCount() {
		return buttons.size();
	}
}