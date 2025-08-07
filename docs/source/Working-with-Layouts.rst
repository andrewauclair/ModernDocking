#####################
Working With Layouts
#####################

Layouts can be created in memory from scratch with a builder, pulled from the current window or application layout and saved to files for long term storage.

In memory layouts are created using the ``WindowLayoutBuilder`` class and are built using persistentIDs only, by calling similar ``dock`` functions to the ones provided in the ``Docking`` class. These persistentIDs do not need to be currently registered in the docking framework to be added to ``WindowLayoutBuilder``. An error will be thrown if the persistentID already exists in the layout. When done building the layout, call ``build()`` to create a ``WindowLayout`` or ``buildApplicationLayout()`` to directly build an ``ApplicationLayout``. If you're building a layout for multiple windows, you can manually create an ``ApplicationLayout`` and use ``WindowLayoutBuilder`` to create layouts and add them to ``ApplicationLayout`` with ``addFrame``.

The ``WindowLayout`` and ``ApplicationLayout`` can then be saved to XML, as well as loaded, with the ``WindowLayoutXML`` and ``ApplicationLayoutXML`` classes, respectively.

The docking framework can store these layouts for you and provides a special ``JMenuItem`` that can restore named layouts on the application.

Layouts can be restored by using the ``restoreApplicationLayout`` and ``restoreWindowLayout`` methods of the ``DockingState`` class. This undocks all dockables from the window (or entire application for an ApplicationLayout) and docks the dockables specified by the layout.

Default layout management and restore is discussed in :doc:`Persistence`