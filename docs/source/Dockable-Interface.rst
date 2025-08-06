==================
Dockable Interface
==================

The ``Dockable`` interface is required to be implemented by any component that will be dockable.

Typically, this interface is implemented by a class that extends from ``JPanel``.

The ``Dockable`` interface has a minimum subset of methods that must be implemented by the application and a set of methods that are optional.
These optional methods provide extra customization points for the application per dockable.

.. warning::
    The return values for the <code>Dockable</code> interface methods should be constant after the creation of the dockable component.
    Certain methods can be dynamic and the framework provides methods to update their values within the framework. Any method that doesn't specify such
    a method can lead to unexpected behavior if changed after the dockable component is created.

    Generally, if you wish to modify the return values after creating the dockable component, do so while the dockable is not docked. This can be checked with <b>Docking.isDocked</b>.

-----------------
Mandatory Methods
-----------------

These two methods do not provide default implementations in the interface and must be implemented by the application.

.. code-block:: java
    String persistentID()

Provides a unique ID for the dockable to use within Modern Docking

.. code-block:: java
    getTabText

Provides the text that will be displayed on a tab when the dockable is in a ``JTabbedPane``.

.. important::
    If the text displayed on the tab ever changes, the application must call <b>Docking.updateTabText</b> with the dockables persistentID to force the framework to update the text. This should be done anytime the text is changed, just in case the dockable is displaying in a JTabbedPane. Undocking and docking the dockable again will also update the tab text.</note>

----------------
Optional Methods
----------------

The following methods are provided to the application with a default. This means the application only needs to implement the methods
if it wishes to change the default.

.. code-block::
    int getType()

``Default:`` 0

Provides an int to Modern Docking. This represents a unique type category for the dockable. Modern Docking will use this value when docking to determine which dockables to dock to.

.. code-block::
    String getTitleText()

``Default:`` value of ``getTabText``

Provides the text that Modern Docking should display on the header. This string can be different than ``getTabText``

.. code-block::
    String getTabTooltip()

``Default::`` ``null``
Used by the framework to get the text to display as a tooltip on <code>JTabbedPane</code> tabs.

<procedure title="getIcon" id="getIcon">
<code-block lang="java">Icon getIcon()</code-block>
<p>Used by the framework to get the icon for the dockable to use in a <code>JTabbedPane</code> tab.</p>
<p>Default value is <code>null</code></p>
</procedure>

<procedure title="isFloatingAllowed" id="isFloatingAllowed">
<code-block lang="java">boolean isFloatingAllowed()</code-block>
<p>Tells Modern Docking if the dockable is allowed to be opened in its own window</p>
<p>Default value is <code>true</code></p>
</procedure>

<procedure title="isLimitedToWindow" id="isLimitedToWindow">
<code-block lang="java">boolean isLimitedToWindow()</code-block>
   <p>Allows the application to limit the dockable to the window it was initially docked in.</p>
   <p>Default value is <code>false</code></p>
</procedure>

<procedure title="getStyle" id="getStyle">
<code-block lang="java">DockableStyle getStyle()</code-block>
<p>The docking style of the dockable which can be <code>DockableStyle.VERTICAL</code>, <code>DockableStyle.HORIZONTAL</code>, <code>DockableStyle.BOTH</code> or <code>DockableStyle.CENTER_ONLY</code>. Modern Docking will use this to determine which docking regions to allow when docking this dockable. Docking handles that do not match this style will be hidden.</p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>

<procedure title="getAutoHideStyle" id="getAutoHideStyle">
<code-block lang="java">DockableStyle getAutoHideStyle()</code-block>
<p>Determines which toolbars this dockable can be displayed on. Uses the same values as <code>getStyle</code>. <code>DockableStyle.VERTICAL</code> will allow the dockable on the east and west auto hide toolbars. <code>DockableStyle.HORIZONTAL</code> will allow the dockable on the south auto hide toolbar. <code>DockableStyle.CENTER_ONLY</code> is invalid for this method.</p>
<p>Default value is <code>DockableStyle.BOTH</code></p>
</procedure>

<procedure title="isClosable" id="isClosable">
<code-block lang="java">boolean isClosable()</code-block>
<p>Indicates to the docking framework whether the Dockable component can be closed and undocked.</p>
<p>Default value is <code>true</code></p>
</procedure>

<procedure title="requestClose" id="requestClose">
<code-block lang="java">boolean requestClose()</code-block>
<p>Called by Modern Docking when the dockable is in the process of closing due to <code>undock</code>. This allows the application to stop the dockable from closing. For example, maybe the user has unsaved changes and the application wishes to confirm closing of the dockable.</p>
<p>Default value is <code>true</code></p>
</procedure>

<procedure title="isAutoHideAllowed" id="isAutoHideAllowed">
<code-block lang="java">boolean isAutoHideAllowed()</code-block>
<p>Determines if the dockable can be set to the auto hide toolbars.</p>
<p>Default value is <code>false</code></p>
</procedure>

<procedure title="isMinMaxAllowed" id="isMinMaxAllowed">
<code-block lang="java">boolean isMinMaxAllowed()</code-block>
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

<procedure title="getTabPreference" id="getTabPreference">
<code-block lang="java">DockableTabPreference getTabPreference()</code-block>
<p>Gives the dockables preferred tab location when in a <code>JTabbedPane</code></p>
<p>Default value is <code>DockableTabPreference.NONE</code></p>
</procedure>

<procedure title="addMoreOptions" id="addMoreOptions">
<code-block lang="java">void addMoreOptions(JPopupMenu menu)</code-block>
<p>Adds this dockables menu items to the context menu</p>
</procedure>

<procedure title="createHeaderUI" id="createHeaderUI">
<code-block lang="java">DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel)</code-block>
<p>Creates the header UI for this dockable. The default implementation will create the default Modern Docking header.</p>
<p>Default value is <code>DockingInternal.createDefaultHeaderUI(headerController, headerModel);</code></p>
</procedure>

<procedure title="updateProperties" id="updateProperties">
<code-block lang="java">void updateProperties()</code-block>
<p>Modern Docking will call this method after setting the values of any fields annotated with <code>DockingProperty</code>. If there are no fields with that annotation then this method is not called</p>
</procedure>