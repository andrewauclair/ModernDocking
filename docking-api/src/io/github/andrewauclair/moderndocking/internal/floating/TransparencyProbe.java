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
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
    /** Per-channel tolerance for the Robot color comparison. */
    private static final int COLOR_TOLERANCE = 30;

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
     * Places a transparent probe window over a magenta reference window and uses
     * {@link Robot} to verify that pixels show through rather than rendering opaque black.
     */
    private static boolean probeOffEDT(GraphicsDevice gd) {
        Rectangle screen = gd.getDefaultConfiguration().getBounds();
        int sampleX = screen.x + 4;
        int sampleY = screen.y + 4;

        // holder[0] = reference frame, holder[1] = probe frame (null if setup failed)
        JFrame[] holder = { null, null };

        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame reference = new JFrame();
                reference.setUndecorated(true);
                reference.setType(Window.Type.UTILITY);
                reference.getContentPane().setBackground(PROBE_COLOR);
                reference.setSize(8, 8);
                reference.setLocation(screen.x, screen.y);
                reference.setVisible(true);
                holder[0] = reference;

                JFrame probe = new JFrame();
                probe.setUndecorated(true);
                probe.setType(Window.Type.UTILITY);
                try {
                    probe.setBackground(new Color(0, 0, 0, 0));
                    probe.getRootPane().setBackground(new Color(0, 0, 0, 0));
                    if (probe.getContentPane() instanceof JComponent) {
                        ((JComponent) probe.getContentPane()).setOpaque(false);
                    }
                }
                catch (IllegalComponentStateException e) {
                    // Transparency setup failed — leave holder[1] null to signal failure
                    return;
                }
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

        if (holder[1] == null) {
            // Probe frame setup threw — transparency not supported
            if (holder[0] != null) {
                SwingUtilities.invokeLater(holder[0]::dispose);
            }
            return false;
        }

        // Give the compositor time to finalize rendering before sampling
        try {
            Thread.sleep(150);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            Robot robot = new Robot(gd);
            robot.waitForIdle();
            Color sampled = robot.getPixelColor(sampleX, sampleY);
            return isCloseToProbeColor(sampled);
        }
        catch (AWTException e) {
            return false;
        }
        finally {
            JFrame ref = holder[0];
            JFrame prob = holder[1];
            SwingUtilities.invokeLater(() -> {
                if (ref != null) ref.dispose();
                if (prob != null) prob.dispose();
            });
        }
    }

    private static boolean isCloseToProbeColor(Color c) {
        return Math.abs(c.getRed()   - PROBE_COLOR.getRed())   < COLOR_TOLERANCE
            && Math.abs(c.getGreen() - PROBE_COLOR.getGreen()) < COLOR_TOLERANCE
            && Math.abs(c.getBlue()  - PROBE_COLOR.getBlue())  < COLOR_TOLERANCE;
    }
}