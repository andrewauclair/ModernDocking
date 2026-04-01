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
import com.formdev.flatlaf.extras.FlatSVGUtils;
import demo.MiscPanels.FixedPanel;
import demo.MiscPanels.MoreOptionsPanel;
import demo.MiscPanels.NoTabGroupPanel;
import demo.MiscPanels.ScrollablePanel;
import demo.MiscPanels.VetoClosePanel;
import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.api.RootDockingPanelAPI;
import io.github.andrewauclair.moderndocking.api.WindowLayoutBuilderAPI;
import io.github.andrewauclair.moderndocking.event.NewFloatingFrameListener;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingLayouts;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import io.github.andrewauclair.moderndocking.ui.ToolbarLocation;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Base frame for both single-app and multi-app comprehensive demos.
 * Subclasses supply the three framework-specific factory methods.
 */
public abstract class CommonDemoFrame extends JFrame {

    private final DockingAPI docking;

    private final DocumentsAnchor documentsAnchor;
    private final ProjectPanel projectTree;
    private final PropertiesPanel propertiesPanel;
    private final OutputPanel outputPanel;
    private final EventLogPanel eventLogPanel;
    private final ScrollablePanel scrollablePanel;
    private final FixedPanel fixedPanel;
    private final VetoClosePanel vetoPanel;
    private final MoreOptionsPanel moreOptionsPanel;
    private final NoTabGroupPanel noTabGroupPanel;
    private final TestHarnessPanel testHarnessPanel;

    // =========================================================================
    // Abstract factory methods
    // =========================================================================

    /** Initialize and return the DockingAPI instance for this frame. */
    protected abstract DockingAPI createDocking();

    /** Create the concrete RootDockingPanel for this frame. */
    protected abstract RootDockingPanelAPI createRoot(DockingAPI docking);

    /** Create the concrete WindowLayoutBuilder for this frame. */
    protected abstract WindowLayoutBuilderAPI createLayoutBuilder(DockingAPI docking, String firstId);

    // =========================================================================
    // Constructor
    // =========================================================================

    protected CommonDemoFrame(String title, File persistFile) {
        super(title);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception on thread: " + t.getName());
            e.printStackTrace();
        });
        SwingUtilities.invokeLater(() ->
                Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
                    System.err.println("Uncaught exception on EDT");
                    e.printStackTrace();
                })
        );

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImages(FlatSVGUtils.createWindowIconImages("/modern-docking-demo.svg"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                docking.getAppState().persist();
                dispose();
                boolean anyVisible = false;
                for (Frame f : Frame.getFrames()) {
                    if (f.isVisible() && f != CommonDemoFrame.this) {
                        anyVisible = true;
                        break;
                    }
                }
                if (!anyVisible) {
                    System.exit(0);
                }
            }
        });

        docking = createDocking();
        DockingUI.initialize();

        // Create all panels now that the framework is ready
        documentsAnchor = new DocumentsAnchor(docking);
        projectTree = new ProjectPanel(docking, "project-tree", new File("docking-api"), DocumentsAnchor.PERSISTENT_ID);
        propertiesPanel = new PropertiesPanel(docking, "properties-panel");
        outputPanel = new OutputPanel(docking, "output-panel");
        eventLogPanel = new EventLogPanel(docking, "event-log");
        scrollablePanel = new ScrollablePanel(docking, "scrollable-panel");
        fixedPanel = new FixedPanel(docking, "fixed-panel");
        vetoPanel = new VetoClosePanel(docking, "veto-close-panel");
        moreOptionsPanel = new MoreOptionsPanel(docking, "more-options-panel");
        noTabGroupPanel = new NoTabGroupPanel(docking, "no-tab-group-panel");

        List<Dockable> allDockables = new ArrayList<>();
        allDockables.add(projectTree);
        allDockables.add(propertiesPanel);
        allDockables.add(outputPanel);
        allDockables.add(eventLogPanel);
        allDockables.add(scrollablePanel);
        allDockables.add(fixedPanel);
        allDockables.add(vetoPanel);
        allDockables.add(moreOptionsPanel);
        allDockables.add(noTabGroupPanel);

        testHarnessPanel = new TestHarnessPanel(docking, "test-harness", allDockables);

        docking.addDockingListener(eventLogPanel);
        docking.addNewFloatingFrameListener(new NewFloatingFrameListener() {
            @Override
            public void newFrameCreated(JFrame frame, RootDockingPanelAPI root) {
            }

            @Override
            public void newFrameCreated(JFrame frame, RootDockingPanelAPI root, Dockable dockable) {
                frame.setTitle("Floating \u2014 " + dockable.getTitleText());
            }
        });

        // Restore dynamic editor panels (created when files are opened from the project tree)
        docking.setUserDynamicDockableCreationListener((persistentID, className, titleText, tabText, properties) -> {
            if (className.equals(EditorPanel.class.getName())) {
                Property filePathProp = properties.get("file-path");
                String filePath = (filePathProp instanceof Property.StringProperty)
                        ? ((Property.StringProperty) filePathProp).getValue()
                        : "";
                return new EditorPanel(docking, persistentID, tabText, filePath);
            }
            return null;
        });

        // Root panel
        setLayout(new BorderLayout());
        RootDockingPanelAPI root = createRoot(docking);
        JPanel rootWrapper = new JPanel(new BorderLayout());
        rootWrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        rootWrapper.add(root, BorderLayout.CENTER);
        add(rootWrapper, BorderLayout.CENTER);

        setJMenuBar(buildMenuBar());

        // Default layout
        ApplicationLayout defaultLayout = buildDefaultLayout();
        DockingLayouts.addLayout("default", defaultLayout);

        docking.getAppState().setAutoPersist(true);
        docking.getAppState().setPersistFile(persistFile);
        docking.getAppState().setDefaultApplicationLayout(defaultLayout);

        boolean restored = false;
        try {
            restored = docking.getAppState().restore();
        }
        catch (DockingLayoutException ex) {
            // fallback to default
        }

        if (!restored) {
            setLocationRelativeTo(null);
        }
    }

    // =========================================================================
    // Default layout
    // =========================================================================

    private ApplicationLayout buildDefaultLayout() {
        WindowLayoutBuilderAPI builder = createLayoutBuilder(docking, documentsAnchor.getPersistentID())
                .dockToRoot(projectTree.getPersistentID(), DockingRegion.WEST, 0.20)
                .dockToRoot(outputPanel.getPersistentID(), DockingRegion.SOUTH, 0.25)
                .dockToRoot(propertiesPanel.getPersistentID(), DockingRegion.EAST, 0.22)
                .dock(eventLogPanel.getPersistentID(), propertiesPanel.getPersistentID(), DockingRegion.CENTER);

        return builder.buildApplicationLayout();
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
                    docking.getLayoutPersistence().saveLayoutToFile(
                            fc.getSelectedFile(),
                            docking.getDockingState().getApplicationLayout());
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
                    ApplicationLayout layout = docking.getLayoutPersistence()
                            .loadApplicationLayoutFromFile(fc.getSelectedFile());
                    docking.getDockingState().restoreApplicationLayout(layout);
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
                DockingLayouts.addLayout(name, docking.getDockingState().getApplicationLayout());
            }
        });
        menu.add(storeNamed);

        return menu;
    }

    private JMenu buildViewMenu() {
        JMenu menu = new JMenu("View");

        addViewItem(menu, projectTree);
        addViewItem(menu, propertiesPanel);
        addViewItem(menu, outputPanel);
        addViewItem(menu, eventLogPanel);
        menu.addSeparator();
        addViewItem(menu, scrollablePanel);
        addViewItem(menu, fixedPanel);
        addViewItem(menu, vetoPanel);
        addViewItem(menu, moreOptionsPanel);
        addViewItem(menu, noTabGroupPanel);
        menu.addSeparator();
        addViewItem(menu, testHarnessPanel);

        return menu;
    }

    private void addViewItem(JMenu menu, Dockable d) {
        JMenuItem item = new JMenuItem(d.getTabText());
        item.addActionListener(e -> docking.display(d));
        menu.add(item);
    }

    private JMenu buildActionsMenu() {
        JMenu menu = new JMenu("Actions");

        // --- Focused mode ---
        JMenuItem exitFocused = new JMenuItem("Exit Focused Mode (find active)");
        exitFocused.addActionListener(e -> exitFocusedModeForAny());
        menu.add(exitFocused);
        menu.addSeparator();

        // --- Float in new window ---
        JMenu floatMenu = new JMenu("Float in New Window");
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
        addAutoHideItem(autoHideMenu, outputPanel, ToolbarLocation.SOUTH);
        addAutoHideItem(autoHideMenu, propertiesPanel, ToolbarLocation.EAST);
        menu.add(autoHideMenu);

        return menu;
    }

    private void exitFocusedModeForAny() {
        for (Dockable d : docking.getDockables()) {
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
            }
            else {
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
            }
            else {
                JOptionPane.showMessageDialog(this, d.getTabText() + " is not currently docked.");
            }
        });
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

        String defaultLayoutName = "default";
        JMenuItem restoreDefault = new JMenuItem("Restore Default Layout");
        restoreDefault.addActionListener(e -> {
            ApplicationLayout layout = DockingLayouts.getLayout(defaultLayoutName);
            if (layout != null) {
                docking.getDockingState().restoreApplicationLayout(layout);
            }
        });
        menu.add(restoreDefault);

        menu.addSeparator();

        JMenu layoutsMenu = new JMenu("Layouts");
        layoutsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                layoutsMenu.removeAll();
                for (String name : DockingLayouts.getLayoutNames()) {
                    JMenuItem item = new JMenuItem(name);
                    item.addActionListener(ev -> {
                        ApplicationLayout layout = DockingLayouts.getLayout(name);
                        if (layout != null) {
                            docking.getDockingState().restoreApplicationLayout(layout);
                        }
                    });
                    layoutsMenu.add(item);
                }
            }

            @Override public void menuDeselected(MenuEvent e) {}
            @Override public void menuCanceled(MenuEvent e) {}
        });
        menu.add(layoutsMenu);

        return menu;
    }
}
