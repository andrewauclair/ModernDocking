package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.Property;
import java.util.Map;

public interface DynamicDockableCreationListener {
    Dockable createDockable(String persistentID, String className, String titleText, String tabText, Map<String, Property> properties);
}
