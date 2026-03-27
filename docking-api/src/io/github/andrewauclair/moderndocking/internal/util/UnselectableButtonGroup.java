package io.github.andrewauclair.moderndocking.internal.util;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * special form of ButtonGroup that allows the currently selected button to deselected
 * found from this answer: <a href="https://stackoverflow.com/a/51867860">stackoverflow</a>
 */
public class UnselectableButtonGroup extends ButtonGroup {

	/**
	 * the list of buttons participating in this group
	 */
	private final Vector<AbstractButton> buttonOptions = new Vector<>();

	/**
	 * The current selection.
	 */
	private ButtonModel selectionModel = null;

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
		buttonOptions.addElement(b);

		if (b.isSelected()) {
			if (selectionModel == null) {
				selectionModel = b.getModel();
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
		buttonOptions.removeElement(b);
		if (b.getModel() == selectionModel) {
			selectionModel = null;
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
		if (selectionModel != null) {
			ButtonModel oldSelection = selectionModel;
			selectionModel = null;
			oldSelection.setSelected(false);
		}
	}

	/**
	 * Returns all the buttons that are participating in this group.
	 *
	 * @return an <code>Enumeration</code> of the buttons in this group
	 */
	public Enumeration<AbstractButton> getElements() {
		return buttonOptions.elements();
	}
	/**
	 * Returns the model of the selected button.
	 *
	 * @return the selected button model
	 */
	public ButtonModel getSelection() {
		return selectionModel;
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
			if (model != selectionModel) {
				ButtonModel oldSelection = selectionModel;
				selectionModel = model;
				if (oldSelection != null) {
					oldSelection.setSelected(false);
				}
				model.setSelected(true);
			}
		}
		else if (selectionModel != null) {
			ButtonModel oldSelection = selectionModel;
			selectionModel = null;
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
		return model == selectionModel;
	}

	/**
	 * Returns the number of buttons in the group.
	 *
	 * @return the button count
	 * @since 1.3
	 */
	public int getButtonCount() {
		return buttonOptions.size();
	}
}