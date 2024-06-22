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
import ModernDocking.DockingProperty;
import ModernDocking.Property;
import ModernDocking.layouts.DockingSimplePanelNode;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DockableProperties {
    public static void configureProperties(DockableWrapper wrapper, Map<String, Property> properties) {
        Dockable dockable = wrapper.getDockable();

        // remove any existing properties
        for (String key : wrapper.getProperties().keySet()) {
            wrapper.removeProperty(key);
        }

        // add all properties to the wrapper
        for (String key : properties.keySet()) {
            wrapper.setProperty(key, properties.get(key));
        }

        List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(DockingProperty.class) != null)
                .collect(Collectors.toList());

        for (Field field : dockingPropFields) {
            try {
                // make sure we can access the field if it is private/protected. only try this if we're sure we can't already access it
                // because it may result in an IllegalAccessException for trying
                if (!field.canAccess(dockable)) {
                    field.setAccessible(true);
                }

                // grab the property and store the value by its name
                DockingProperty property = field.getAnnotation(DockingProperty.class);

                if (properties.containsKey(property.name())) {
                    try {
                        DockableProperties.validateProperty(field, properties.get(property.name()));
                    }
                    catch (Exception e) {
                        // TODO possibly make a new DockingPropertyException
                        throw new RuntimeException(String.format("Dockable: '%s' (%s), default value: '%s' for field '%s' (%s) is invalid", dockable.getPersistentID(), dockable.getClass().getSimpleName(), property.defaultValue(), field.getName(), field.getType().getSimpleName()), e);
                    }

                    setProperty(dockable, field, properties.get(property.name()));

                    // remove the property from the wrapper as it is more specific than the static props
                    wrapper.removeProperty(property.name());
                }
                else {
                    // set the default of the type
                    setProperty(dockable, field, createProperty(field, property));
                }
            } catch (IllegalAccessException | SecurityException e) {
                // TODO handle this better
                e.printStackTrace();
            }
        }

        dockable.updateProperties();
    }



    public static Map<String, Property> saveProperties(DockableWrapper wrapper) {
        Dockable dockable = wrapper.getDockable();

        Map<String, Property> properties = new HashMap<>(wrapper.getProperties());

        List<Field> dockingPropFields = Arrays.stream(dockable.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(DockingProperty.class) != null)
                .collect(Collectors.toList());

        for (Field field : dockingPropFields) {
            try {
                // make sure we can access the field if it is private/protected
                field.setAccessible(true);

                // grab the property and store the value by its name
                DockingProperty property = field.getAnnotation(DockingProperty.class);

                properties.put(property.name(), getProperty(wrapper, field));
            }
            catch (IllegalAccessException ignore) {
            }
        }

        return properties;
    }

    // throws if validation failed
    public static void validateProperty(Field field, DockingProperty property) {
        createProperty(field, property);
    }

    public static void validateProperty(Field field, Property property) {
        if (createProperty(field, property.toString()).getType() != property.getType()) {
            throw new RuntimeException("Type of property does not match type of value.");
        }
    }

    private static Property createProperty(Field field, DockingProperty property) {
        String value = "";

        if (!Objects.equals(property.defaultValue(), "__no_default_value__")) {
            value = property.defaultValue();
        }
        return createProperty(field, value);
    }

    private static Property createProperty(Field field, String value) {
        Class<?> type = field.getType();

        if (type == byte.class) {
            return new Property.ByteProperty(value.isEmpty() ? (byte) 0 : Byte.parseByte(value));
        }
        else if (type == short.class) {
            return new Property.ShortProperty(value.isEmpty() ? (short) 0 : Short.parseShort(value));
        }
        else if (type == int.class) {
            return new Property.IntProperty(value.isEmpty() ? 0 : Integer.parseInt(value));
        }
        else if (type == long.class) {
            return new Property.LongProperty(value.isEmpty() ? (long) 0 : Long.parseLong(value));
        }
        else if (type == float.class) {
            return new Property.FloatProperty(value.isEmpty() ? 0.0f : Float.parseFloat(value));
        }
        else if (type == double.class) {
            return new Property.DoubleProperty(value.isEmpty() ? 0.0 : Double.parseDouble(value));
        }
        else if (type == char.class) {
            return new Property.CharacterProperty(value.isEmpty() ? '\0' : value.charAt(0));
        }
        else if (type == boolean.class) {
            return new Property.BooleanProperty(!value.isEmpty() && Boolean.parseBoolean(value));
        }
        else if (type == String.class) {
            return new Property.StringProperty(value);
        }
//        else if (type.isEnum()) {
//            return Integer.toString(((Enum<?>) field.get(dockable)).ordinal());
//            return "";
//        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }

    private static Property getProperty(DockableWrapper wrapper, Field field) throws IllegalAccessException {
        Dockable dockable = wrapper.getDockable();

        Class<?> type = field.getType();

        if (type == byte.class) {
            return new Property.ByteProperty((byte) field.get(dockable));
        }
        else if (type == short.class) {
            return new Property.ShortProperty((short) field.get(dockable));
        }
        else if (type == int.class) {
            return new Property.IntProperty((int) field.get(dockable));
        }
        else if (type == long.class) {
            return new Property.LongProperty((long) field.get(dockable));
        }
        else if (type == float.class) {
            return new Property.FloatProperty((float) field.get(dockable));
        }
        else if (type == double.class) {
            return new Property.DoubleProperty((double) field.get(dockable));
        }
        else if (type == char.class) {
            return new Property.CharacterProperty((char) field.get(dockable));
        }
        else if (type == boolean.class) {
            return new Property.BooleanProperty((boolean) field.get(dockable));
        }
        else if (type == String.class) {
            return new Property.StringProperty((String) field.get(dockable));
        }
//        else if (type.isEnum()) {
//            return Integer.toString(((Enum<?>) field.get(dockable)).ordinal());
//            return "";
//        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }

    private static void setProperty(Dockable dockable, Field field, Property value) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == byte.class) {
            field.set(dockable, ((Property.ByteProperty) value).getValue());
        }
        else if (type == short.class) {
            field.set(dockable, ((Property.ShortProperty) value).getValue());
        }
        else if (type == int.class) {
            field.set(dockable, ((Property.IntProperty) value).getValue());
        }
        else if (type == long.class) {
            field.set(dockable, ((Property.LongProperty) value).getValue());
        }
        else if (type == float.class) {
            field.set(dockable, ((Property.FloatProperty) value).getValue());
        }
        else if (type == double.class) {
            field.set(dockable, ((Property.DoubleProperty) value).getValue());
        }
        else if (type == char.class) {
            field.set(dockable, ((Property.CharacterProperty) value).getValue());
        }
        else if (type == boolean.class) {
            field.set(dockable, ((Property.BooleanProperty) value).getValue());
        }
        else if (type == String.class) {
            field.set(dockable, ((Property.StringProperty) value).getValue());
        }
//        else if (type.isEnum()) {
//            int ordinal = Integer.parseInt(value);
//
//            field.set(dockable, type.getEnumConstants()[ordinal]);
//        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }
}
