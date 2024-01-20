# Customization Points

## Docking Color Settings

### Handle Colors

<p>Modern Docking provides options to control the look and feel of Docking Handles and Docking Overlay. This is done through the <code>UIManager</code></p>

<procedure title="Dockable Header" id="dockableHeader">
<table>
<tr>
<td><code>ModernDocking.titlebar.border.enabled</code></td>
<td>Enable or disable the border around the default dockable header. Enabled by default</td>
</tr>
<tr>
<td><code>ModernDocking.titlebar.background.color</code></td>
<td>Set the background color of the default dockable header</td>
</tr>
<tr>
<td><code>ModernDocking.titlebar.border.color</code></td>
<td>Set the color of the default dockable header border. Used if the border is enabled</td>
</tr>
<tr>
<td><code>ModernDocking.titlebar.border.size</code></td>
<td>Set the thickness of the default dockable header border. Used if the border is enabled</td>
</tr>
</table>
</procedure>

<procedure title="Docking Handles" id="dockingHandles">
<table>
<tr>
<td><code>ModernDocking.handles.background</code></td>
<td>Set color used for the docking handles</td>
</tr>
<tr>
<td><code>ModernDocking.handles.background.border</code></td>
<td>Set the border color used for the docking handles</td>
</tr>
<tr>
<td><code>ModernDocking.handles.foreground</code></td>
<td>Set the foreground color used for the docking handles</td>
</tr>
<tr>
<td><code>ModernDocking.handles.fill</code></td></tr>
<td>Set the fill color used for the docking handles</td>
</table>
</procedure>

<procedure title="Docking Overlay" id="dockingOverlay">
<table>
<tr>
<td><code>ModernDocking.overlay.color</code></td>
<td>Set the color used for the DOcking Overlay</td>
</tr>
<tr>
<td><code>ModernDocking.overlay.border.color</code></td>
<td>Set the border color used for the Docking Overlay</td>
</tr>
<tr>
<td><code>ModernDocking.overlay.alpha</code></td>
<td>Set the amount of alpha to use for the Docking Overlay</td>
</tr>
</table>
</procedure>

## Tab Placement

By default, Modern Docking places all tabs in `JTabbePane`s at the bottom. This can be changed using `Settings.alwaysDisplayTabsMode` which will switch the tab placement to the top.


## Tab Layout Policy

The tab layout policy of `JTabbedPane` can be customized with `Settings.setTabLayoutPolicy`.
The default tab layout policy is `JTabbedPane.SCROLL_TAB_LAYOUT` and it can be set to either `JTabbedPane.SCROLL_TAB_LAYOUT` or `JTabbedPane.WRAP_TAB_LAYOUT`.

## Custom Dockable Header

Create your own implementation of the header UI and return it in Dockable.