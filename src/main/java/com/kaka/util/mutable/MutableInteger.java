package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public final class MutableInteger extends Number implements Serializable, Comparable<MutableInteger>, Cloneable {

    public static MutableInteger of(final int value) {
        return new MutableInteger(value);
    }

    private int value;

    public MutableInteger() {
    }

    public MutableInteger(final int value) {
        this.value = value;
    }

    public MutableInteger(final String value) {
        this.value = Integer.parseInt(value);
    }

    public MutableInteger(final Number number) {
        this.value = number.intValue();
    }

    public int get() {
        return value;
    }

    public void set(final int value) {
        this.value = value;
    }

    public void set(final Number value) {
        this.value = value.intValue();
    }

    public int plus(int step) {
        value += step;
        return value;
    }

    public int reduce(int step) {
        value -= step;
        return value;
    }

    public int multiply(int v) {
        value *= v;
        return value;
    }

    public int divide(int v) {
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
            if (((Integer) this.value).getClass() == obj.getClass()) {
                return value == (Integer) obj;
            }
            if (this.getClass() == obj.getClass()) {
                return value == ((MutableInteger) obj).value;
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
    public int compareTo(final MutableInteger other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public MutableInteger clone() {
        return new MutableInteger(value);
    }

}
