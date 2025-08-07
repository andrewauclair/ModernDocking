.. role:: java(code)
    :language: java

###################
Dockable Properties
###################

.. _Picocli: https://picocli.info/

The :java:`@DockingProperty` attribute is used to dynamically add properties to dockable components and was inspired by the excellent Java command line parsing library `Picocli`_.

For example, say we have a simple :java:`JCheckBox` on our Dockable component and wish to persist it together with the Docking layout. We could create a :java:`boolean` member variable in our Dockable component class and tie it to a property.

.. :: TODO make this example actually have a JCheckBox

.. code-block:: java

    public class Example implements Dockable {
        @DockingProperty(name = "enabled", defaultValue = "false")
        private boolean enabled;

        @Override
        public void updateProperties() {
            // enabled value is now usable
        }
    }

These dockable properties can be done for the following Java types:

- byte
- short
- int
- long
- float
- double
- char
- boolean
- String