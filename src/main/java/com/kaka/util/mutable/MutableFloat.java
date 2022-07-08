package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public final class MutableFloat extends Number implements Serializable, Comparable<MutableFloat>, Cloneable {

    public static MutableFloat of(final float value) {
        return new MutableFloat(value);
    }

    public float value;

    public MutableFloat() {
    }

    public MutableFloat(final float value) {
        this.value = value;
    }

    public MutableFloat(final String value) {
        this.value = Float.parseFloat(value);
    }

    public MutableFloat(final Number number) {
        this.value = number.floatValue();
    }

    public float get() {
        return value;
    }

    public void set(final float value) {
        this.value = value;
    }

    public void set(final Number value) {
        this.value = value.floatValue();
    }

    public float plus(float step) {
        value += step;
        return value;
    }

    public float reduce(float step) {
        value -= step;
        return value;
    }

    public float multiply(float v) {
        value *= v;
        return value;
    }

    public float divide(float v) {
        value /= v;
        return value;
    }

    @Override
    public String toString() {
        return Float.toString(value);
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (((Float) this.value).getClass() == obj.getClass()) {
                return Float.floatToIntBits(value) == Float.floatToIntBits((Float) obj);
            }
            if (this.getClass() == obj.getClass()) {
                return Float.floatToIntBits(value) == Float.floatToIntBits(((MutableFloat) obj).value);
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
        return (long) value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public boolean isNaN() {
        return Float.isNaN(value);
    }

    public boolean isInfinite() {
        return Float.isInfinite(value);
    }

    @Override
    public int compareTo(final MutableFloat other) {
        return Float.compare(value, other.value);
    }

    @Override
    public MutableFloat clone() {
        return new MutableFloat(value);
    }

}
