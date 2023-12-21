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

### getPersistentID

`getPersistentID` provides a unique ID that the framework can use to refer to the dockable. This is the main piece of information that the application
and the framework share in order to refer to specific dockables.

### getTabText

`getTabText` provides the text that should be displayed on a tab when this dockable is in a `JTabbedPane`.

<note>If the text displayed on the tab ever changes, the application must call <b>Docking.updateTabText</b> with the dockables persistentID to force the framework to update the text. This should be done anytime the text is changed, just in case the dockable is displaying in a JTabbedPane</note>

## Optional Methods

All the following methods are provided to the application with a default. This means the application only needs to implement the methods
if it wishes to change the default.

### getType

<!-- This one is a bit weird and complicated. It's really just a hack, and we should add something better. -->

### getTabTooltip

<procedure title="getIcon" id="getIcon">
<p>Used by the framework to get the icon for the dockable to use in a `JTabbedPane` tab.</p>
<p>Default value is <code>null</code></p>
</procedure>

### isFloatingAllowed

Allows the application to specify whether a dockable can be dragged out of its current window and floated on its own as a new window.

<procedure title="isLimitedToRoot" id="isLimitedToRoot">
   <p>Allows the application to limit the dockable to the window it was initially docked in.</p>
   <p>Default value is <code>false</code></p>
</procedure>


### getStyle

### getPinningStyle

to be renamed

### isClosable

### isPinningAllowed

### isMinMaxAllowed

### isWrappableInScrollpane

### getHasMoreOptions

### getTabPosition

### addMoreOptions

### createHeaderUI

### updateProperties

<!-- todo: should updateProperties be mandatory? it'll probably be easy to forget if you start adding properties to dockables -->