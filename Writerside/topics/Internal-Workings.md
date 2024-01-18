# Internal Workings

These topics cover internal functionality of the framework for anyone wanting to become more familiar with how Modern Docking works. This knowledge is not required for using Modern Docking.

<procedure title="ActiveDockableHighlighter" id="ActiveDockableHighlighter">
<p>The <code>ActiveDockableHighlighter</code> is responsible for drawing a border around the dockable that the mouse is currently over. Using an AWT event listener on the AWT Toolkit lets us listen for all mouse events in the entire application.</p>
</procedure>

<procedure title="AppStatePersister" id="AppStatePersister">
<p>Used to call <code>AppState.persist</code> whenever a <code>Window</code> instance resizes, moves or changes state.</p>
</procedure>

<procedure title="" id="">
<p></p>
</procedure>