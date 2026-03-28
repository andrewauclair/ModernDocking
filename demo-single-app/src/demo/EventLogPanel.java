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
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Event log panel. Implements DockingListener — registered in the ComprehensiveDemo
 * constructor after all dockables are ready.
 * <p>
 * Displays every DockingEvent with a timestamp, the dockable's persistent ID, and
 * whether the event was temporary. Also shows per-type counters that can be reset
 * independently of clearing the log text.
 */
public class EventLogPanel extends JPanel implements Dockable, DockingListener {

    private final JTextArea log = new JTextArea();
    final Map<DockingEvent.ID, AtomicInteger> counters = new LinkedHashMap<>();
    final Map<DockingEvent.ID, JLabel> countLabels = new LinkedHashMap<>();

    public EventLogPanel() {
        setLayout(new GridBagLayout());

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        // Counter row per event type
        JPanel counterPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;

        for (DockingEvent.ID id : DockingEvent.ID.values()) {
            counters.put(id, new AtomicInteger(0));
            JLabel lbl = new JLabel(id.name() + ": 0  ");
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
            countLabels.put(id, lbl);
            counterPanel.add(lbl, gbc);
            gbc.gridy++;
        }

//        JScrollPane counterScroll = new JScrollPane(counterPanel);

        JButton resetCounters = new JButton("Reset Counters");

        resetCounters.addActionListener(e -> {
            counters.forEach((id, c) -> c.set(0));
            countLabels.forEach((id, lbl) -> lbl.setText(id.name() + ": 0  "));
        });

        JButton clearLog = new JButton("Clear Log");
        clearLog.addActionListener(e -> log.setText(""));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        buttonRow.add(resetCounters);
        buttonRow.add(clearLog);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;

        add(counterPanel, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridy++;

        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        add(logScroll, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;

        add(buttonRow, gbc);
        gbc.gridy++;

        Docking.registerDockable(this);
    }

    @Override
    public void dockingChange(DockingEvent e) {
        SwingUtilities.invokeLater(() -> {
            String ts = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            String entry = String.format("[%s] %-30s %s%s%n",
                    ts,
                    e.getID().name(),
                    e.getDockable().getPersistentID(),
                    e.isTemporary() ? "  (temporary)" : "");
            log.append(entry);
            log.setCaretPosition(log.getDocument().getLength());

            int n = counters.get(e.getID()).incrementAndGet();
            countLabels.get(e.getID()).setText(e.getID().name() + ": " + n + "  ");
        });
    }

    @Override
    public String getPersistentID() {
        return "event-log";
    }

    @Override
    public String getTabText() {
        return "Event Log";
    }

    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}
