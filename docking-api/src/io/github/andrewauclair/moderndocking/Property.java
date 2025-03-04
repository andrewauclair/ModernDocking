package io.github.andrewauclair.moderndocking;

/**
 * Base class for all Property classes
 */
public abstract class Property {
    private final String name;

    /**
     * Create a new instance with name
     *
     * @param name The name of the property
     */
    public Property(String name) {
        this.name = name;
    }

    /**
     * Get the actual type of the property
     *
     * @return The type of the property
     */
    public abstract Class<?> getType();

    /**
     * Check if the property is a null value
     *
     * @return Is the property null?
     */
    public abstract boolean isNull();

    /**
     * Get the name of the property
     *
     * @return Name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Property class that provides access to a byte
     */
    public static class ByteProperty extends Property {
        private final byte value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public ByteProperty(String name, byte value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return byte.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Byte.toString(value);
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a short
     */
    public static class ShortProperty extends Property {
        private final short value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public ShortProperty(String name, short value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return short.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Short.toString(value);
        }

        public short getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a int
     */
    public static class IntProperty extends Property {
        private final int value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public IntProperty(String name, int value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return int.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a long
     */
    public static class LongProperty extends Property {
        private final long value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public LongProperty(String name, long value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return long.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

        public long getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a float
     */
    public static class FloatProperty extends Property {
        private final float value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public FloatProperty(String name, float value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return float.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Float.toString(value);
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a double
     */
    public static class DoubleProperty extends Property {
        private final double value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public DoubleProperty(String name, double value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return double.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a char
     */
    public static class CharacterProperty extends Property {
        private final char value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public CharacterProperty(String name, char value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return char.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Character.toString(value);
        }

        public char getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a boolean
     */
    public static class BooleanProperty extends Property {
        private final boolean value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public BooleanProperty(String name, boolean value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return boolean.class;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String toString() {
            return Boolean.toString(value);
        }

        public boolean getValue() {
            return value;
        }
    }

    /**
     * Property class that provides access to a String
     */
    public static class StringProperty extends Property {
        private final String value;

        /**
         * Create a new instance
         *
         * @param name The name of the property
         * @param value The value of the property
         */
        public StringProperty(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public String toString() {
            return value;
        }

        public String getValue() {
            return value;
        }
    }
}