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

/*
 * ComprehensiveDemo.java — single-app entry point
 *
 * Features demonstrated:
 *   Core:
 *     - Drag-to-dock (all regions), root handles, tab groups, floating
 *     - Programmatic dock / undock / display / bringToFront / newWindow
 *     - Auto-hide (pin) to WEST / SOUTH / EAST toolbars
 *     - isClosable, requestClose veto, DockableStyle, isWrappableInScrollpane
 *     - hasMoreOptions / addMoreOptions
 *     - DockingProperty annotation + updateProperties
 *     - DockingListener (event log with per-type counters)
 *     - NewFloatingFrameListener
 *     - AppState auto-persistence
 *     - LayoutPersistence (save/load file)
 *     - DockingLayouts named layouts + ApplicationLayoutMenuItem + LayoutsMenu
 *     - WindowLayoutBuilder programmatic default layout
 *     - Settings: tab preference, tab layout policy, active highlighter
 *     - DockingUI theme switching (FlatLaf light / dark)
 *
 *   1.5.0 additions (marked // TODO 1.5.0 at stubs, no marker where already live):
 *     - enterFocusedMode / exitFocusedMode / inFocusedMode
 *     - DockingEvent.ID.FOCUSED_MODE_ENTERED / FOCUSED_MODE_EXITED
 *     - isTabGroupAllowed() — stubbed, not yet in Dockable interface
 *     - Settings.setDefaultDisplayRegion — stubbed, not yet in Settings
 *     - DockingSettings.setUseLayeredPaneOverlay — live
 */

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
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.app.AppState;
import io.github.andrewauclair.moderndocking.app.ApplicationLayoutMenuItem;
import io.github.andrewauclair.moderndocking.app.DockableMenuItem;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.DockingState;
import io.github.andrewauclair.moderndocking.app.LayoutPersistence;
import io.github.andrewauclair.moderndocking.app.LayoutsMenu;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.app.WindowLayoutBuilder;
import io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class ComprehensiveDemo extends JFrame {

    private static final File PERSIST_FILE = new File("comprehensive_demo_layout.xml");

    // All declared null — initialized after Docking.initialize(this)
    private EditorPanel editor1;
    private EditorPanel editor2;
    private EditorPanel editor3;
    private ProjectPanel projectTree;
    private PropertiesPanel propertiesPanel;
    private OutputPanel outputPanel;
    private EventLogPanel eventLogPanel;
    private ScrollablePanel scrollablePanel;
    private FixedPanel fixedPanel;
    private VetoClosePanel vetoPanel;
    private MoreOptionsPanel moreOptionsPanel;
    private NoTabGroupPanel noTabGroupPanel;
    private TestHarnessPanel testHarnessPanel;

    public ComprehensiveDemo() {
        super("Modern Docking — Comprehensive Demo");

        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AppState.persist();
                dispose();
                System.exit(0);
            }
        });

        // Must be called before any Docking.registerDockable() call
        Docking.initialize(this);
        DockingUI.initialize();

        // Create all panels now that the framework is ready
        editor1 = new EditorPanel("editor-1", "Editor 1");
        editor2 = new EditorPanel("editor-2", "Editor 2");
        editor3 = new EditorPanel("editor-3", "Editor 3");
        projectTree = new ProjectPanel();
        propertiesPanel = new PropertiesPanel();
        outputPanel = new OutputPanel();
        eventLogPanel = new EventLogPanel();
        scrollablePanel = new ScrollablePanel();
        fixedPanel = new FixedPanel();
        vetoPanel = new VetoClosePanel();
        moreOptionsPanel = new MoreOptionsPanel();
        noTabGroupPanel = new NoTabGroupPanel();

        List<Dockable> allDockables = new ArrayList<>();

        allDockables.add(editor1);
        allDockables.add(editor2);
        allDockables.add(editor3);
        allDockables.add(projectTree);
        allDockables.add(propertiesPanel);
        allDockables.add(outputPanel);
        allDockables.add(eventLogPanel);
        allDockables.add(scrollablePanel);
        allDockables.add(fixedPanel);
        allDockables.add(vetoPanel);
        allDockables.add(moreOptionsPanel);
        allDockables.add(noTabGroupPanel);

        testHarnessPanel = new TestHarnessPanel(allDockables);

        // Wire event log as a docking listener
        Docking.addDockingListener(eventLogPanel);

        // Wire floating frame listener to stamp new frame titles
        Docking.addNewFloatingFrameListener(new NewFloatingFrameListener() {
            @Override
            public void newFrameCreated(JFrame frame, RootDockingPanelAPI root) {
            }

            @Override
            public void newFrameCreated(JFrame frame, RootDockingPanelAPI root, Dockable dockable) {
                frame.setTitle("Floating — " + dockable.getTitleText());
            }
        });

        // Root panel
        setLayout(new BorderLayout());
        RootDockingPanel root = new RootDockingPanel(this);
        JPanel rootWrapper = new JPanel(new BorderLayout());
        rootWrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        rootWrapper.add(root, BorderLayout.CENTER);
        add(rootWrapper, BorderLayout.CENTER);

        // Menu bar
        setJMenuBar(buildMenuBar());

        // Default layout
        ApplicationLayout defaultLayout = buildDefaultLayout();
        DockingLayouts.addLayout("default", defaultLayout);

        // Auto-persistence
        AppState.setAutoPersist(true);
        AppState.setPersistFile(PERSIST_FILE);
        AppState.setDefaultApplicationLayout(defaultLayout);

        boolean restored = false;
        try {
            restored = AppState.restore();
        }
        catch (DockingLayoutException ex) {
            // Default layout is applied automatically as fallback
        }

        if (!restored) {
            setLocationRelativeTo(null);
        }
    }

    // =========================================================================
    // Menu bar
    // =========================================================================

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
                    LayoutPersistence.saveLayoutToFile(fc.getSelectedFile(),
                            DockingState.getApplicationLayout());
                }
                catch (DockingLayoutException ex) {
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
                    ApplicationLayout layout =
                            LayoutPersistence.loadApplicationLayoutFromFile(fc.getSelectedFile());
                    DockingState.restoreApplicationLayout(layout);
                }
                catch (DockingLayoutException ex) {
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
                DockingLayouts.addLayout(name, DockingState.getApplicationLayout());
            }
        });
        menu.add(storeNamed);

        menu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");

        exit.addActionListener(e -> {
            AppState.persist();
            dispose();
            System.exit(0);
        });
        menu.add(exit);

        return menu;
    }

    private JMenu buildViewMenu() {
        JMenu menu = new JMenu("View");

        menu.add(new DockableMenuItem(editor1.getPersistentID(), editor1.getTabText()));
        menu.add(new DockableMenuItem(editor2.getPersistentID(), editor2.getTabText()));
        menu.add(new DockableMenuItem(editor3.getPersistentID(), editor3.getTabText()));
        menu.addSeparator();
        menu.add(new DockableMenuItem(projectTree.getPersistentID(), projectTree.getTabText()));
        menu.add(new DockableMenuItem(propertiesPanel.getPersistentID(), propertiesPanel.getTabText()));
        menu.add(new DockableMenuItem(outputPanel.getPersistentID(), outputPanel.getTabText()));
        menu.add(new DockableMenuItem(eventLogPanel.getPersistentID(), eventLogPanel.getTabText()));
        menu.addSeparator();
        menu.add(new DockableMenuItem(scrollablePanel.getPersistentID(), scrollablePanel.getTabText()));
        menu.add(new DockableMenuItem(fixedPanel.getPersistentID(), fixedPanel.getTabText()));
        menu.add(new DockableMenuItem(vetoPanel.getPersistentID(), vetoPanel.getTabText()));
        menu.add(new DockableMenuItem(moreOptionsPanel.getPersistentID(), moreOptionsPanel.getTabText()));
        menu.add(new DockableMenuItem(noTabGroupPanel.getPersistentID(), noTabGroupPanel.getTabText()));
        menu.addSeparator();
        menu.add(new DockableMenuItem(testHarnessPanel.getPersistentID(), testHarnessPanel.getTabText()));

        return menu;
    }

    private JMenu buildActionsMenu() {
        JMenu menu = new JMenu("Actions");
        JMenu focusedMode = new JMenu("Focused Mode");
        JMenu enterFocused = new JMenu("Enter");

        addFocusedModeEnterItem(enterFocused, editor1);
        addFocusedModeEnterItem(enterFocused, editor2);
        addFocusedModeEnterItem(enterFocused, editor3);

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
        addFloatItem(floatMenu, editor3);
        addFloatItem(floatMenu, projectTree);
        addFloatItem(floatMenu, propertiesPanel);
        addFloatItem(floatMenu, outputPanel);

        menu.add(floatMenu);

        // --- Display (undocked → layout) ---
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

        addAutoHideItem(autoHideMenu, editor1, ToolbarLocation.WEST);
        addAutoHideItem(autoHideMenu, outputPanel, ToolbarLocation.SOUTH);
        addAutoHideItem(autoHideMenu, propertiesPanel, ToolbarLocation.EAST);
        menu.add(autoHideMenu);

        menu.addSeparator();

        // --- bringToFront ---
        JMenu bringToFrontMenu = new JMenu("Bring to Front");

        addBringToFrontItem(bringToFrontMenu, editor1);
        addBringToFrontItem(bringToFrontMenu, editor2);
        addBringToFrontItem(bringToFrontMenu, editor3);

        menu.add(bringToFrontMenu);

        menu.addSeparator();

        // --- Live tab text update ---
        JMenuItem changeTabText = new JMenuItem("Randomize Editor 1 Tab Text");

        changeTabText.addActionListener(e -> {
            String newText = "Editor " + (char) ('A' + (int) (Math.random() * 26));
            editor1.setTabText(newText);
            Docking.updateTabInfo(editor1.getPersistentID());
        });
        menu.add(changeTabText);

        return menu;
    }

    private void addFocusedModeEnterItem(JMenu menu, Dockable d) {
        JMenuItem item = new JMenuItem(d.getTabText());
        item.addActionListener(e -> Docking.enterFocusedMode(d));
        menu.add(item);
    }

    private void exitFocusedModeForAny() {
        for (Dockable d : new Dockable[]{editor1, editor2, editor3,
                projectTree, propertiesPanel, outputPanel, eventLogPanel,
                scrollablePanel, fixedPanel, vetoPanel, moreOptionsPanel, noTabGroupPanel}) {
            if (Docking.inFocusedMode(d)) {
                Docking.exitFocusedMode(d);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No dockable is currently in focused mode.");
    }

    private void addFloatItem(JMenu menu, Dockable d) {
        JMenuItem item = new JMenuItem(d.getTabText());

        item.addActionListener(e -> {
            if (Docking.isDocked(d)) {
                Docking.newWindow(d);
            }
            else {
                JOptionPane.showMessageDialog(this, d.getTabText() + " is not currently docked.");
            }
        });
        menu.add(item);
    }

    private void addDisplayItem(JMenu menu, Dockable d) {
        JMenuItem item = new JMenuItem(d.getTabText());
        item.addActionListener(e -> Docking.display(d));
        menu.add(item);
    }

    private void addAutoHideItem(JMenu menu, Dockable d, ToolbarLocation loc) {
        JMenuItem item = new JMenuItem(d.getTabText() + " → " + loc.name());

        item.addActionListener(e -> {
            if (Docking.isDocked(d)) {
                Docking.autoHideDockable(d, loc, this);
            }
            else {
                JOptionPane.showMessageDialog(this, d.getTabText() + " is not currently docked.");
            }
        });
        menu.add(item);
    }

    private void addBringToFrontItem(JMenu menu, Dockable d) {
        JMenuItem item = new JMenuItem(d.getTabText());
        item.addActionListener(e -> Docking.bringToFront(d));
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

        // Layered pane overlay — 1.5.0 (DockingSettings.setUseLayeredPaneOverlay is live)
        JCheckBoxMenuItem layeredOverlay = new JCheckBoxMenuItem("Use Layered Pane Overlay (FloatUtilsLayer)");
        layeredOverlay.addActionListener(e ->
                DockingSettings.setUseLayeredPaneOverlay(layeredOverlay.isSelected()));
        menu.add(layeredOverlay);

        menu.addSeparator();

        JMenuItem lightTheme = new JMenuItem("FlatLaf Light Theme");
        lightTheme.addActionListener(e -> {
            FlatLightLaf.setup();
            DockingUI.initialize();
            SwingUtilities.updateComponentTreeUI(this);
        });
        menu.add(lightTheme);

        JMenuItem darkTheme = new JMenuItem("FlatLaf Dark Theme");
        darkTheme.addActionListener(e -> {
            FlatDarkLaf.setup();
            DockingUI.initialize();
            SwingUtilities.updateComponentTreeUI(this);
        });
        menu.add(darkTheme);

        return menu;
    }

    private JMenu buildWindowMenu() {
        JMenu menu = new JMenu("Window");
        menu.add(new ApplicationLayoutMenuItem("default", "Restore Default Layout"));
        menu.addSeparator();
        menu.add(new LayoutsMenu());
        return menu;
    }

    // =========================================================================
    // Default layout
    // =========================================================================

    private ApplicationLayout buildDefaultLayout() {
        WindowLayoutBuilderAPI builder = new WindowLayoutBuilder(editor1.getPersistentID())
                .dock(editor2.getPersistentID(), editor1.getPersistentID(), DockingRegion.CENTER)
                .dock(editor3.getPersistentID(), editor1.getPersistentID(), DockingRegion.CENTER)
                .dockToRoot(projectTree.getPersistentID(), DockingRegion.WEST, 0.20)
                .dockToRoot(outputPanel.getPersistentID(), DockingRegion.SOUTH, 0.25)
                .dockToRoot(propertiesPanel.getPersistentID(), DockingRegion.EAST, 0.22)
                .dock(eventLogPanel.getPersistentID(), propertiesPanel.getPersistentID(), DockingRegion.CENTER)
                .display(editor1.getPersistentID());

        return builder.buildApplicationLayout();
    }

    // =========================================================================
    // Entry point
    // =========================================================================

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ComprehensiveDemo().setVisible(true));
    }
}
