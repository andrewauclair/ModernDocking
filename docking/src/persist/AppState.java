package persist;

import java.io.File;

// TODO this class should allow automatic saving of the layouts for all docking frames
// TODO this class should support saving/loading from xml or something similar
public class AppState {
	private static boolean autoPersist = false;
	private static File autoPersistFile = null;

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
}
