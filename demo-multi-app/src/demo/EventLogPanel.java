package demo;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.api.DockingAPI;
import io.github.andrewauclair.moderndocking.event.DockingEvent;
import io.github.andrewauclair.moderndocking.event.DockingListener;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EventLogPanel extends JPanel implements Dockable, DockingListener {

    private final String id;
    private final DockingAPI docking;
    private final JTextArea log = new JTextArea();
    final Map<DockingEvent.ID, AtomicInteger> counters    = new LinkedHashMap<>();
    final Map<DockingEvent.ID, JLabel>        countLabels = new LinkedHashMap<>();

    public EventLogPanel(DockingAPI docking, String id) {
        this.docking = docking;
        this.id = id;
        setLayout(new BorderLayout());

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JPanel counterPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 3));
        for (DockingEvent.ID eventId : DockingEvent.ID.values()) {
            counters.put(eventId, new AtomicInteger(0));
            JLabel lbl = new JLabel(eventId.name() + ": 0  ");
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
            countLabels.put(eventId, lbl);
            counterPanel.add(lbl);
        }

        JScrollPane counterScroll = new JScrollPane(counterPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        counterScroll.setPreferredSize(new Dimension(0, 60));

        JButton resetCounters = new JButton("Reset Counters");
        resetCounters.addActionListener(e -> {
            counters.forEach((eventId, c) -> c.set(0));
            countLabels.forEach((eventId, lbl) -> lbl.setText(eventId.name() + ": 0  "));
        });

        JButton clearLog = new JButton("Clear Log");
        clearLog.addActionListener(e -> log.setText(""));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        buttonRow.add(resetCounters);
        buttonRow.add(clearLog);

        add(counterScroll, BorderLayout.NORTH);
        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        add(logScroll, BorderLayout.CENTER);
        add(buttonRow, BorderLayout.SOUTH);

        docking.registerDockable(this);
    }

    @Override
    public void dockingChange(DockingEvent e) {
        SwingUtilities.invokeLater(() -> {
            String ts    = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            String entry = String.format("[%s] %-30s %s%s%n",
                    ts,
                    e.getID().name(),
                    e.getDockable().getPersistentID(),
                    e.isTemporary() ? "  (temporary)" : "");
            log.append(entry);
            log.setCaretPosition(log.getDocument().getLength());

            int n = counters.get(e.getID()).incrementAndGet();
            countLabels.get(e.getID()).setText(e.getID().name() + ":" + n + "  ");
        });
    }

    @Override public String getPersistentID() { return id; }
    @Override public String getTabText()      { return "Event Log"; }
}
