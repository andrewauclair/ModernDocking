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
package io.github.andrewauclair.moderndocking.internal.floating;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Probes at startup whether the platform supports per-pixel window translucency.
 * The result is cached after the first call and reused for all subsequent calls.
 * <p>
 * When transparency is not supported, {@link Floating} uses a JLayeredPane-based
 * overlay instead of a transparent JFrame.
 */
class TransparencyProbe {
    /** Reference background color used by the Robot-based check. Chosen to be visually
     *  distinctive (magenta) and unlikely to match typical desktop content. */
    private static final Color PROBE_COLOR = new Color(255, 0, 255);
    /** Sentinel color painted on the probe frame before switching to transparent.
     *  Allows Robot polling to confirm the probe frame is actually on screen. */
    private static final Color SENTINEL_COLOR = new Color(0, 255, 255);
    /** Per-channel tolerance for the Robot color comparison. */
    private static final int COLOR_TOLERANCE = 30;
    /** How long to poll for the sentinel/transparent frame to appear (ms). */
    private static final int POLL_TIMEOUT_MS = 2000;
    /** Polling interval while waiting for a frame to appear (ms). */
    private static final int POLL_INTERVAL_MS = 20;

    private static volatile Boolean cachedResult = null;

    private TransparencyProbe() {
    }

    /**
     * Returns whether per-pixel window translucency is supported on this platform.
     * Runs the probe on the first call; subsequent calls return the cached result.
     * <p>
     * Should be triggered early (e.g., in {@link Floating#registerDockingWindow}) so the
     * result is available before any drag operation begins.
     *
     * @return true if transparent overlay windows will work correctly
     */
    static boolean isTransparencySupported() {
        if (cachedResult == null) {
            cachedResult = probe();
        }
        return cachedResult;
    }

    private static boolean probe() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Fast path: platform declares per-pixel translucency unsupported
        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
            return false;
        }

        return probeOffEDT(gd);
    }

    /**
     * Full empirical check for when the probe runs off the EDT.
     * <p>
     * Phase 1: shows the probe frame as an opaque sentinel color (cyan) and polls
     * Robot until that color appears at the sample point, confirming the probe is
     * composited on screen and on top of the magenta reference frame.
     * <p>
     * Phase 2: switches the probe frame to fully transparent and samples again.
     * If the underlying magenta reference frame shows through, per-pixel translucency
     * works. If the pixel stays black (or the sentinel color), it does not.
     */
    private static boolean probeOffEDT(GraphicsDevice gd) {
        Rectangle screen = gd.getDefaultConfiguration().getBounds();
        int sampleX = screen.x + 4;
        int sampleY = screen.y + 4;

        // holder[0] = reference window, holder[1] = probe window (null if setup failed)
        JWindow[] holder = { null, null };

        // --- Phase 1: create reference (magenta) + probe (opaque sentinel cyan) ---
        // JWindow is used instead of JFrame so neither window appears in the taskbar.
        try {
            SwingUtilities.invokeAndWait(() -> {
                JWindow reference = new JWindow();
                reference.getContentPane().setBackground(PROBE_COLOR);
                reference.setSize(8, 8);
                reference.setLocation(screen.x, screen.y);
                reference.setVisible(true);
                holder[0] = reference;

                JWindow probe = new JWindow();
                // Start opaque with sentinel color so we can confirm it is on screen
                // before switching to transparent.
                probe.getContentPane().setBackground(SENTINEL_COLOR);
                probe.setSize(8, 8);
                probe.setLocation(screen.x, screen.y);
                probe.setVisible(true);
                holder[1] = probe;
            });
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        catch (InvocationTargetException e) {
            return false;
        }

        Robot robot;
        try {
            robot = new Robot(gd);
            // Drain the event queue once after frame creation; subsequent polls use
            // only Thread.sleep to avoid hammering the EDT every 20 ms.
            robot.waitForIdle();
        }
        catch (AWTException e) {
            SwingUtilities.invokeLater(() -> {
                if (holder[0] != null) holder[0].dispose();
                if (holder[1] != null) holder[1].dispose();
            });
            return false;
        }

        // Poll until the sentinel color is visible, confirming the probe frame is rendered
        // and on top before we attempt the transparency switch.
        boolean sentinelSeen = pollForColor(robot, sampleX, sampleY, SENTINEL_COLOR);

        if (!sentinelSeen) {
            // Can't confirm the probe frame appeared — conservative fallback
            SwingUtilities.invokeLater(() -> {
                if (holder[0] != null) holder[0].dispose();
                if (holder[1] != null) holder[1].dispose();
            });
            return false;
        }

        // --- Phase 2: switch probe to transparent, then check for magenta reference ---
        try {
            SwingUtilities.invokeAndWait(() -> {
                JWindow probe = holder[1];
                try {
                    probe.setBackground(new Color(0, 0, 0, 0));
                    probe.getRootPane().setBackground(new Color(0, 0, 0, 0));
                    if (probe.getContentPane() instanceof JComponent) {
                        ((JComponent) probe.getContentPane()).setOpaque(false);
                    }
                    probe.repaint();
                }
                catch (IllegalComponentStateException e) {
                    // Transparency setup failed — signal via null
                    holder[1] = null;
                }
            });
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SwingUtilities.invokeLater(() -> {
                if (holder[0] != null) holder[0].dispose();
            });
            return false;
        }
        catch (InvocationTargetException e) {
            SwingUtilities.invokeLater(() -> {
                if (holder[0] != null) holder[0].dispose();
            });
            return false;
        }

        if (holder[1] == null) {
            SwingUtilities.invokeLater(() -> {
                if (holder[0] != null) holder[0].dispose();
            });
            return false;
        }

        // Drain the EDT once after the transparency switch before polling.
        robot.waitForIdle();

        // Poll for the magenta reference to become visible through the now-transparent probe
        boolean result;
        try {
            result = pollForColor(robot, sampleX, sampleY, PROBE_COLOR);
        }
        finally {
            JWindow ref = holder[0];
            JWindow prob = holder[1];
            SwingUtilities.invokeLater(() -> {
                if (ref != null) ref.dispose();
                if (prob != null) prob.dispose();
            });
        }
        return result;
    }

    /**
     * Polls {@link Robot#getPixelColor} at (x, y) until the sampled color is within
     * {@link #COLOR_TOLERANCE} of {@code target}, or {@link #POLL_TIMEOUT_MS} elapses.
     *
     * @return true if the target color was observed within the timeout
     */
    private static boolean pollForColor(Robot robot, int x, int y, Color target) {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (isCloseTo(robot.getPixelColor(x, y), target)) {
                return true;
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private static boolean isCloseTo(Color c, Color target) {
        return Math.abs(c.getRed()   - target.getRed())   < COLOR_TOLERANCE
            && Math.abs(c.getGreen() - target.getGreen()) < COLOR_TOLERANCE
            && Math.abs(c.getBlue()  - target.getBlue())  < COLOR_TOLERANCE;
    }
}