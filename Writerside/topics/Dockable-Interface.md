# Dockable Interface

The `Dockable` interface is required to be implemented by any component that will be dockable.

Typically, this interface is implemented by a class that extends from `JPanel`.

The `Dockable` interface has a minimum subset of methods that must be implemented by the application and a set of methods that are optional.
These optional methods provide extra customization points for the application per dockable.

<warning>
The return values for the `Dockable` interface methods should be constant after the creation of the dockable component.
Certain methods can be dynamic and the framework provides methods to update their values within the framework. Any method that doesn't specify such
a method can lead to unexpected behavior if changed after the dockable component is created.

Generally, if you wish to modify the return values after creating the dockable component, do so while the dockable is not docked. This can be checked with <b>Docking.isDocked</b>.
</warning>

## Mandatory Methods

These two methods do not provide default implementations in the interface and must be implemented by the application.

<procedure title="getPersistentID" id="getPersistentID">
<p><code>getPersistentID</code> provides a unique ID that the framework can use to refer to the dockable. This is the main piece of information that the application and the framework share in order to refer to specific dockables.</p>
</procedure>

<procedure title="getTabText" id="getTabText">
<p><code>getTabText</code> provides the text that should be displayed on a tab when this dockable is in a <code>JTabbedPane</code>.</p>
<note>If the text displayed on the tab ever changes, the application must call <b>Docking.updateTabText</b> with the dockables persistentID to force the framework to update the text. This should be done anytime the text is changed, just in case the dockable is displaying in a JTabbedPane. Undocking and docking the dockable again will also update the tab text.</note>
</procedure>

## Optional Methods

All the following methods are provided to the application with a default. This means the application only needs to implement the methods
if it wishes to change the default.

<procedure title="getType" id="getType">
<p><code>getType</code> provides an int to Modern Docking. This represents a unique type category for the dockable. Modern Docking will use this value when docking to determine which dockables to dock to.</p>
<p>Default value is <code>0</code></p>
</procedure>

<procedure title="getTabTooltip" id="getTabTooltip">
<p>Used by the framework to get the text to display as a tooltip on <code>JTabbedPane</code> tabs.</p>
<p>Default value is <code>null</code></p>
</procedure>

<procedure title="getIcon" id="getIcon">
<p>Used by the framework to get the icon for the dockable to use in a `JTabbedPane` tab.</p>
<p>Default value is <code>null</code></p>
</procedure>

<procedure title="isFloatingAllowed" id="isFloatingAllowed">
<p>Used by the framework to decided whether the dockable component is allowed to be opened in its own window</p>
<p>Default value is <code>true</code></p>
</procedure>

<procedure title="isLimitedToRoot" id="isLimitedToRoot">
<code-block lang="java">boolean isLimitedToRoot()</code-block>
   <p>Allows the application to limit the dockable to the window it was initially docked in.</p>
   <p>Default value is <code>false</code></p>
</procedure>

<procedure title="getStyle" id="getStyle">
<p>The docking style of the dockable which can be <code>DockableStyle.VERTICAL</code>, <code>DockableStyle.HORIZONTAL</code> or <code>DockableStyle.BOTH</code>. Modern Docking will use this to determine which docking regions to allow when docking this dockable. Docking handles that do not match this style will be hidden.</p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>

<procedure title="getPinningStyle" id="getPinningStyle">
<p>Determines which toolbars this dockable can be displayed on. Uses the same values as <code>getStyle</code></p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>

<procedure title="isClosable" id="isClosable">
<code-block lang="java">boolean isClosable()</code-block>
<p>Indicates to the docking framework whether the Dockable component can be closed and undocked.</p>
<p>Default value is <code>true</code></p>
</procedure>

<procedure title="isPinningAllowed" id="isPinningAllowed">
<p>Determines if the dockable can be pinned to the pinning toolbars and hidden.</p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="isMinMaxAllowed" id="isMinMaxAllowed">
<p>Determines if the dockable can be maximized so that it takes up all the space in the window.</p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="isWrappableInScrollpane" id="isWrappableInScrollpane">
<code-block lang="java">boolean isWrappableInScrollpane()</code-block>
<p>Allows the application to specify whether the docking framework should automatically wrap the Dockable component in a <code>JScrollPane</code>.</p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="getHasMoreOptions" id="getHasMoreOptions">
<code-block lang="java">boolean getHasMoreOptions()</code-block>
<p>Flag that tells Modern Docking that this dockable has more menu items it wishes to add to the context menu. If this method returns true then Modern Docking will call <code>addMoreOptions</code></p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="getTabPosition" id="getTabPosition">
<code-block lang="java">int getTabPosition()</code-block>
<p>Gives the dockables preferred tab location when in a <code>JTabbedPane</code></p>
<p>Default value is <code>SwingConstants.BOTTOM</code></p>
</procedure>

<procedure title="addMoreOptions" id="addMoreOptions">
<code-block lang="java">void addMoreOptions(JPopupMenu menu)</code-block>
<p>Adds this dockables menu items to the context menu</p>
</procedure>

<procedure title="createHeaderUI" id="createHeaderUI">
<p>Creates the header UI for this dockable. The default implementation will create the default Modern Docking header.</p>
</procedure>

<procedure title="updateProperties" id="updateProperties">
<p>Modern Docking will call this method after setting the values of any fields annotated with <code>DockingProperty</code>. If there are no fields with that annotation then this method is not called</p>
</procedure>