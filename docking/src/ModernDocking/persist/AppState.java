/*
Copyright (c) 2022-2023 Andrew Auclair

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
package ModernDocking.persist;

import ModernDocking.DockingState;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.ApplicationLayoutXML;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppState {
	private static final Logger logger = Logger.getLogger(AppState.class.getPackageName());
	private static final int PERSIST_TIMER_DELAY_MS = 500;

	private static boolean autoPersist = false;
	private static File autoPersistFile = null;

	private static ApplicationLayout defaultAppLayout = null;

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

						ApplicationLayout layout = DockingState.getApplicationLayout();

						try {
							ApplicationLayoutXML.saveLayoutToFile(autoPersistFile, layout);
						}
						catch (DockingLayoutException ex) {
							logger.log(Level.INFO, ex.getMessage(), ex);
						}
					}
					// we're done with the timer for now. null it out
					persistTimer = null;
				}
			});
			persistTimer.setRepeats(false);
			persistTimer.setCoalesce(false);
			persistTimer.start();
		}
		else {
			persistTimer.restart();
		}
	}

	// returns true if and only if a layout is restored from a file. Restoring from the default layout will return false.
	public static boolean restore() throws DockingLayoutException {
		// don't restore if auto persist is disabled
		if (autoPersistFile == null || !autoPersistFile.exists()) {
			// restore the default layout if we have one
			if (defaultAppLayout != null) {
				DockingState.restoreApplicationLayout(defaultAppLayout);
			}
			return false;
		}

		try {
			AppState.setPaused(true);

			ApplicationLayout layout = ApplicationLayoutXML.loadLayoutFromFile(autoPersistFile);

			DockingState.restoreApplicationLayout(layout);

			return true;
		}
		catch (Exception e) {
			if (defaultAppLayout != null) {
				DockingState.restoreApplicationLayout(defaultAppLayout);
			}

			if (e instanceof DockingLayoutException) {
				throw e;
			}
			throw new DockingLayoutException(e);
		}
		finally {
			// make sure that we turn persistance back on
			AppState.setPaused(false);
		}
	}

	public static void setDefaultApplicationLayout(ApplicationLayout layout) {
		defaultAppLayout = layout;
	}
}
