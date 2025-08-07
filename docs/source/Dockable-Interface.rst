.. role:: java(code)
    :language: java

##################
Dockable Interface
##################

The ``Dockable`` interface is required to be implemented by any component that will be dockable.

Typically, this interface is implemented by a class that extends from ``JPanel``.

The ``Dockable`` interface has a minimum subset of methods that must be implemented by the application and a set of methods that are optional.
These optional methods provide extra customization points for the application per dockable.

.. warning::
    The return values for the ``Dockable`` interface methods should be constant after the creation of the dockable component.
    Certain methods can be dynamic and the framework provides methods to update their values within the framework. Any method that doesn't specify such
    a method can lead to unexpected behavior if changed after the dockable component is created.

    Generally, if you wish to modify the return values after creating the dockable component, do so while the dockable is not docked. This can be checked with :java:`Docking.isDocked`.

-----------------
Mandatory Methods
-----------------

These two methods do not provide default implementations in the interface and must be implemented by the application.

^^^^^^^^^^^^^
Persistent ID
^^^^^^^^^^^^^

.. code-block:: java

    String persistentID()

Provides a unique ID for the dockable to use within Modern Docking

^^^^^^^^
Tab Text
^^^^^^^^

.. code-block:: java

    String getTabText()

Provides the text that will be displayed on a tab when the dockable is in a ``JTabbedPane``.

.. important::
    If the text displayed on the tab ever changes, the application must call :java:`Docking.updateTabText` with the dockables persistentID to force the framework to update the text. This should be done anytime the text is changed, just in case the dockable is displaying in a JTabbedPane. Undocking and docking the dockable again will also update the tab text.

----------------
Optional Methods
----------------

The following methods are provided to the application with a default. This means the application only needs to implement the methods
if it wishes to change the default.

^^^^
Type
^^^^

.. code-block:: java

    int getType()

:Default: 0

Provides an int to Modern Docking. This represents a unique type category for the dockable. Modern Docking will use this value when docking to determine which dockables to dock to.

^^^^^^^^^^
Title Text
^^^^^^^^^^

.. code-block:: java

    String getTitleText()

:Default: value of :java:`getTabText`

Provides the text that Modern Docking should display on the header. This string can be different than ``getTabText``

^^^^^^^^^^^
Tab Tooltip
^^^^^^^^^^^

.. code-block:: java

    String getTabTooltip()

:Default: :java:`null`

Used by the framework to get the text to display as a tooltip on :java:`JTabbedPane` tabs.

^^^^^^^^^^^
Icon
^^^^^^^^^^^

.. code-block:: java

    Icon getIcon()

:Default: :java:`null`

Used by the framework to get the icon for the dockable to use in a ``JTabbedPane`` tab.

^^^^^^^^^^^^^^^^^^^
Is Floating Allowed
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    boolean isFloatingAllowed()

:Default: :java:`true`

Tells Modern Docking if the dockable is allowed to be opened in its own window

^^^^^^^^^^^^^^^^^^^^
Is Limited to Window
^^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    boolean isLimitedToWindow()

:Default: :java:`false`

Allows the application to limit the dockable to the window it was initially docked in.

^^^^^^^^^^^
Style
^^^^^^^^^^^

.. code-block:: java

    DockableStyle getStyle()

:Default: :java:`DockableStyle.BOTH`

The docking style of the dockable which can be :java:`DockableStyle.VERTICAL`, :java:`DockableStyle.HORIZONTAL`, :java:`DockableStyle.BOTH` or :java:`DockableStyle.CENTER_ONLY`. Modern Docking will use this to determine which docking regions to allow when docking this dockable. Docking handles that do not match this style will be hidden.

^^^^^^^^^^^^^^^
Auto Hide Style
^^^^^^^^^^^^^^^

.. code-block:: java

    DockableStyle getAutoHideStyle()

:Default: :java:`DockableStyle.BOTH`

Determines which toolbars this dockable can be displayed on. Uses the same values as :java:`getStyle`. :java:`DockableStyle.VERTICAL` will allow the dockable on the east and west auto hide toolbars. :java:`DockableStyle.HORIZONTAL` will allow the dockable on the south auto hide toolbar. :java:`DockableStyle.CENTER_ONLY` is invalid for this method.

^^^^^^^^^^^
Closable
^^^^^^^^^^^

.. code-block:: java

    boolean isClosable()

:Default: :java:`true`

Indicates to the docking framework whether the Dockable component can be closed and undocked.

^^^^^^^^^^^^^^^
Request Close
^^^^^^^^^^^^^^^

.. code-block:: java

    boolean requestClose()

:Default: :java:`true`

Called by Modern Docking when the dockable is in the process of closing due to :java:`undock`. This allows the application to stop the dockable from closing. For example, maybe the user has unsaved changes and the application wishes to confirm closing of the dockable.

^^^^^^^^^^^^^^^^^^^
Auto Hide Allowed
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    boolean isAutoHideAllowed()

:Default: :java:`false`

Determines if the dockable can be set to the auto hide toolbars.

^^^^^^^^^^^^^^^
Min Max Allowed
^^^^^^^^^^^^^^^

.. code-block:: java

    boolean isMinMaxAllowed()

:Default: :java:`false`

Determines if the dockable can be maximized so that it takes up all the space in the window.

^^^^^^^^^^^^^^^^^^^^^^^
Wrappable in Scrollpane
^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    boolean isWrappableInScrollpane()

:Default: :java:`false`

Allows the application to specify whether the docking framework should automatically wrap the Dockable component in a :java:`JScrollPane`.

^^^^^^^^^^^^^^^^^^^
Has More Options
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    boolean getHasMoreOptions()

:Default: :java:`false`

Flag that tells Modern Docking that this dockable has more menu items it wishes to add to the context menu. If this method returns true then Modern Docking will call :java:`addMoreOptions`

^^^^^^^^^^^^^^^
Tab Preference
^^^^^^^^^^^^^^^

.. code-block:: java

    DockableTabPreference getTabPreference()

:Default: :java:`DockableTabPreference.NONE`

Gives the dockables preferred tab location when in a :java:`JTabbedPane`

^^^^^^^^^^^^^^^^^^^
Add More Options
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    void addMoreOptions(JPopupMenu menu)

Adds this dockables menu items to the context menu

^^^^^^^^^^^^^^^^^^^
Create Header UI
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel)

:Default: :java:`DockingInternal.createDefaultHeaderUI(headerController, headerModel)`

Creates the header UI for this dockable. The default implementation will create the default Modern Docking header.

^^^^^^^^^^^^^^^^^^^
Update Properties
^^^^^^^^^^^^^^^^^^^

.. code-block:: java

    void updateProperties()

Modern Docking will call this method after setting the values of any fields annotated with :java:`DockingProperty`. If there are no fields with that annotation then this method is not called
