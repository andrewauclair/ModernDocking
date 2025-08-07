#############
Look and Feel
#############

Colors
======

Modern Docking handles all colors by using properties in the UIManager. For all the colors listed here, Modern Docking will first attempt to use the Modern Docking property name, then the theme color or a custom configured property from the user. If none of these are found, Modern Docking will default to a predefined color.

Docking Handle Background
-------------------------

    This setting controls the color used for the background of Docking Handles.

    UIManager property used for the background color on Docking Handles. This property can be modified by calling ``DockingSettings.setHandleBackgroundProperty``

    Modern Docking property: ``ModernDocking.handleBackground``

    Default UIManager property: ``TableHeader.background``

Docking Handle Foreground
-------------------------

    This setting controls the color used for the foreground of Docking Handles. The foreground color is used both for the borders and the mouse over color.

    UIManager property used for the foreground color on Docking Handles. This property can be modified by calling ``DockingSettings.setHandleForegroundProperty``

    Modern Docking property: ``ModernDocking.handleForeground``

    Default UIManager property: ``TableHeader.foreground``

Dockable Header Background
--------------------------

    This setting controls the color used for the background of the default dockable header provided by Modern Docking.

    This property can be modified by calling ``DockingSettings.setHeaderBackgroundProperty``

    Modern Docking property: ``ModernDocking.headerBackground``

    Default UIManager property: ``TableHeader.background``

Dockable Header Foreground
--------------------------

    This setting controls the color used for the foreground of the default dockable header provided by Modern Docking.

    This property can be modified by calling ``DockingSettings.setHeaderForegroundProperty``

    Modern Docking property: ``ModernDocking.headerForeground``

    Default UIManager property: ``TableHeader.foreground``

Docking Overlay Background
--------------------------

    This setting controls the color used for the background of the docking overlay. An alpha value less than 100% is typically used for this color.

    This property can be modified by calling ``DockingSettings.setOverlayBackgroundProperty``

    Modern Docking property: ``ModernDocking.overlayBackground``

Active Dockable Highlighter Selected Border Color
-------------------------------------------------

    This setting controls the color used for the border color when the mouse is over a dockable and the active dockable highlighter is enabled.

    This property can be modified by calling ``DockingSettings.setHighlighterSelectedBorderProperty``

    Modern Docking property: ``ModernDocking.highlighterSelectedBorder``

    Default UIManager property: ``Component.focusColor``

Active Dockable Highlighter Not Selected Border Color
-----------------------------------------------------

    This setting controls the color used to reset to the default border when a dockable is no longer under the mouse. Modern Docking will use this color when the active dockable highlighter is active. The color should be set to the theme's default border color.

    This property can be modified by calling ``DockingSettings.setHighlighterNotSelectedBorderProperty``

    Modern Docking property: ``ModernDocking.highlighterNotSelectedBorder``

    Default UIManager property: ``Component.borderColor``
