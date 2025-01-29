/*
Copyright (c) 2022-2024 Andrew Auclair

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
package io.github.andrewauclair.moderndocking.api;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for auto persisting the application layout to a file when there are changes to the layout
 */
public class AppStateAPI {
	private static final Logger logger = Logger.getLogger(AppStateAPI.class.getPackageName());
	private static final int PERSIST_TIMER_DELAY_MS = 500;

	private static boolean autoPersist = false;
	private static final Map<DockingAPI, File> autoPersistFiles = new HashMap<>();

	private static ApplicationLayout defaultAppLayout = null;
	private static ApplicationLayout lastPersistedLayout = null;

	private static boolean paused = false;

	private static Timer persistTimer = null;
	private final DockingAPI docking;

	protected AppStateAPI(DockingAPI docking) {
		this.docking = docking;
	}

	/**
	 * Set whether the framework should auto persist the application layout to a file when
	 * docking changes, windows resize, etc.
	 *
	 * @param autoPersist Should the framework auto persist the application layout to a file?
	 */
	public void setAutoPersist(boolean autoPersist) {
		AppStateAPI.autoPersist = autoPersist;
	}

	/**
	 * Are we currently auto persisting to a file?
	 *
	 * @return True - we are auto persisting, False - we are not auto persisting
	 */
	public boolean isAutoPersist() {
		return autoPersist;
	}

	/**
	 * Set the file that should be used for auto persistence. This will be written as an XML file.
	 *
	 * @param file File to persist layout to
	 */
	public void setPersistFile(File file) {
		autoPersistFiles.put(docking, file);
	}

	/**
	 * Retrieve the file that we are persisting the application layout into
	 *
	 * @return The file we are currently persisting to
	 */
	public File getPersistFile() {
		return autoPersistFiles.get(docking);
	}

	/**
	 * Sets the pause state of the auto persistence
	 *
	 * @param paused Whether auto persistence should be enabled
	 */
	public void setPaused(boolean paused) {
		AppStateAPI.paused = paused;
	}

	/**
	 * Gets the pause state of the auto persistence
	 *
	 * @return Whether auto persistence is enabled
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Used to persist the current app layout to the layout file.
	 * This is a no-op if auto persistence is turned off, it's paused or there is no file
	 */
	public void persist() {
		if (!autoPersist || paused) {
			return;
		}

		// we don't want to persist immediately in case this function is getting called a lot.
		// start a timer that will be restarted every time persist() is called, until finally the timer will go off and persist the file.
		if (persistTimer == null) {
			persistTimer = new Timer(PERSIST_TIMER_DELAY_MS, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// we might have gotten to the timer and then paused persistence
					if (!paused && docking.getRootPanels().containsKey(docking.getMainWindow())) {
						ApplicationLayout layout = docking.getDockingState().getApplicationLayout();

						if (lastPersistedLayout != null) {
							if (layout.getMainFrameLayout().getState() != Frame.NORMAL){
								// set position and size of all frames into the new layout
								layout.getMainFrameLayout().setLocation(lastPersistedLayout.getMainFrameLayout().getLocation());
								layout.getMainFrameLayout().setSize(lastPersistedLayout.getMainFrameLayout().getSize());
							}

							List<WindowLayout> oldFrames = lastPersistedLayout.getFloatingFrameLayouts();
							List<WindowLayout> newFrames = layout.getFloatingFrameLayouts();

							for (WindowLayout newFrame : newFrames) {
								if (newFrame.getState() == Frame.NORMAL) {
									continue;
								}

								Optional<WindowLayout> oldFrame = oldFrames.stream()
										.filter(windowLayout -> windowLayout.getWindowHashCode() == newFrame.getWindowHashCode())
										.findFirst();

								if (oldFrame.isPresent()) {
									newFrame.setLocation(oldFrame.get().getLocation());
									newFrame.setSize(oldFrame.get().getSize());
								}
							}
						}
						lastPersistedLayout = layout;

						try {
							docking.getLayoutPersistence().saveLayoutToFile(autoPersistFiles.get(docking), layout);

							DockingLayouts.layoutPersisted(layout);

							logger.log(Level.FINE, "ModernDocking: Persisted Layout Successfully");
						}
						catch (DockingLayoutException ex) {
							logger.log(Level.WARNING, ex.getMessage(), ex);
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

	/**
	 * Restore the application layout from the auto persist file.
	 *
	 * @return true if and only if a layout is restored from a file. Restoring from the default layout will return false.
	 * @throws DockingLayoutException Thrown for any issues with the layout file.
	 */
	public boolean restore() throws DockingLayoutException {
		// don't restore if auto persist is disabled
		File file = autoPersistFiles.get(docking);

		if (!autoPersistFiles.containsKey(docking) || !file.exists()) {
			// restore the default layout if we have one
			if (defaultAppLayout != null) {
				docking.getDockingState().restoreApplicationLayout(defaultAppLayout);
			}
			return false;
		}

		try {
			setPaused(true);

			ApplicationLayout layout = docking.getLayoutPersistence().loadApplicationLayoutFromFile(file);

			docking.getDockingState().restoreApplicationLayout(layout);

			return true;
		}
		catch (Exception e) {
			if (defaultAppLayout != null) {
				docking.getDockingState().restoreApplicationLayout(defaultAppLayout);
			}

			if (e instanceof DockingLayoutException) {
				throw e;
			}
			throw new DockingLayoutException(file, DockingLayoutException.FailureType.LOAD, e);
		}
		finally {
			// make sure that we turn persistence back on
			setPaused(false);
		}
	}

	/**
	 * Set the default layout used by the application. This layout is restored after the application has loaded
	 * and there is no persisted layout or the persisted layout fails to load.
	 *
	 * @param layout Default layout
	 */
	public void setDefaultApplicationLayout(ApplicationLayout layout) {
		defaultAppLayout = layout;
	}

	public Property getProperty(Dockable dockable, String propertyName) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

		return wrapper.getProperty(propertyName);
	}

	public void setProperty(Dockable dockable, String propertyName, Property value) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

		wrapper.setProperty(propertyName, value);
	}

	public void removeProperty(Dockable dockable, String propertyName) {
		DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

		wrapper.removeProperty(propertyName);
	}
}
