/*
Copyright (c) 2026 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package demo;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import demo.MiscPanels.FixedPanel;
import demo.MiscPanels.MoreOptionsPanel;
import demo.MiscPanels.NoTabGroupPanel;
import demo.MiscPanels.ScrollablePanel;
import demo.MiscPanels.VetoClosePanel;
import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.app.ApplicationLayoutMenuItem;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.DockableMenuItem;
import io.github.andrewauclair.moderndocking.app.LayoutsMenu;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-app comprehensive demo. Opens two independent frames, each with its own
 * DockingAPI instance and its own set of dockable panels.
 */
public class ComprehensiveDemo {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            DockingUI.initialize();

            ComprehensiveDemoFrame frame1 = new ComprehensiveDemoFrame(
                    "Modern Docking — Comprehensive Demo (Frame 1)",
                    new File("comprehensive_demo_layout_1.xml"),
                    "f1", 100, 100);
            ComprehensiveDemoFrame frame2 = new ComprehensiveDemoFrame(
                    "Modern Docking — Comprehensive Demo (Frame 2)",
                    new File("comprehensive_demo_layout_2.xml"),
                    "f2", 800, 100);

            frame1.setVisible(true);
            frame2.setVisible(true);
        });
    }

    // =========================================================================
    // Per-frame window
    // =========================================================================

    static class ComprehensiveDemoFrame extends JFrame {

        private final DockingAPI docking;
        private final String     prefix;

        private final EditorPanel      editor1;
        private final EditorPanel      editor2;
        private final ProjectPanel     projectTree;
        private final PropertiesPanel  propertiesPanel;
        private final OutputPanel      outputPanel;
        private final EventLogPanel    eventLogPanel;
        private final ScrollablePanel  scrollablePanel;
        private final FixedPanel       fixedPanel;
        private final VetoClosePanel   vetoPanel;
        private final MoreOptionsPanel moreOptionsPanel;
        private final NoTabGroupPanel  noTabGroupPanel;
        private final TestHarnessPanel testHarnessPanel;

        ComprehensiveDemoFrame(String title, File persistFile, String prefix, int defaultX, int defaultY) {
            super(title);
            this.prefix = prefix;

            setSize(1200, 800);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    docking.getAppState().persist();
                    dispose();
                    // only exit if both frames are gone
                    boolean anyVisible = false;
                    for (Frame f : Frame.getFrames()) {
                        if (f.isVisible() && f != ComprehensiveDemoFrame.this) {
                            anyVisible = true;
                            break;
                        }
                    }
                    if (!anyVisible) System.exit(0);
                }
            });

            // Initialize this frame's DockingAPI instance
            docking = new Docking(this);

            // Create all panels (after docking is initialized)
            editor1          = new EditorPanel(docking, prefix + "-editor-1", "Editor 1");
            editor2          = new EditorPanel(docking, prefix + "-editor-2", "Editor 2");
            projectTree      = new ProjectPanel(docking, prefix + "-project-tree");
            propertiesPanel  = new PropertiesPanel(docking, prefix + "-properties-panel");
            outputPanel      = new OutputPanel(docking, prefix + "-output-panel");
            eventLogPanel    = new EventLogPanel(docking, prefix + "-event-log");
            scrollablePanel  = new ScrollablePanel(docking, prefix + "-scrollable-panel");
            fixedPanel       = new FixedPanel(docking, prefix + "-fixed-panel");
            vetoPanel        = new VetoClosePanel(docking, prefix + "-veto-close-panel");
            moreOptionsPanel = new MoreOptionsPanel(docking, prefix + "-more-options-panel");
            noTabGroupPanel  = new NoTabGroupPanel(docking, prefix + "-no-tab-group-panel");

            List<Dockable> allDockables = new ArrayList<>();
            allDockables.add(editor1);
            allDockables.add(editor2);
            allDockables.add(projectTree);
            allDockables.add(propertiesPanel);
            allDockables.add(outputPanel);
            allDockables.add(eventLogPanel);
            allDockables.add(scrollablePanel);
            allDockables.add(fixedPanel);
            allDockables.add(vetoPanel);
            allDockables.add(moreOptionsPanel);
            allDockables.add(noTabGroupPanel);

            testHarnessPanel = new TestHarnessPanel(docking, prefix + "-test-harness", allDockables);

            // Wire event log as docking listener for this instance
            docking.addDockingListener(eventLogPanel);

            // Wire floating frame listener
            docking.addNewFloatingFrameListener(new io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener() {
                @Override
                public void newFrameCreated(javax.swing.JFrame frame, io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI root) {}
                @Override
                public void newFrameCreated(javax.swing.JFrame frame, io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI root, Dockable dockable) {
                    frame.setTitle("Floating — " + dockable.getTitleText());
                }
            });

            // Root panel
            setLayout(new BorderLayout());
            RootDockingPanel root = new RootDockingPanel(docking, this);
            JPanel rootWrapper = new JPanel(new BorderLayout());
            rootWrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            rootWrapper.add(root, BorderLayout.CENTER);
            add(rootWrapper, BorderLayout.CENTER);

            setJMenuBar(buildMenuBar());

            // Default layout
            ApplicationLayout defaultLayout = buildDefaultLayout();
            DockingLayouts.addLayout(prefix + "-default", defaultLayout);

            docking.getAppState().setAutoPersist(true);
            docking.getAppState().setPersistFile(persistFile);
            docking.getAppState().setDefaultApplicationLayout(defaultLayout);

            boolean restored = false;
            try {
                restored = docking.getAppState().restore();
            } catch (DockingLayoutException ex) {
                // fallback to default
            }

            if (!restored) {
                setLocation(defaultX, defaultY);
            }
        }

        // =====================================================================
        // Menu bar
        // =====================================================================

        private JMenuBar buildMenuBar() {
            JMenuBar bar = new JMenuBar();
            bar.add(buildFileMenu());
            bar.add(buildViewMenu());
            bar.add(buildActionsMenu());
            bar.add(buildSettingsMenu());
            bar.add(buildWindowMenu());
            return bar;
        }

        private JMenu buildFileMenu() {
            JMenu menu = new JMenu("File");

            JMenuItem save = new JMenuItem("Save Layout to File...");
            save.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        docking.getLayoutPersistence().saveLayoutToFile(
                                fc.getSelectedFile(),
                                docking.getDockingState().getApplicationLayout());
                    } catch (DockingLayoutException ex) {
                        JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
                    }
                }
            });
            menu.add(save);

            JMenuItem load = new JMenuItem("Load Layout from File...");
            load.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        ApplicationLayout layout = docking.getLayoutPersistence()
                                .loadApplicationLayoutFromFile(fc.getSelectedFile());
                        docking.getDockingState().restoreApplicationLayout(layout);
                    } catch (DockingLayoutException ex) {
                        JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
                    }
                }
            });
            menu.add(load);

            menu.addSeparator();

            JMenuItem storeNamed = new JMenuItem("Store Current Layout as Named...");
            storeNamed.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(this, "Layout name:");
                if (name != null && !name.isBlank()) {
                    DockingLayouts.addLayout(name, docking.getDockingState().getApplicationLayout());
                }
            });
            menu.add(storeNamed);

            return menu;
        }

        private JMenu buildViewMenu() {
            JMenu menu = new JMenu("View");

            menu.add(new DockableMenuItem(docking, editor1.getPersistentID(),         editor1.getTabText()));
            menu.add(new DockableMenuItem(docking, editor2.getPersistentID(),         editor2.getTabText()));
            menu.addSeparator();
            menu.add(new DockableMenuItem(docking, projectTree.getPersistentID(),     projectTree.getTabText()));
            menu.add(new DockableMenuItem(docking, propertiesPanel.getPersistentID(), propertiesPanel.getTabText()));
            menu.add(new DockableMenuItem(docking, outputPanel.getPersistentID(),     outputPanel.getTabText()));
            menu.add(new DockableMenuItem(docking, eventLogPanel.getPersistentID(),   eventLogPanel.getTabText()));
            menu.addSeparator();
            menu.add(new DockableMenuItem(docking, scrollablePanel.getPersistentID(),  scrollablePanel.getTabText()));
            menu.add(new DockableMenuItem(docking, fixedPanel.getPersistentID(),       fixedPanel.getTabText()));
            menu.add(new DockableMenuItem(docking, vetoPanel.getPersistentID(),        vetoPanel.getTabText()));
            menu.add(new DockableMenuItem(docking, moreOptionsPanel.getPersistentID(), moreOptionsPanel.getTabText()));
            menu.add(new DockableMenuItem(docking, noTabGroupPanel.getPersistentID(),  noTabGroupPanel.getTabText()));
            menu.addSeparator();
            menu.add(new DockableMenuItem(docking, testHarnessPanel.getPersistentID(), testHarnessPanel.getTabText()));

            return menu;
        }

        private JMenu buildActionsMenu() {
            JMenu menu = new JMenu("Actions");

            // --- Focused mode (1.5.0) ---
            JMenu focusedMode = new JMenu("Focused Mode");

            JMenu enterFocused = new JMenu("Enter");
            addFocusedModeEnterItem(enterFocused, editor1);
            addFocusedModeEnterItem(enterFocused, editor2);
            focusedMode.add(enterFocused);

            JMenuItem exitFocused = new JMenuItem("Exit (find active)");
            exitFocused.addActionListener(e -> exitFocusedModeForAny());
            focusedMode.add(exitFocused);

            menu.add(focusedMode);
            menu.addSeparator();

            // --- Float in new window ---
            JMenu floatMenu = new JMenu("Float in New Window");
            addFloatItem(floatMenu, editor1);
            addFloatItem(floatMenu, editor2);
            addFloatItem(floatMenu, projectTree);
            addFloatItem(floatMenu, propertiesPanel);
            addFloatItem(floatMenu, outputPanel);
            menu.add(floatMenu);

            // --- Display ---
            JMenu displayMenu = new JMenu("Display (if not docked)");
            addDisplayItem(displayMenu, scrollablePanel);
            addDisplayItem(displayMenu, fixedPanel);
            addDisplayItem(displayMenu, vetoPanel);
            addDisplayItem(displayMenu, moreOptionsPanel);
            addDisplayItem(displayMenu, noTabGroupPanel);
            menu.add(displayMenu);

            menu.addSeparator();

            // --- Default display region (1.5.0) ---
            JMenu regionMenu = new JMenu("Default Display Region");
            ButtonGroup regionGroup = new ButtonGroup();
            for (DockingRegion region : DockingRegion.values()) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                        region.name(), region == DockingRegion.NORTH);
                item.addActionListener(e -> {
                    // TODO 1.5.0 — Settings.setDefaultDisplayRegion(region) not yet available
                });
                regionGroup.add(item);
                regionMenu.add(item);
            }
            menu.add(regionMenu);

            menu.addSeparator();

            // --- Auto-hide shortcuts ---
            JMenu autoHideMenu = new JMenu("Auto-Hide to Toolbar");
            addAutoHideItem(autoHideMenu, editor1,        ToolbarLocation.WEST);
            addAutoHideItem(autoHideMenu, outputPanel,    ToolbarLocation.SOUTH);
            addAutoHideItem(autoHideMenu, propertiesPanel, ToolbarLocation.EAST);
            menu.add(autoHideMenu);

            menu.addSeparator();

            // --- bringToFront ---
            JMenu bringToFrontMenu = new JMenu("Bring to Front");
            addBringToFrontItem(bringToFrontMenu, editor1);
            addBringToFrontItem(bringToFrontMenu, editor2);
            menu.add(bringToFrontMenu);

            menu.addSeparator();

            JMenuItem changeTabText = new JMenuItem("Randomize Editor 1 Tab Text");
            changeTabText.addActionListener(e -> {
                String newText = "Editor " + (char) ('A' + (int) (Math.random() * 26));
                editor1.setTabText(newText);
                docking.updateTabInfo(editor1.getPersistentID());
            });
            menu.add(changeTabText);

            return menu;
        }

        private void addFocusedModeEnterItem(JMenu menu, Dockable d) {
            JMenuItem item = new JMenuItem(d.getTabText());
            item.addActionListener(e -> docking.enterFocusedMode(d));
            menu.add(item);
        }

        private void exitFocusedModeForAny() {
            for (Dockable d : new Dockable[]{editor1, editor2,
                    projectTree, propertiesPanel, outputPanel, eventLogPanel,
                    scrollablePanel, fixedPanel, vetoPanel, moreOptionsPanel, noTabGroupPanel}) {
                if (docking.inFocusedMode(d)) {
                    docking.exitFocusedMode(d);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "No dockable is currently in focused mode.");
        }

        private void addFloatItem(JMenu menu, Dockable d) {
            JMenuItem item = new JMenuItem(d.getTabText());
            item.addActionListener(e -> {
                if (docking.isDocked(d)) {
                    docking.newWindow(d);
                } else {
                    JOptionPane.showMessageDialog(this, d.getTabText() + " is not currently docked.");
                }
            });
            menu.add(item);
        }

        private void addDisplayItem(JMenu menu, Dockable d) {
            JMenuItem item = new JMenuItem(d.getTabText());
            item.addActionListener(e -> docking.display(d));
            menu.add(item);
        }

        private void addAutoHideItem(JMenu menu, Dockable d, ToolbarLocation loc) {
            JMenuItem item = new JMenuItem(d.getTabText() + " \u2192 " + loc.name());
            item.addActionListener(e -> {
                if (docking.isDocked(d)) {
                    docking.autoHideDockable(d, loc, this);
                } else {
                    JOptionPane.showMessageDialog(this, d.getTabText() + " is not currently docked.");
                }
            });
            menu.add(item);
        }

        private void addBringToFrontItem(JMenu menu, Dockable d) {
            JMenuItem item = new JMenuItem(d.getTabText());
            item.addActionListener(e -> docking.bringToFront(d));
            menu.add(item);
        }

        private JMenu buildSettingsMenu() {
            JMenu menu = new JMenu("Settings");

            JCheckBoxMenuItem alwaysTabs = new JCheckBoxMenuItem("Always Display Tabs Mode");
            alwaysTabs.addActionListener(e ->
                    Settings.setDefaultTabPreference(alwaysTabs.isSelected()
                            ? DockableTabPreference.BOTTOM_ALWAYS
                            : DockableTabPreference.BOTTOM));
            menu.add(alwaysTabs);

            JCheckBoxMenuItem highlighter = new JCheckBoxMenuItem(
                    "Active Dockable Highlighter", Settings.isActiveHighlighterEnabled());
            highlighter.addActionListener(e ->
                    Settings.setActiveHighlighterEnabled(highlighter.isSelected()));
            menu.add(highlighter);

            JCheckBoxMenuItem scrollTabs = new JCheckBoxMenuItem("Scroll Tab Layout (unchecked = Wrap)", true);
            scrollTabs.addActionListener(e ->
                    Settings.setTabLayoutPolicy(scrollTabs.isSelected()
                            ? JTabbedPane.SCROLL_TAB_LAYOUT
                            : JTabbedPane.WRAP_TAB_LAYOUT));
            menu.add(scrollTabs);

            menu.addSeparator();

            JCheckBoxMenuItem layeredOverlay = new JCheckBoxMenuItem("Use Layered Pane Overlay (FloatUtilsLayer)");
            layeredOverlay.addActionListener(e ->
                    DockingSettings.setUseLayeredPaneOverlay(layeredOverlay.isSelected()));
            menu.add(layeredOverlay);

            menu.addSeparator();

            JMenuItem lightTheme = new JMenuItem("FlatLaf Light Theme");
            lightTheme.addActionListener(e -> {
                FlatLightLaf.setup();
                DockingUI.initialize();
                for (Frame f : Frame.getFrames()) {
                    SwingUtilities.updateComponentTreeUI(f);
                }
            });
            menu.add(lightTheme);

            JMenuItem darkTheme = new JMenuItem("FlatLaf Dark Theme");
            darkTheme.addActionListener(e -> {
                FlatDarkLaf.setup();
                DockingUI.initialize();
                for (Frame f : Frame.getFrames()) {
                    SwingUtilities.updateComponentTreeUI(f);
                }
            });
            menu.add(darkTheme);

            return menu;
        }

        private JMenu buildWindowMenu() {
            JMenu menu = new JMenu("Window");
            menu.add(new ApplicationLayoutMenuItem(docking, prefix + "-default", "Restore Default Layout"));
            menu.addSeparator();
            menu.add(new LayoutsMenu(docking));
            return menu;
        }

        private ApplicationLayout buildDefaultLayout() {
            WindowLayoutBuilderAPI builder = new WindowLayoutBuilder(docking, editor1.getPersistentID())
                    .dock(editor2.getPersistentID(), editor1.getPersistentID(), DockingRegion.CENTER)
                    .dockToRoot(projectTree.getPersistentID(),     DockingRegion.WEST,  0.20)
                    .dockToRoot(outputPanel.getPersistentID(),     DockingRegion.SOUTH, 0.25)
                    .dockToRoot(propertiesPanel.getPersistentID(), DockingRegion.EAST,  0.22)
                    .dock(eventLogPanel.getPersistentID(), propertiesPanel.getPersistentID(), DockingRegion.CENTER)
                    .display(editor1.getPersistentID());

            return builder.buildApplicationLayout();
        }
    }
}
