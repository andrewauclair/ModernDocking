#############
Events
#############

DockingEvent
------------

    This event is fired when dockables are docked, undocked, shown, hidden, pinned or unpinned. Shown and hidden are used when a dockable is in a ``JTabbedPane`` and the active tab changes. Pinned and Unpinned are used when the dockable is added to a toolbar or removed from a toolbar. Shown and hidden will also be fired when a pinned dockable is shown and hidden.

    Temporary events are fired when a user starts dragging a dockable. This allows any listeners to perform actions only based on permanent events (i.e. events that do not have the temporary flag set).JTabbedPane

DockingLayoutEvent
------------------

    Fired when layouts are added to or removed from ``DockingLayouts`` and when layouts are restored or persisted to a file
