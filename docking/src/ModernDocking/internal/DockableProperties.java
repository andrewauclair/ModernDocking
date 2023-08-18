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
package ModernDocking.internal;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.DockingProperty;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DockableProperties {
    public static void configureProperties(Dockable dockable, Map<String, String> properties) {
        if (!Docking.isExperimentalPropertyFeatureEnabled()) {
            dockable.setProperties(properties);
            return;
        }

        List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(DockingProperty.class) != null)
                .collect(Collectors.toList());

        for (Field field : dockingPropFields) {
            try {
                // make sure we can access the field if it is private/protected
                field.setAccessible(true);

                // grab the property and store the value by its name
                DockingProperty property = field.getAnnotation(DockingProperty.class);

                if (properties.containsKey(property.name())) {
                    setProperty(dockable, field, properties.get(property.name()));
                }
                else if (!Objects.equals(property.defaultValue(), "__no_default_value__")) {
                    setProperty(dockable, field, property.defaultValue());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();

            }
        }

        dockable.updateProperties();
    }

    public static Map<String, String> saveProperties(Dockable dockable) {
        if (!Docking.isExperimentalPropertyFeatureEnabled()) {
            return new HashMap<>(dockable.getProperties());
        }

        Map<String, String> properties = new HashMap<>();

        List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(DockingProperty.class) != null)
                .collect(Collectors.toList());

        for (Field field : dockingPropFields) {
            try {
                // make sure we can access the field if it is private/protected
                field.setAccessible(true);

                // grab the property and store the value by its name
                DockingProperty property = field.getAnnotation(DockingProperty.class);

                properties.put(property.name(), getProperty(dockable, field));
            }
            catch (IllegalAccessException ignore) {
            }
        }

        return properties;
    }

    private static String getProperty(Dockable dockable, Field field) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == byte.class) {
            return Byte.toString((Byte) field.get(dockable));
        }
        else if (type == short.class) {
            return Short.toString((Short) field.get(dockable));
        }
        else if (type == int.class) {
            return Integer.toString((Integer) field.get(dockable));
        }
        else if (type == long.class) {
            return Long.toString((Long) field.get(dockable));
        }
        else if (type == float.class) {
            return Float.toString((Float) field.get(dockable));
        }
        else if (type == double.class) {
            return Double.toString((Double) field.get(dockable));
        }
        else if (type == char.class) {
            return Character.toString((Character) field.get(dockable));
        }
        else if (type == String.class) {
            return (String) field.get(dockable);
        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }

    private static void setProperty(Dockable dockable, Field field, String value) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == byte.class) {
            field.set(dockable, Byte.parseByte(value));
        }
        else if (type == short.class) {
            field.set(dockable, Short.parseShort(value));
        }
        else if (type == int.class) {
            field.set(dockable, Integer.parseInt(value));
        }
        else if (type == long.class) {
            field.set(dockable, Long.parseLong(value));
        }
        else if (type == float.class) {
            field.set(dockable, Float.parseFloat(value));
        }
        else if (type == double.class) {
            field.set(dockable, Double.parseDouble(value));
        }
        else if (type == char.class) {
            field.set(dockable, value.charAt(0));
        }
        else if (type == String.class) {
            field.set(dockable, value);
        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }
}
