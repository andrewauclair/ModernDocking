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
package io.github.andrewauclair.moderndocking.internal;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockingProperty;
import io.github.andrewauclair.moderndocking.Property;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Internal utilities for managing dockable properties
 */
public class DockableProperties {
    private static boolean loadingLegacyFile = false;

    /**
     * Unused. All methods are static
     */
    private DockableProperties() {
    }

    /**
     * If true, we're loading a file from before 0.12.0
     *
     * @param legacy Legacy flag
     */
    public static void setLoadingLegacyFile(boolean legacy) {
        loadingLegacyFile = legacy;
    }

    /**
     * Set the values of properties on a dockable
     *
     * @param wrapper The dockable to update
     * @param properties The properties to set on the dockable DockingProperty annotated fields
     */
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
                    Property prop = properties.get(property.name());

                    try {
                        if (loadingLegacyFile) {
                            Property.StringProperty legacyProp = (Property.StringProperty) properties.get(property.name());
                            prop = parseProperty(prop.getName(), field.getType().getSimpleName().toString(), legacyProp.getValue());
                        }
                        DockableProperties.validateProperty(field, prop);
                    }
                    catch (Exception e) {
                        // TODO possibly make a new DockingPropertyException
                        throw new RuntimeException(String.format("Dockable: '%s' (%s), default value: '%s' for field '%s' (%s) is invalid", dockable.getPersistentID(), dockable.getClass().getSimpleName(), property.defaultValue(), field.getName(), field.getType().getSimpleName()), e);
                    }

                    setProperty(dockable, field, prop);

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

    /**
     * Get the properties from the dockable as a map
     *
     * @param wrapper The dockable to get properties for
     *
     * @return Map of properties
     */
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

                properties.put(property.name(), getProperty(wrapper, property.name(), field));
            }
            catch (IllegalAccessException ignore) {
            }
        }

        return properties;
    }

    // throws if validation failed

    /**
     * Validate a property instance
     *
     * @param field The field for the property
     * @param property The docking property annotation
     */
    public static void validateProperty(Field field, DockingProperty property) {
        createProperty(field, property);
    }

    /**
     * Validate a property
     *
     * @param field The field for the property
     * @param property The internal property instance
     */
    public static void validateProperty(Field field, Property property) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(property);
        Property prop = createProperty(field, property.getName(), property.toString());
        if (prop.getType() != property.getType()) {
            throw new RuntimeException("Type of property does not match type of value.");
        }
    }

    /**
     * Parse a property string into the proper type
     *
     * @param property Property name
     * @param type Type of property
     * @param value The string value of the property
     *
     * @return Instance of Property with the proper Java class type
     */
    public static Property parseProperty(String property, String type, String value) {
        if (type.equals("byte")) {
            return new Property.ByteProperty(property, value.isEmpty() ? (byte) 0 : Byte.parseByte(value));
        }
        else if (type.equals("short")) {
            return new Property.ShortProperty(property, value.isEmpty() ? (short) 0 : Short.parseShort(value));
        }
        else if (type.equals("int")) {
            return new Property.IntProperty(property, value.isEmpty() ? 0 : Integer.parseInt(value));
        }
        else if (type.equals("long")) {
            return new Property.LongProperty(property, value.isEmpty() ? (long) 0 : Long.parseLong(value));
        }
        else if (type.equals("float")) {
            return new Property.FloatProperty(property, value.isEmpty() ? 0.0f : Float.parseFloat(value));
        }
        else if (type.equals("double")) {
            return new Property.DoubleProperty(property, value.isEmpty() ? 0.0 : Double.parseDouble(value));
        }
        else if (type.equals("char")) {
            return new Property.CharacterProperty(property, value.isEmpty() ? '\0' : value.charAt(0));
        }
        else if (type.equals("boolean")) {
            return new Property.BooleanProperty(property, !value.isEmpty() && Boolean.parseBoolean(value));
        }
        else if (type.equals("String")) {
            return new Property.StringProperty(property, value);
        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }

    private static Property createProperty(Field field, DockingProperty property) {
        String value = "";

        if (!Objects.equals(property.defaultValue(), "__no_default_value__")) {
            value = property.defaultValue();
        }
        return createProperty(field, property.name(), value);
    }

    private static Property createProperty(Field field, String property, String value) {
        Class<?> type = field.getType();

        if (type == byte.class) {
            return new Property.ByteProperty(property, value.isEmpty() ? (byte) 0 : Byte.parseByte(value));
        }
        else if (type == short.class) {
            return new Property.ShortProperty(property, value.isEmpty() ? (short) 0 : Short.parseShort(value));
        }
        else if (type == int.class) {
            return new Property.IntProperty(property, value.isEmpty() ? 0 : Integer.parseInt(value));
        }
        else if (type == long.class) {
            return new Property.LongProperty(property, value.isEmpty() ? (long) 0 : Long.parseLong(value));
        }
        else if (type == float.class) {
            return new Property.FloatProperty(property, value.isEmpty() ? 0.0f : Float.parseFloat(value));
        }
        else if (type == double.class) {
            return new Property.DoubleProperty(property, value.isEmpty() ? 0.0 : Double.parseDouble(value));
        }
        else if (type == char.class) {
            return new Property.CharacterProperty(property, value.isEmpty() ? '\0' : value.charAt(0));
        }
        else if (type == boolean.class) {
            return new Property.BooleanProperty(property, !value.isEmpty() && Boolean.parseBoolean(value));
        }
        else if (type == String.class) {
            return new Property.StringProperty(property, value);
        }
//        else if (type.isEnum()) {
//            return Integer.toString(((Enum<?>) field.get(dockable)).ordinal());
//            return "";
//        }
        else {
            throw new RuntimeException("Unsupported property type");
        }
    }

    private static Property getProperty(DockableWrapper wrapper, String property, Field field) throws IllegalAccessException {
        Dockable dockable = wrapper.getDockable();

        Class<?> type = field.getType();

        if (type == byte.class) {
            return new Property.ByteProperty(property, (byte) field.get(dockable));
        }
        else if (type == short.class) {
            return new Property.ShortProperty(property, (short) field.get(dockable));
        }
        else if (type == int.class) {
            return new Property.IntProperty(property, (int) field.get(dockable));
        }
        else if (type == long.class) {
            return new Property.LongProperty(property, (long) field.get(dockable));
        }
        else if (type == float.class) {
            return new Property.FloatProperty(property, (float) field.get(dockable));
        }
        else if (type == double.class) {
            return new Property.DoubleProperty(property, (double) field.get(dockable));
        }
        else if (type == char.class) {
            return new Property.CharacterProperty(property, (char) field.get(dockable));
        }
        else if (type == boolean.class) {
            return new Property.BooleanProperty(property, (boolean) field.get(dockable));
        }
        else if (type == String.class) {
            return new Property.StringProperty(property, (String) field.get(dockable));
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
