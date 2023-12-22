# Dockable Interface

The `Dockable` interface is required to be implemented by the application on any component it wishes to dock.
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
<p>`getPersistentID` provides a unique ID that the framework can use to refer to the dockable. This is the main piece of information that the application
and the framework share in order to refer to specific dockables.</p>
</procedure>

<procedure title="getTabText" id="getTabText">
<p>`getTabText` provides the text that should be displayed on a tab when this dockable is in a `JTabbedPane`.</p>
<note>If the text displayed on the tab ever changes, the application must call <b>Docking.updateTabText</b> with the dockables persistentID to force the framework to update the text. This should be done anytime the text is changed, just in case the dockable is displaying in a JTabbedPane</note>
</procedure>

## Optional Methods

All the following methods are provided to the application with a default. This means the application only needs to implement the methods
if it wishes to change the default.

### getType

<!-- This one is a bit weird and complicated. It's really just a hack, and we should add something better. -->

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
Allows the application to specify whether a dockable can be dragged out of its current window and floated on its own as a new window.

<procedure title="isLimitedToRoot" id="isLimitedToRoot">
<code-block lang="java">boolean isLimitedToRoot()</code-block>
   <p>Allows the application to limit the dockable to the window it was initially docked in.</p>
   <p>Default value is <code>false</code></p>
</procedure>

<procedure title="getStyle" id="getStyle">
<p></p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>

<procedure title="getPinningStyle" id="getPinningStyle">
<p></p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>
to be renamed

<procedure title="isClosable" id="isClosable">
<code-block lang="java">boolean isClosable()</code-block>
<p>Indicates to the docking framework whether the Dockable component can be closed and undocked.</p>
<p>Default value is <code>true</code></p>
</procedure>

### isPinningAllowed

### isMinMaxAllowed

<procedure title="isWrappableInScrollpane" id="isWrappableInScrollpane">
<code-block lang="java">boolean isWrappableInScrollpane()</code-block>
<p>Allows the application to specify whether the docking framework should automatically wrap the Dockable component in a <code>JScrollPane</code>.</p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="getHasMoreOptions" id="getHasMoreOptions">
<code-block lang="java">boolean getHasMoreOptions()</code-block>
<p></p>
</procedure>

<procedure title="getTabPosition" id="getTabPosition">
<code-block lang="java">int getTabPosition()</code-block>
<p></p>
<p>Default value is <code>SwingConstants.BOTTOM</code></p>
</procedure>

<procedure title="addMoreOptions" id="addMoreOptions">
<code-block lang="java">void addMoreOptions(JPopupMenu menu)</code-block>
<p></p>
</procedure>
### createHeaderUI

### updateProperties

<!-- todo: should updateProperties be mandatory? it'll probably be easy to forget if you start adding properties to dockables -->