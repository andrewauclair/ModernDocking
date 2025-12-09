package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DynamicDockableParameters;

public interface DynamicDockableCreationListener {
    Dockable createDockable(String persistentID, String className, String titleText, String tabText);
}
