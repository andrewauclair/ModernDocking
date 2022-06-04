package docking;

import floating.FloatListener;

import javax.swing.*;

public class DockableWrapper {
	private final Dockable dockable;

	private FloatListener floatListener;
	public DockableWrapper(Dockable dockable) {
		this.dockable = dockable;
		floatListener = new FloatListener(this
);

	}

	public Dockable getDockable() {
		return dockable;
	}

	public void docked() {
//		floatListener = new FloatListener(dockable);
	}

	public void undocked() {
//		if (floatListener != null) {
//			floatListener.removeListeners();
//			floatListener = null;
//		}
	}

	public void removedListeners() {
		floatListener = null;
	}


}
