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

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

public class MiscPanels {

    public static class ScrollablePanel extends JPanel implements Dockable {
        private final String id;
        private final DockingAPI docking;

        public ScrollablePanel(DockingAPI docking, String id) {
            this.docking = docking;
            this.id = id;
            setLayout(new BorderLayout());
            JPanel content = new JPanel(new GridLayout(0, 1, 0, 2));
            for (int i = 1; i <= 40; i++) {
                content.add(new JLabel("  Item " + i + " — demonstrating wrappable scroll pane"));
            }
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setViewportBorder(null);
            add(scroll, BorderLayout.CENTER);
            docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public String getTabText() {
            return "Scrollable";
        }

        @Override
        public boolean isWrappableInScrollpane() {
            return true;
        }
    }

    public static class FixedPanel extends JPanel implements Dockable {
        private final String id;
        private final DockingAPI docking;

        public FixedPanel(DockingAPI docking, String id) {
            this.docking = docking;
            this.id = id;
            setLayout(new BorderLayout());
            add(centered("<html><center><b>Non-closable Panel</b><br><br>" +
                            "isClosable() = false<br>No × button is rendered.</center></html>"),
                    BorderLayout.CENTER);
            docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public String getTabText() {
            return "Non-Closable";
        }

        @Override
        public boolean isClosable() {
            return false;
        }
    }

    public static class VetoClosePanel extends JPanel implements Dockable {
        private final String id;
        private final DockingAPI docking;

        public VetoClosePanel(DockingAPI docking, String id) {
            this.docking = docking;
            this.id = id;
            setLayout(new BorderLayout());
            add(centered("<html><center><b>Veto Close Panel</b><br><br>" +
                            "requestClose() shows a confirm dialog.<br>" +
                            "Click No to veto the close.</center></html>"),
                    BorderLayout.CENTER);
            docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public String getTabText() {
            return "Veto Close";
        }

        @Override
        public boolean requestClose() {
            int r = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to close this panel?",
                    "Confirm Close", JOptionPane.YES_NO_OPTION);
            return r == JOptionPane.YES_OPTION;
        }
    }

    public static class MoreOptionsPanel extends JPanel implements Dockable {
        private final String id;
        private final DockingAPI docking;

        public MoreOptionsPanel(DockingAPI docking, String id) {
            this.docking = docking;
            this.id = id;
            setLayout(new BorderLayout());
            add(centered("<html><center><b>More Options Panel</b><br><br>" +
                            "Open the header ⚙ menu to see<br>custom entries added via addMoreOptions().</center></html>"),
                    BorderLayout.CENTER);
            docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public String getTabText() {
            return "More Options";
        }

        @Override
        public void addMoreOptions(JPopupMenu menu) {
            JMenuItem a = new JMenuItem("Custom Action A");
            a.addActionListener(e -> JOptionPane.showMessageDialog(this, "Custom Action A fired."));
            menu.add(a);

            JMenuItem b = new JMenuItem("Custom Action B");
            b.addActionListener(e -> JOptionPane.showMessageDialog(this, "Custom Action B fired."));
            menu.add(b);
        }
    }

    public static class NoTabGroupPanel extends JPanel implements Dockable {
        private final String id;
        private final DockingAPI docking;

        public NoTabGroupPanel(DockingAPI docking, String id) {
            this.docking = docking;
            this.id = id;
            setLayout(new BorderLayout());
            add(centered("<html><center><b>No Tab Group Panel</b><br><br>" +
                            "isTabGroupAllowed() = false (1.5.0)<br><br>" +
                            "Drag another panel over this one — the<br>" +
                            "CENTER handle should not appear.</center></html>"),
                    BorderLayout.CENTER);
            docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return id;
        }

        @Override
        public String getTabText() {
            return "No Tab Group";
        }
        // TODO 1.5.0 — isTabGroupAllowed() not yet in Dockable interface
        // @Override public boolean isTabGroupAllowed() { return false; }
    }

    private static JLabel centered(String html) {
        JLabel lbl = new JLabel(html, JLabel.CENTER);
        lbl.setVerticalAlignment(JLabel.CENTER);
        return lbl;
    }
}
