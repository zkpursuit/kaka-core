package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public final class MutableShort extends Number implements Serializable, Comparable<MutableShort>, Cloneable {

    public static MutableShort of(final short value) {
        return new MutableShort(value);
    }

    public short value;

    public MutableShort() {
    }

    public MutableShort(final short value) {
        this.value = value;
    }

    public MutableShort(final String value) {
        this.value = Short.parseShort(value);
    }

    public MutableShort(final Number number) {
        this.value = number.shortValue();
    }

    public short get() {
        return value;
    }

    public void set(final short value) {
        this.value = value;
    }

    public void set(final Number value) {
        this.value = value.shortValue();
    }

    public short plus(short step) {
        value += step;
        return value;
    }

    public short reduce(short step) {
        value -= step;
        return value;
    }

    public short multiply(short v) {
        value *= v;
        return value;
    }

    public short divide(short v) {
        value /= v;
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (((Short) this.value).getClass() == obj.getClass()) {
                return value == (Short) obj;
            }
            if (this.getClass() == obj.getClass()) {
                return value == ((MutableShort) obj).value;
            }
        }
        return false;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int compareTo(final MutableShort other) {
        return Short.compare(value, other.value);
    }

    @Override
    public MutableShort clone() {
        return new MutableShort(value);
    }

}
