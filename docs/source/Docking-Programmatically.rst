Docking Programmatically
=======================

.. code-block:: java

    void dock(String persistentID, Window window)
    void dock(Dockable dockable, Window window)

Dock to a window

.. code-block:: java

    void dock(String persistentID, Window window, DockingRegion region)
    void dock(Dockable dockable, Window window, DockingRegion region)

Dock to a region of a window

.. code-block:: java

    void dock(String persistentID, Window window, DockingRegion region, double dividerProportion)
    void dock(Dockable dockable, Window window, DockingRegion region, double dividerProportion)

Dock to a region of a window with a non-default divider proportion

.. code-block:: java

    void dock(String sourcePersistentID, String targetPersistentID, DockingRegion region)
    void dock(String sourcePersistentID, String Dockable target, DockingRegion region)
    void dock(Dockable source, String targetPersistentID, DockingRegion region)
    void dock(Dockable source, Dockable target, DockingRegion region)
    void dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion)

Dock to a region of a target
