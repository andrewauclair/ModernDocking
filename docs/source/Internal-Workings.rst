#################
Internal Workings
#################

These topics cover internal functionality of the framework for anyone wanting to become more familiar with how Modern Docking works. This knowledge is not required for using Modern Docking.

ActiveDockableHighlighter
-------------------------

    The ``ActiveDockableHighlighter`` is responsible for drawing a border around the dockable that the mouse is currently over. Using an AWT event listener on the AWT Toolkit lets us listen for all mouse events in the entire application.

AppStatePersister
-----------------

    Used to call ``AppState.persist`` whenever a ``Window`` instance resizes, moves or changes state.
