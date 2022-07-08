package com.kaka.util.mutable;

import java.io.Serializable;

/**
 * @author zkpursuit
 */
public class MutableBoolean implements Serializable, Comparable<MutableBoolean>, Cloneable {
    private boolean value;

    public MutableBoolean() {
    }

    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public MutableBoolean(Boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (obj instanceof MutableBoolean) {
            return this.value == ((MutableBoolean) obj).get();
        }
        return false;
    }

    public int hashCode() {
        return this.value ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
    }

    public int compareTo(MutableBoolean other) {
        return Boolean.compare(this.value, other.value);
    }

    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public MutableBoolean clone() {
        return new MutableBoolean(value);
    }
}
