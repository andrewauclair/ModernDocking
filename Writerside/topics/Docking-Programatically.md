# Docking Programmatically

<procedure title="dock" id="dock">
<p>
The <code>dock</code> methods of the <code>Docking</code> class are used to dock dockables. There are several variations of this method that allow docking directly to a window or to specific regions of other dockables. There are variations that allow specifying the divider proportions to use for <code>JSplitPane</code>s and each form of <code>dock</code> allows using the <code>persistentID</code> directly or an instance of <code>Dockable</code>
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
<table style="header-column">
<tr><td><code>undock(String persistentID)</code></td><td>Undocks the dockable with the <code>persistentID</code>. Nothing is done if the dockable is not docked</td></tr>
<tr><td><code>undock(Dockable dockable</code></td><td>Undocks <code>dockable</code>. Nothing is done if the dockable is not docked</td></tr>
</table>
</procedure>

<procedure title="newWindow" id="newWindow">
<table style="header-column">
<tr><td><code>newWindow(Dockable dockable)</code></td><td>Opens the <code>dockable</code> in a new `FloatingFrame` instance</td></tr>
<tr><td><code>newWindow(String persistentID, Point location, Dimension size)</code></td><td>Opens the dockable with <code>persistentID</code> in a new <code>FloatingFrame</code> at the given <code>location</code> with the given <code>size</code></td></tr>
<tr><td><code>newWindow(Dockable dockable, Point location, Dimension size)</code></td><td>Opens the <code>dockable</code> in a new <code>FloatingFrame</code> at the given <code>location</code> with the given <code>size</code></td></tr>
</table>
</procedure>

<procedure title="bringToFront" id="bringToFront">
<p>This method will show a dockable if it is not already showing. If the dockable is not the active tab of a <code>JTabbedPanel</code> it will be made the active tab. If the dockable is unpinned it will be shown</p>
</procedure>

<procedure title="display" id="display">
<p>This method is a combination of <code>dock</code> and <code>bringToFront</code>. If the dockable is not docked it will be docked and then brought to the front</p>
</procedure>

<procedure title="isDocked" id="isDocked">
<p>Simple method to check if a dockable is already docked</p>
</procedure>

<procedure title="isMaximized" id="isMaximized">
<p>Simple method to check if a dockable is currently maximized</p>
</procedure>