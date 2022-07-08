package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public final class MutableDouble extends Number implements Serializable, Comparable<MutableDouble>, Cloneable {

    public static MutableDouble of(final double value) {
        return new MutableDouble(value);
    }

    private double value;

    public MutableDouble() {
    }

    public MutableDouble(final double value) {
        this.value = value;
    }

    public MutableDouble(final String value) {
        this.value = Double.parseDouble(value);
    }

    public MutableDouble(final Number number) {
        this.value = number.doubleValue();
    }

    public double get() {
        return value;
    }

    public void set(final double value) {
        this.value = value;
    }

    public void set(final Number value) {
        this.value = value.doubleValue();
    }

    public double plus(double step) {
        value += step;
        return value;
    }

    public double reduce(double step) {
        value -= step;
        return value;
    }

    public double multiply(double v) {
        value *= v;
        return value;
    }

    public double divide(double v) {
        value /= v;
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (((Double) this.value).getClass() == obj.getClass()) {
                return Double.doubleToLongBits(value) == Double.doubleToLongBits((Double) obj);
            }
            if (this.getClass() == obj.getClass()) {
                return Double.doubleToLongBits(value) == Double.doubleToLongBits(((MutableDouble) obj).value);
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
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public boolean isNaN() {
        return Double.isNaN(value);
    }

    public boolean isInfinite() {
        return Double.isInfinite(value);
    }

    @Override
    public int compareTo(final MutableDouble other) {
        return Double.compare(value, other.value);
    }

    @Override
    public MutableDouble clone() {
        return new MutableDouble(value);
    }

}
