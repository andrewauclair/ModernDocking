/*
Copyright (c) 2022 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package persist;

import docking.Docking;
import layouts.FullAppLayout;
import layouts.FullAppLayoutXML;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class AppState {
	private static final int PERSIST_TIMER_DELAY_MS = 500;

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

		// we don't want to persist immediately in case this function is getting called a lot.
		// start a timer that will be restarted every time persist() is called, until finally the timer will go off and persist the file.
		if (persistTimer == null) {
			persistTimer = new Timer(PERSIST_TIMER_DELAY_MS, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// we might have gotten to the timer and then paused persistence
					if (!paused) {
						System.out.println("persist full docking layout");

						FullAppLayout layout = Docking.getFullLayout();

						FullAppLayoutXML.saveLayoutToFile(autoPersistFile, layout);
					}
					// we're done with the timer for now. null it out
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
		// don't restore if auto persist is disabled
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
