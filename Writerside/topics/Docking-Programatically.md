# Docking Programmatically

dock - docks a dockable. there are many possibilities with this
undock - undocks a dockable and removes it from display.
newWindow - opens a dockable in a new window
bringToFront - brings a dockable to the front. sets to selected tab or brings frame to front, unminimizes frame.
display - if a dockable is docked this calls bringToFront, if not, it calls dock
isDocked - checks if a dockable is docked
isMaximized - checks if a dockable is maximized

pin/unpin (isPinned, isUnpinned, pinDockable, unpinDockable) (renaming to auto-hide)


<procedure title="dock" id="dock">
<p>
The <code>dock</code> methods of the <code>Docking</code> class are used to dock dockables.
</p>
<table style="header-column">
<tr><td><code>dock(String persistentID, Window window)</code></td><td>Allows docking the dockable with <code>persistentID</code> to a given Window. This will only work if the root docking panel of the window is empty.</td></tr>
<tr><td><code>dock(Dockable dockable, Window window)</code></td><td>Allows docking the <code>dockable</code> to a given Window. This will only work if the root docking panel of the window is empty.</td></tr>
<tr><td><code>dock(String persistentID, Window window, DockingRegion region)</code></td><td>Docks the dockable with <code>persistentID</code> to the specified root region of the window. The divider proportion is set to .25.</td></tr>
<tr><td><code>dock(Dockable dockable, Window window, DockingRegion region)</code></td><td>Docks the dockable with <code>persistentID</code> to the specified root region of the window. The divider proportion is set to .25.</td></tr>
<tr><td><code>dock(String persistentID, Window window, DockingRegion, double dividerProportion)</code></td></tr>
<tr><td><code>dock(Dockable dockable, Window window, DockingRegion, double dividerProportion)</code></td></tr>
<tr><td><code>dock(String sourcePersistentID, String targetPersistentID, DockingRegion region)</code></td></tr>
<tr><td><code>dock(String sourcePersistentID, Dockable target, DockingRegion region)</code></td></tr>
<tr><td><code>dock(Dockable source, String targetPersistentID, DockingRegion region)</code></td></tr>
<tr><td><code>dock(Dockable source, Dockable target, DockingRegion region)</code></td></tr>
<tr><td><code>dock(String sourcePersistentID, String targetPersistentID, DockingRegion region, double dividerProportion)</code></td></tr>
<tr><td><code>dock(Dockable source, Dockable target, DockingRegion region, double dividerProportion)</code></td></tr>
</table>
</procedure>

<procedure title="undock" id="undock">
</procedure>

<procedure title="newWindow" id="newWindow">
<tr><td><code>newWindow(Dockable dockable)</code></td><td>Opens the <code>dockable</code> in a new `FloatingFrame` instance.</td></tr>
<tr><td><code>newWindow(String persistentID, Point location, Dimension size)</code></td></tr>
<tr><td><code>newWindow(Dockable dockable, Point location, Dimension size)</code></td></tr>
</procedure>

<procedure title="bringToFront" id="bringToFront">
</procedure>

<procedure title="display" id="display">
</procedure>

<procedure title="isDocked" id="isDocked">
</procedure>

<procedure title="isMaximized" id="isMaximized">
</procedure>