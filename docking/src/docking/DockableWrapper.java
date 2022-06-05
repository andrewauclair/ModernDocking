package docking;

import floating.FloatListener;

public class DockableWrapper {
	private DockingPanel parent;
	private final Dockable dockable;

	private FloatListener floatListener;
	public DockableWrapper(Dockable dockable) {
		this.dockable = dockable;
		floatListener = new FloatListener(this);
	}

	public void setParent(DockingPanel parent) {
		this.parent = parent;
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

	public DockingPanel getParent() {
		return parent;
	}
}
