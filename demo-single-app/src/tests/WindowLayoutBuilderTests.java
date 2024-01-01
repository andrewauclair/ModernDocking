/*
Copyright (c) 2023 Andrew Auclair

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
package tests;

import ModernDocking.*;
import ModernDocking.app.ApplicationLayoutMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.app.WindowLayoutBuilder;
import ModernDocking.event.DockingLayoutEvent;
import ModernDocking.event.DockingLayoutListener;
import ModernDocking.layouts.DockingLayouts;
import basic.SimplePanel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import exception.FailOnThreadViolationRepaintManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WindowLayoutBuilderTests extends JFrame implements DockingLayoutListener {

    private final JMenu layout;

    WindowLayoutBuilderTests() {
        setTitle("DockingLayoutBuilder Tests");

        setSize(500, 500);

        Docking.initialize(this);

        List<SimplePanel> panels = new ArrayList<>();

        for (int i = 1; i <= 16; i++) {
            panels.add(new SimplePanel(String.valueOf(i), String.valueOf(i)));
        }

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        layout = new JMenu("Layout");

        menuBar.add(layout);

        DockingLayouts.addLayoutsListener(this);

        buildLayouts();

        add(new RootDockingPanel(this));
    }

    private void buildLayouts() {
        DockingLayouts.addLayout("simple (1)", new WindowLayoutBuilder("1")
                .buildApplicationLayout());

        DockingLayouts.addLayout("tabs (1, 2)", new WindowLayoutBuilder("1")
                .dock("2", "1")
                .display("1")
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (2), east (1) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.WEST)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north (1), south (2) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.SOUTH)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (1), east (2) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.EAST)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north (2), south (1) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.NORTH)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (2) east (1) south (3) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.WEST)
                .dockToRoot("3", DockingRegion.SOUTH)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west( simple (2) ), east( split( north (1), south (3) ) ) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.WEST)
                .dock("3", "1", DockingRegion.SOUTH)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west( tabs (1, 2) ), east( split( north (3), south ( split( west (4), east (5) ) ) ) ) )", new WindowLayoutBuilder("1")
                .dock("2", "1")
                .dock("3", "2", DockingRegion.EAST)
                .dock("4", "3", DockingRegion.SOUTH)
                .dock("5", "4", DockingRegion.EAST)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (1, .25), east (2) )", new WindowLayoutBuilder("2")
                .dock("1", "2", DockingRegion.WEST, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (1), east (2, .25) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.EAST, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north (1, .25), south (2) )", new WindowLayoutBuilder("2")
                .dock("1", "2", DockingRegion.NORTH, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north (1), south (2, .25) )", new WindowLayoutBuilder("1")
                .dock("2", "1", DockingRegion.SOUTH, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west (1, .25), east ( tab( 2, 3 ) ) )", new WindowLayoutBuilder("2")
                .dock("3", "2")
                .dock("1", "2", DockingRegion.WEST, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( west ( tabs( 1, 3 ) ), east (2, .25) )", new WindowLayoutBuilder("1")
                .dock("3", "1")
                .dock("2", "1", DockingRegion.EAST, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north (1, .25), south ( tabs( 2, 3 ) ) )", new WindowLayoutBuilder("2")
                .dock("3", "2")
                .dock("1", "2", DockingRegion.NORTH, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("split( north ( tabs( 1, 3 ) ), south (2, .25) )", new WindowLayoutBuilder("1")
                .dock("3", "1")
                .dock("2", "1", DockingRegion.SOUTH, .25)
                .buildApplicationLayout());

        DockingLayouts.addLayout("dock to root east/west", new WindowLayoutBuilder("1")
                .dockToRoot("2", DockingRegion.EAST)
                .dockToRoot("3", DockingRegion.WEST)
                .buildApplicationLayout());

        DockingLayouts.addLayout("dock to root north/south", new WindowLayoutBuilder("1")
                .dockToRoot("2", DockingRegion.NORTH)
                .dockToRoot("3", DockingRegion.SOUTH)
                .buildApplicationLayout());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel(args);

            WindowLayoutBuilderTests mainFrame = new WindowLayoutBuilderTests();
            mainFrame.setVisible(true);
        });
    }

    private static void configureLookAndFeel(String[] args) {
        try {
            FlatLaf.registerCustomDefaultsSource("docking");

            if (args.length > 1) {
                System.setProperty("flatlaf.uiScale", args[1]);
            }

            if (args.length > 0 && args[0].equals("light")) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else if (args.length > 0 && args[0].equals("dark")) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                    throw new RuntimeException(ex);
                }
            }
            FlatLaf.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException ex) {
                throw new RuntimeException(ex);
            }
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

        // this is an app to test the docking framework, we want to make sure we detect EDT violations as soon as possible
        FailOnThreadViolationRepaintManager.install();
    }

    @Override
    public void layoutChange(DockingLayoutEvent e) {
        switch (e.getID()) {
            case ADDED:
                layout.add(new ApplicationLayoutMenuItem(e.getLayoutName()));
                break;
            case REMOVED:
                for (int i = 0; i < layout.getItemCount(); i++) {
                    if (layout.getItem(i).getName().equals(e.getLayoutName())) {
                        layout.remove(i);
                        break;
                    }
                }
                break;
        }
    }
}
