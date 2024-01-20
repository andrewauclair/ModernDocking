# User Facing Features

Users of your application will interact with several UI components provided by Modern Docking.
The framework is built around using Java Swing's `JTabbedPane` and `JSplitPane` to provide the docking layouts.
The positioning of the applications dockable components within these `JTabbedPane`s and `JSplitPane`s is done by dragging the dockables.
When the dockable is being dragged it will float above the window of the application and show Docking Handles and Docking Regions.
Docking Handles allow the user to place the dockable precisely where they want it and Docking Regions provide extra visual feedback of this action.
Docking Regions are also a quick way to "snap" dockables into place without needing to precisely drag to a Docking Handle.

## Using Docking Handles to Dock
When dragging a dockable hovering over another dockable will display docking handles in the center of the dockable.
These handles provide easy access to dock to the North, South, East and West regions.



![Docking Handles](docking_handles.gif)

The dockable can also be docked to any of these regions by hovering over the region itself and dropping the dockable.


![Docking Regions](docking_regions.gif)

The root also has North, South, East and West handles to dock the dockable directly to the root of the panel.


![Root Docking Handles](root_docking_handles.gif)

## Adjusting Split
Splits can be adjusted and perform continuous layout. Double-clicking the split will return it to 50-50 split for the 2 sides.


![Adjusting Splits](adjusting_split.gif)

## Creating Tab Group
Panels can be grouped into tabbed panes by dragging a dockable to the center region of another dockable.


![Creating Tab Group](creating_tab_group.gif)

## Closing Panels
Panels can be closed using the X button on their headers. This option can be disabled in the source code.


![Closing Panels](close_panel.gif)

## Floating a Panel
Panels can be floated as their own window by dragging them by their header and dropping them outside the frame.
This creates a new JFrame with the dockable in it. More dockables can then be docked to this dockable.


![Floating Panels](floating_panel.gif)

## Pinning a Panel
Panels can be set to unpinned with View Mode > Unpinned from the settings button on the panel header.
This option will display the panel on a side toolbar as a button which can be pressed to display the panel.
Clicking outside the panel will return it to the toolbar. To pin the panel and return it to normal, select the View Mode > Pinned option.

![Pinning Panel](pinning_panel.gif)


## Option Panel in New Window
Panels can be opened in their own window using View Mode > Window.

![New Window](new_window.gif)


## Additional Settings Options
Display custom settings options on the settings menu.


![Additional Settings Options](has_more_options.gif)