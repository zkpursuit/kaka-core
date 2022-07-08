package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public final class MutableLong extends Number implements Serializable, Comparable<MutableLong>, Cloneable {

    public static MutableLong of(final long value) {
        return new MutableLong(value);
    }

    public long value;

    public MutableLong() {
    }

    public MutableLong(final long value) {
        this.value = value;
    }

    public MutableLong(final String value) {
        this.value = Long.parseLong(value);
    }

    public MutableLong(final Number number) {
        this.value = number.longValue();
    }

    public long get() {
        return value;
    }

    public void set(final long value) {
        this.value = value;
    }

    public void set(final Number value) {
        this.value = value.longValue();
    }

    public long plus(long step) {
        value += step;
        return value;
    }

    public long reduce(long step) {
        value -= step;
        return value;
    }

    public long multiply(long v) {
        value *= v;
        return value;
    }

    public long divide(long v) {
        value /= v;
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (((Long) this.value).getClass() == obj.getClass()) {
                return value == (Long) obj;
            }
            if (this.getClass() == obj.getClass()) {
                return value == ((MutableLong) obj).value;
            }
        }
        return false;
    }

    @Override
    public int intValue() {
        return (int) value;
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
    public int compareTo(final MutableLong other) {
        return Long.compare(value, other.value);
    }

    @Override
    public MutableLong clone() {
        return new MutableLong(value);
    }

}
