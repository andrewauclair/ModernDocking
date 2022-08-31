package ModernDocking.internal;

import ModernDocking.persist.AppState;

import javax.swing.*;
import java.awt.event.*;

public class AppStatePersister extends ComponentAdapter implements WindowStateListener {
	public AppStatePersister() {

	}

	public void addFrame(JFrame frame) {
		frame.addComponentListener(this);
		frame.addWindowStateListener(this);
	}

	public void removeFrame(JFrame frame) {
		frame.removeComponentListener(this);
		frame.removeWindowStateListener(this);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void windowStateChanged(WindowEvent e) {
		AppState.persist();
	}
}
