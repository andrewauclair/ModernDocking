#############
Persistence
#############

Modern Docking will persist the current layout of the application to a file specified through the API. When auto persistence is enabled, this file is saved after a number of different UI actions listed below. A delay mechanism is employed to avoid unnecessarily saving the file, such as when the user is dragging splitters.

The persistence feature defaults to off and can be enabled by calling the ``setPersist``. The file that Modern Docking should use to persist the layout can be configured with ``setPersistFile``. Finally, a default layout can be configured with ``setDefaultApplicationLayout`` for when persistence is disabled or Modern Docking fails to load the current auto persist file.
