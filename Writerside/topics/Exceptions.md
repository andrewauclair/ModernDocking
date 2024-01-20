# Exceptions

<procedure title="DockableNotFoundException" id="DockableNotFoundException">
<p>Thrown when a dockable is not found when restoring a DockingLayout</p>
<note>Thrown by <code>DockingState.restoreState</code></note>
</procedure>
<procedure title="DockableRegistrationFailureException" id="DockableRegistrationFailureException">
<p>Thrown when a dockable with the <code>persistentID</code> has already been registered</p>
<note>Thrown by <code>Docking.registerDockable</code></note>
</procedure>
<procedure title="DockingLayoutException" id="DockingLayoutException">
<p>This exception is thrown when there is an issue saving or loading a layout file. The exception provides the file that failed and the failure type</p>
<note>Thrown by <code>AppState.restore</code>, <code>LayoutPersistence.saveLayoutToFile</code> and <code>LayoutPersistence.loadApplicationLayoutFromFile</code></note>
</procedure>
<procedure title="NotDockedException" id="NotDockedException">
<p>This exception is thrown when Modern Docking attempts to use a dockable that should be docked but isn't. Thrown when the target dockable when docking is not docked or when attempting to bring a dockable to front that isn't already docked</p>
<note>Thrown by <code>Docking.dock</code> and <code>Docking.bringToFront</code></note>
</procedure>
<procedure title="RootDockingPanelNotFoundException" id="RootDockingPanelNotFoundException">
<p>Thrown when the root for a window is not found</p>
<note>Thrown by <code>Docking.configurePinning</code>, <code>Docking.dock</code> and <code>DockingComponentUtils.rootForWindow</code></note>
</procedure>
<procedure title="RootDockingPanelRegistrationFailureException" id="RootDockingPanelRegistrationFailureException">
<p>Thrown when Modern Docking fails to register a <code>RootDockingPanel</code> because one is already registered for the window</p>
<note>Thrown by <code>Docing.registerDockingPanel</code></note>
</procedure>