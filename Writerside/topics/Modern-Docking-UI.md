# Modern Docking UI

Modern Docking UI is an extension to Modern Docking that changes png's to svg's for the Settings and Close icons.

## Initializing DockingUI

Using our [Hello World](Hello-World.md) example, we can update it to initialize DockingUI like below, which is all that's required to use Modern Docking UI.

```java
public static class MainFrame extends JFrame {
    public MainFrame() {
        setSize(400, 300);

        Docking.initialize(this);
        DockingUI.initialize();
        
        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DockingPanel helloWorld = new DockingPanel("Hello World");

        Docking.dock(helloWorld, this);
    }
}
```

## FlatLaf


