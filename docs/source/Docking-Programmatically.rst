########################
Docking Programmatically
########################

Dock
----

``dock`` methods of the ``Docking`` class are used to dock dockables. There are several variations of this method that allow docking directly to a window or to specific regions of other dockables. There are variations that allow specifying the divider proportions to use for ``JSplitPane`` and each form of ``dock`` allows using the ``persistentID`` directly or an instance of ``Dockable``

.. code-block:: java

    dock(String persistentID, Window window)
    dock(Dockable dockable, Window window)

Allows docking the dockable to a given Window. This will only work if the root docking panel of the window is empty.

.. code-block:: java

    dock(String persistentID, Window window, DockingRegion region)
    dock(Dockable dockable, Window window, DockingRegion region)

Docks the dockable to the specified root region of the window. The divider proportion is set to .25

.. code-block:: java

    dock(String persistentID, Window window, DockingRegion, double dividerProportion)
    dock(Dockable dockable, Window window, DockingRegion, double dividerProportion)

Dock the dockable to the specific root region of the window with a user provided divider proportion.

.. code-block:: java

    dock(String sourcePersistentID, String targetPersistentID, DockingRegion region)
    dock(String sourcePersistentID, Dockable target, DockingRegion region)
    dock(Dockable source, String targetPersistentID, DockingRegion region)
    dock(Dockable source, Dockable target, DockingRegion region)

Dock the dockable to a specific region of another dockable.

.. code-block:: java

    dock(String sourcePersistentID, String targetPersistentID, DockingRegion region, double dividerProportion)
    dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion)

Dock the dockable to a specific region of another dockable with a user provided divider proportion.

Undock
------

.. code-block:: java

    undock(String persistentID)
    undock(Dockable dockable)

Undocks the dockable. Nothing is done if the dockable is not docked

.. code-block:: java

    newWindow(Dockable dockable)
    newWindow(String persistentID, Point location, Dimension size)
    newWindow(Dockable dockable, Point location, Dimension size)

Opens the dockable in a new ``FloatingFrame`` instance. If `location` and `size` are provided, they are used to size the new frame.

Bring to Front
--------------

.. code-block:: java

    void bringToFront(Dockable dockable)
    void bringToFront(String persistentID)

    Brings the dockable to the front if it is not showing. If the dockable is in a tab group it will be made the active tab. If the dockable is hidden due to the Auto Hide feature, then it will be shown. Finally, the frame containing the dockable will be brought to the front with the ``JFrame::toFront`` function.

Display
-------

    This method is a combination of ``dock`` and ``bringToFront``. If the dockable is not docked it will be docked and then brought to the front</p>

.. code-block:: java

    display(Dockable dockable)</code>
    display(String persistentID)</code>

isDocked
--------

    Checks if a dockable is already docked

isMaximized
-----------

    Checks if a dockable is currently maximized
