package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DynamicDockableParameters;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.exception.DockableNotFoundException;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.internal.FailedDockable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultDynamicDockableCreationListener implements DynamicDockableCreationListener {
    private static final Logger logger = Logger.getLogger(DefaultDynamicDockableCreationListener.class.getPackageName());

    private final DockingAPI docking;

    public DefaultDynamicDockableCreationListener(DockingAPI docking) {
        this.docking = docking;
    }

    @Override
    public Dockable createDockable(String persistentID, String className, String titleText, String tabText, Map<String, Property> properties) {
        boolean foundNewConstructor = false;

        try {
            Class<?> aClass = Class.forName(className);
            Constructor<?> constructor = aClass.getConstructor(DynamicDockableParameters.class);

            constructor.newInstance(new DynamicDockableParameters(persistentID, tabText, titleText));

            foundNewConstructor = true;
        }
        catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
               InvocationTargetException e) {
            logger.log(Level.INFO, "Failed to create instance of dynamic dockable with DynamicDockableParameters constructor. Falling back on (String, String)");
            logger.log(Level.INFO, e.getMessage(), e);
        }

        if (!foundNewConstructor) {
            try {
                Class<?> aClass = Class.forName(className);
                Constructor<?> constructor = aClass.getConstructor(String.class, String.class);

                // create the instance, this should register the dockable and let us look it up
                constructor.newInstance(persistentID, persistentID);
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                   InvocationTargetException e) {
                logger.log(Level.INFO, e.getMessage(), e);
                return null;
            }
        }

        Dockable dockable = getDockable(docking, persistentID);

        if (dockable instanceof FailedDockable) {
            return null;
        }
        return dockable;
    }

    private Dockable getDockable(DockingAPI docking, String persistentID) {
        try {
            return DockingInternal.get(docking).getDockable(persistentID);
        }
        catch (DockableNotFoundException ignore) {
        }
        return new FailedDockable(docking, persistentID);
    }
}
