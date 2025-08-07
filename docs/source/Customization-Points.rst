####################
Customization Points
####################

-------------
Tab Placement
-------------

By default, Modern Docking places all tabs in a ``JTabbedPane`` at the bottom. This can be changed using ``Settings.alwaysDisplayTabsMode`` which will switch the tab placement to the top.

-----------------
Tab Layout Policy
-----------------

The tab layout policy of ``JTabbedPane`` can be customized with ``Settings.setTabLayoutPolicy``.
The default tab layout policy is ``JTabbedPane.SCROLL_TAB_LAYOUT`` and it can be set to either ``JTabbedPane.SCROLL_TAB_LAYOUT`` or ``JTabbedPane.WRAP_TAB_LAYOUT``.

------------------
Active Highlighter
------------------

By default, Modern Docking will highlight the dockable that the house is currently over. This can be disabled before initializing the docking framework by using ``Settings.setActiveHighlighterEnabled``.

----------------------
Custom Dockable Header
----------------------

Create your own implementation of the header UI and return it in Dockable.