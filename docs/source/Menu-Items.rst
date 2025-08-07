#############
Menu Items
#############

Modern Docking provides several specialized menu classes explained below

LayoutsMenu
-----------

    Displays a list of all layouts that the layout manager knows about. These are automatically added and removed. When clicked the menu item will load that layout.

ApplicationLayoutMenuItem
-------------------------

    Menu item specific to one layout. Simply displays the name and when clicked loads the layout with :java:`Docking.restore()`

DockableMenuItem
----------------

    Displays a single dockable. Shows a checkmark if the dockable is docked. If the dockable is not docked, docks it, if it is docked, it displays it.

