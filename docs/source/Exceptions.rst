#############
Exceptions
#############

DockableNotFoundException
-------------------------

    Thrown when a dockable is not found when restoring a DockingLayout

    .. note::
        Thrown by ``DockingState.restoreState``

DockableRegistrationFailureException
------------------------------------

    Thrown when a dockable with the ``persistentID`` has already been registered

    .. note::
        Thrown by ``Docking.registerDockable``

DockingLayoutException
----------------------

    This exception is thrown when there is an issue saving or loading a layout file. The exception provides the file that failed and the failure type

    .. note::
        Thrown by ``AppState.restore``, ``LayoutPersistence.saveLayoutToFile`` and ``LayoutPersistence.loadApplicationLayoutFromFile``

NotDockedException
------------------

    This exception is thrown when Modern Docking attempts to use a dockable that should be docked but isn't. Thrown when the target dockable when docking is not docked or when attempting to bring a dockable to front that isn't already docked

    .. note::
        Thrown by ``Docking.dock`` and ``Docking.bringToFront``

RootDockingPanelNotFoundException
---------------------------------

    Thrown when the root for a window is not found

    .. note::
        Thrown by ``Docking.configurePinning``, ``Docking.dock`` and ``DockingComponentUtils.rootForWindow``

RootDockingPanelRegistrationFailureException
--------------------------------------------

    Thrown when Modern Docking fails to register a ``RootDockingPanel`` because one is already registered for the window

    .. note::
        Thrown by ``Docking.registerDockingPanel``

