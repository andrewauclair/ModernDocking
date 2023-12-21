# Customization Points

## Docking Settings

### Handle Colors
background and foreground

## Tab Placement

By default, Modern Docking places all tabs in `JTabbePane`s at the bottom. This can be changed using `Settings.alwaysDisplayTabsMode` which will switch the tab placement to the top.


## Tab Layout Policy

The tab layout policy of `JTabbedPane` can be customized with `Settings.setTabLayoutPolicy`.
The default tab layout policy is `JTabbedPane.SCROLL_TAB_LAYOUT` and it can be set to either `JTabbedPane.SCROLL_TAB_LAYOUT` or `JTabbedPane.WRAP_TAB_LAYOUT`.

## Custom Dockable Header

Create your own implementation of the header UI and return it in Dockable.