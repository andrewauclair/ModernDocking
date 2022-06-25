package persist;

import docking.Docking;
import layouts.FullAppLayout;
import layouts.FullAppLayoutXML;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class AppState {
	private static boolean autoPersist = false;
	private static File autoPersistFile = null;

	private static boolean paused = false;

	private static Timer persistTimer = null;

	public static void setAutoPersist(boolean autoPersist) {
		AppState.autoPersist = autoPersist;
	}

	public static boolean isAutoPersist() {
		return autoPersist;
	}

	public static void setPersistFile(File file) {
		autoPersistFile = file;
	}

	public static File getPersistFile() {
		return autoPersistFile;
	}

	public static void setPaused(boolean paused) {
		AppState.paused = paused;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void persist() {
		if (!autoPersist || paused || autoPersistFile == null) {
			return;
		}

		if (persistTimer == null) {
			persistTimer = new Timer(1000, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("persist full layout");

					FullAppLayout layout = Docking.getFullLayout();

					FullAppLayoutXML.saveLayoutToFile(autoPersistFile, layout);

					persistTimer = null;
				}
			});
			persistTimer.setRepeats(false);
			persistTimer.setCoalesce(false);
		}
		else {
			persistTimer.restart();
		}
	}

	public static boolean restore() {
		if (autoPersistFile == null || !autoPersistFile.exists()) {
			return false;
		}

		FullAppLayout layout = FullAppLayoutXML.loadLayoutFromFile(autoPersistFile);

		if (layout != null) {
			Docking.restoreFullLayout(layout);
		}

		return layout != null;
	}
}
