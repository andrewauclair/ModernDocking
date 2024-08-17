// https://stackoverflow.com/questions/14635377/java-hardware-acceleration-not-working-with-intel-integrated-graphics
package examples;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class GraphicsTest extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GraphicsTest();
            }
        });
    }

    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    BufferCapabilities bufferCapabilities;
    BufferStrategy bufferStrategy;

    int y = 0;
    int delta = 1;

    public GraphicsTest() {

        setTitle("Hardware Acceleration Test");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);

        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        bufferCapabilities = gc.getBufferCapabilities();

        new AnimationThread().start();
    }

    class AnimationThread extends Thread {
        @Override
        public void run() {

            while(true) {
                Graphics2D g2 = null;
                try {
                    g2 = (Graphics2D) bufferStrategy.getDrawGraphics();
                    draw(g2);
                } finally {
                    if(g2 != null) g2.dispose();
                }
                bufferStrategy.show();

                try {
                    Thread.sleep(16);
                } catch(Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        if(!bufferCapabilities.isPageFlipping() || bufferCapabilities.isFullScreenRequired()) {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.red);
            g2.drawString("Hardware Acceleration is not supported...", 100, 100);
            g2.setColor(Color.white);
            g2.drawString("Page Flipping: " + (bufferCapabilities.isPageFlipping() ? "Available" : "Not Supported"), 100, 130);
            g2.drawString("Full Screen Required: " + (bufferCapabilities.isFullScreenRequired() ? "Required" : "Not Required"), 100, 160);
            g2.drawString("Multiple Buffer Capable: " + (bufferCapabilities.isMultiBufferAvailable() ? "Yes" : "No"), 100, 190);
        } else {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.white);
            g2.drawString("Hardware Acceleration is Working...", 100, 100);
            g2.drawString("Page Flipping: " + (bufferCapabilities.isPageFlipping() ? "Available" : "Not Supported"), 100, 130);
            g2.drawString("Full Screen Required: " + (bufferCapabilities.isFullScreenRequired() ? "Required" : "Not Required"), 100, 160);
            g2.drawString("Multiple Buffer Capable: " + (bufferCapabilities.isMultiBufferAvailable() ? "Yes" : "No"), 100, 190);
        }

        y += delta;
        if((y + 50) > getHeight() || y < 0) {
            delta *= -1;
        }

        g2.setColor(Color.blue);
        g2.fillRect(getWidth()-50, y, 50, 50);
    }
}