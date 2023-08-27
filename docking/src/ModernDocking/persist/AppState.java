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

import ModernDocking.Docking;
import ModernDocking.DockingInstance;
import ModernDocking.DockingState;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.ApplicationLayoutXML;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for auto persisting the application layout to a file when there are changes to the layout
 */
public class AppState {
	private static final Logger logger = Logger.getLogger(AppState.class.getPackageName());
	private static final int PERSIST_TIMER_DELAY_MS = 500;

	private static boolean autoPersist = false;
	private static Map<DockingInstance, File> autoPersistFiles = new HashMap<>();

	private static ApplicationLayout defaultAppLayout = null;

	private static boolean paused = false;

	private static Timer persistTimer = null;

	/**
	 * Set whether the framework should auto persist the application layout to a file when
	 * docking changes, windows resize, etc.
	 *
	 * @param autoPersist Should the framework auto persist the application layout to a file?
	 */
	public static void setAutoPersist(boolean autoPersist) {
		AppState.autoPersist = autoPersist;
	}

	/**
	 * Are we currently auto persisting to a file?
	 *
	 * @return True - we are auto persisting, False - we are not auto persisting
	 */
	public static boolean isAutoPersist() {
		return autoPersist;
	}

	/**
	 * Set the file that should be used for auto persistence. This will be written as an XML file.
	 *
	 * @param file File to persist layout to
	 */
	public static void setPersistFile(File file) {
		setPersistFile(Docking.getSingleInstance(), file);
	}

	public static void setPersistFile(DockingInstance docking, File file) {
		autoPersistFiles.put(docking, file);
	}

	/**
	 * Retrieve the file that we are persisting the application layout into
	 *
	 * @return The file we are currently persisting to
	 */
	public static File getPersistFile() {
		return autoPersistFiles.get(Docking.getSingleInstance());
	}

	public static File getPersistFile(DockingInstance docking) {
		return autoPersistFiles.get(docking);
	}

	/**
	 * Sets the pause state of the auto persistence
	 *
	 * @param paused Whether auto persistence should be enabled
	 */
	public static void setPaused(boolean paused) {
		AppState.paused = paused;
	}

	/**
	 * Gets the pause state of the auto persistence
	 *
	 * @return Whether auto persistence is enabled
	 */
	public static boolean isPaused() {
		return paused;
	}

	/**
	 * Used to persist the current app layout to the layout file.
	 * This is a no-op if auto persistence is turned off, it's paused or there is no file
	 */
	public static void persist() {
		persist(Docking.getSingleInstance());
	}

	public static void persist(DockingInstance docking) {
		if (!autoPersist || paused || autoPersistFiles == null) {
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

						ApplicationLayout layout = DockingState.getApplicationLayout(docking);

						try {
							ApplicationLayoutXML.saveLayoutToFile(docking, autoPersistFiles.get(docking), layout);
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

	public static boolean restore() throws DockingLayoutException {
		return restore(Docking.getSingleInstance());
	}

	/**
	 * Restore the application layout from the auto persist file.
	 *
	 * @return true if and only if a layout is restored from a file. Restoring from the default layout will return false.
	 * @throws DockingLayoutException Thrown for any issues with the layout file.
	 */
	public static boolean restore(DockingInstance docking) throws DockingLayoutException {
		// don't restore if auto persist is disabled
		if (!autoPersistFiles.containsKey(docking) || !autoPersistFiles.get(docking).exists()) {
			// restore the default layout if we have one
			if (defaultAppLayout != null) {
				DockingState.restoreApplicationLayout(docking, defaultAppLayout);
			}
			return false;
		}

		try {
			AppState.setPaused(true);

			ApplicationLayout layout = ApplicationLayoutXML.loadLayoutFromFile(docking, autoPersistFiles.get(docking));

			DockingState.restoreApplicationLayout(docking, layout);

			return true;
		}
		catch (Exception e) {
			if (defaultAppLayout != null) {
				DockingState.restoreApplicationLayout(docking, defaultAppLayout);
			}

			if (e instanceof DockingLayoutException) {
				throw e;
			}
			throw new DockingLayoutException(e);
		}
		finally {
			// make sure that we turn persistence back on
			AppState.setPaused(false);
		}
	}

	/**
	 * Set the default layout used by the application. This layout is restored after the application has loaded
	 * and there is no persisted layout or the persisted layout fails to load.
	 *
	 * @param layout Default layout
	 */
	public static void setDefaultApplicationLayout(ApplicationLayout layout) {
		defaultAppLayout = layout;
	}
}
