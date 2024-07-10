package ModernDocking;

// TODO might have to expose this to the applications
// TODO not sure how we go about converting to the right type without forcing separate calls
public abstract class Property {
    private final String name;

    public Property(String name) {

        this.name = name;
    }

    public abstract Class<?> getType();

    public abstract boolean isNull();

    public String getName() {
        return name;
    }

    public static class ByteProperty extends Property {
        private final byte value;

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

    public static class ShortProperty extends Property {
        private final short value;

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

    public static class IntProperty extends Property {
        private final int value;

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

    public static class LongProperty extends Property {
        private final long value;

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

    public static class FloatProperty extends Property {
        private final float value;

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

    public static class DoubleProperty extends Property {
        private final double value;

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

    public static class CharacterProperty extends Property {
        private final char value;

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

    public static class BooleanProperty extends Property {
        private final boolean value;

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

    public static class StringProperty extends Property {
        private final String value;

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