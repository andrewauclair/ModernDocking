# Hello World

This page will demonstrate how you can create a hello world application with Modern Docking.

At the end of this page you'll know how to:
* create and register dockables
* create and register root panels
* dock dockables programmatically

Our first step will be creating a new class that extends `JFrame` and supporting code to launch this frame:

```java
public class HelloWorld extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Helloworld().setVisible(true);
    }
}
```

That will give us a very basic `JFrame` to start working from.

Now we need to create a constructor that sets a starting size, initializes the docking framework, adds a root, creates and registers dockables and docks them.

First we set a size so that our `JFrame` shows up on screen in a reasonable way `setSize(400, 300);`

Next step is to initialize the docking framework.

```java
Docking.initialize(this);
```

This will initialize the docking framework with our `JFrame` as the Main Frame of the application. This also initializes internal components in the framework that are needed later for floating.

Next, we create and add a `RootDockingPanel`. This can be further customized if you want to use a different layout or have other custom components surrounding the dockable area. For now, we're going to simple use a `BorderLayout` (the default `JFrame` layout manager) and let the root consume all the space.

```java
RootDockingPanel root = new RootDockingPanel(this);
add(root, BorderLayout.CENTER);
```

This creates a new root and adds it to the `JFrame`. `this` is passed to tell the `RootDockingPanel` what `Window` it belongs to. This is used to register the panel with the docking framework and to create any toolbars required for pinning.

Next, we will create a dockable. This will involve creating a new `JPanel` that extends the `Dockable` interface. Most of the methods in the `Dockable` interface have default implementations and are not required to be implemented.

Currently the following methods are required to be implemented:
| Method | Description |
| ------ | ----------- |
| `getPersistentID` | A unique identifier for the dockable within the docking framework |
| `getTabText` | The text to display on the titlebar and on a `JTabbedPane` tab, if docked into a tab group |

We will make a simple panel that takes text as the lone constructor argument and uses it as the persistentID and tab text.

```java
private static class DockingPanel extends JPanel implements Dockable {
    private final String text;

    public DockingPanel(String text) {
		this.text = text;
	}
	
	@Override
	public String getPersistentID() {
		return text;
	}
	
	@Override
	public String getTabText() {
		return text;
	}
}
```

Now that we've created a panel that implements `Dockable` we can start creating dockables in our constructor.

```java
DockingPanel helloWorld = new DockingPanel("Hello World");
```

Now we have a panel and can register and dock it.

```java
Docking.registerDockable(helloWorld);
```

This will register the dockable with the docking framework. The persistentID will be used to uniquely identify the dockable throughout the framework. In more complicated applications we would call `Docking.registerDockable` from within the constructor of our panel, as shown below.

```java
public DockingPanel(String text) {
    this.text = text;
    Docking.registerDockable(this);
}```

We can now dock the dockable. First, we can do this with the panel reference directly.

```java
Docking.dock(helloWorld, this);
```

or, we can use the persistentID value:

```java
Docking.dock("Hello World", this);
```

In both cases `this` again refers to our `JFrame`, requesting that the framework dock our panel to this frame.

Now we have a complete sample that will create a `JFrame` with a dockable with the display text of "Hello World". Full sample is shown below:

```java
public class HelloWorld extends JFrame {
    private static class DockingPanel extends JPanel implements Dockable {
        private final String text;

        public DockingPanel(String text) {
            this.text = text;
        }
        
        @Override
        public String getPersistentID() {
            return text;
        }
        
        @Override
        public String getTabText() {
            return text;
        }
    }

    public HelloWorld() {
        setSize(400, 300);
        
        Docking.initialize(this);
        
        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DockingPanel helloWorld = new DockingPanel("Hello World");
    
        Docking.registerDockable(helloWorld);
        
        Docking.dock(helloWorld, this);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Helloworld().setVisible(true);
    }
}
```