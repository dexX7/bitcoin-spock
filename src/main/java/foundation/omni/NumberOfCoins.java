package foundation.omni;

import java.math.BigDecimal;

/**
 * Number type to represent an Omni protocol number of coins type.
 */
public class NumberOfCoins extends Number implements Comparable<NumberOfCoins> {
    private final BigDecimal value;

    public static final BigDecimal   MIN_VALUE = new BigDecimal("0.00000001");
    public static final BigDecimal   MAX_VALUE = new BigDecimal("92233720368.54775807");

    public NumberOfCoins(NumberOfCoins other) {
        value = other.value;
    }

    public NumberOfCoins(BigDecimal other) {
        value = other;
    }

    public NumberOfCoins(String val) {
        this(new BigDecimal(val));
    }

    public NumberOfCoins(double val) {
        this(BigDecimal.valueOf(val));
    }

    public NumberOfCoins(long val) {
        this(BigDecimal.valueOf(val));
    }

    public NumberOfCoins(int val) {
        this(new BigDecimal((long)val));
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof NumberOfCoins) {
            NumberOfCoins other = (NumberOfCoins) obj;
            return value.compareTo(other.value) == 0;
        }

        if (obj instanceof BigDecimal) {
            BigDecimal other = (BigDecimal) obj;
            return value.compareTo(other) == 0;
        }

        return obj.equals(value);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    public int compareTo(NumberOfCoins other) {
        return value.compareTo(other.value);
    }

    public int compareTo(BigDecimal other) {
        return value.compareTo(other);
    }
}
