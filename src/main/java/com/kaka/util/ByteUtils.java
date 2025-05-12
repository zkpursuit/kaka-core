package com.kaka.util;

public class ByteUtils {

    public static byte[] toByteArray(boolean value) {
        if (value) return new byte[]{1};
        return new byte[]{0};
    }

    public static byte[] toByteArray(short value) {
        return new byte[]{(byte) (value >> 8), (byte) value};
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

    public static byte[] toByteArray(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            bytes[i] = (byte) ((value >> offset) & 0xff);
        }
        return bytes;
    }

    public static byte[] toByteArray(float value) {
        return toByteArray(Float.floatToIntBits(value));
    }

    public static byte[] toByteArray(double value) {
        return toByteArray(Double.doubleToLongBits(value));
    }

    public static void writeToByteArray(int value, byte[] bytes, int destIndex) {
        bytes[destIndex] = (byte) (value >> 24);
        bytes[destIndex + 1] = (byte) (value >> 16);
        bytes[destIndex + 2] = (byte) (value >> 8);
        bytes[destIndex + 3] = (byte) value;
    }

    public static void writeToByteArray(short value, byte[] bytes, int destIndex) {
        bytes[destIndex] = (byte) (value >> 8);
        bytes[destIndex + 1] = (byte) value;
    }

    public static int toInt(byte b1, byte b2, byte b3, byte b4) {
        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    public static void checkArgument(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("byte array is null or empty!");
        }
    }

    public static int toInt(byte[] bytes) {
        checkArgument(bytes);
        return toInt(bytes[0], bytes[1], bytes[2], bytes[3]);
    }

    public static int toInt(byte[] bytes, int start) {
        checkArgument(bytes);
        return toInt(bytes[start], bytes[start + 1], bytes[start + 2], bytes[start + 3]);
    }

    public static long toLong(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        return (b1 & 0xFFL) << 56
                | (b2 & 0xFFL) << 48
                | (b3 & 0xFFL) << 40
                | (b4 & 0xFFL) << 32
                | (b5 & 0xFFL) << 24
                | (b6 & 0xFFL) << 16
                | (b7 & 0xFFL) << 8
                | (b8 & 0xFFL);
    }

    public static long toLong(byte[] bytes) {
        checkArgument(bytes);
        return toLong(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
    }

    public static long toLong(byte[] bytes, int start) {
        checkArgument(bytes);
        return toLong(bytes[start], bytes[start + 1], bytes[start + 2], bytes[start + 3], bytes[start + 4], bytes[start + 5], bytes[start + 6], bytes[start + 7]);
    }

    public static boolean toBoolean(byte[] bytes, int start) {
        checkArgument(bytes);
        byte b = bytes[start];
        return b == 1;
    }

    public static short toShort(byte b1, byte b2) {
        return (short) ((b1 << 8) | (b2 & 0xFF));
    }

    public static short toShort(byte[] bytes) {
        return toShort(bytes[0], bytes[1]);
    }

    public static short toShort(byte[] bytes, int start) {
        checkArgument(bytes);
        return toShort(bytes[start], bytes[start + 1]);
    }

    public static float toFloat(byte[] bytes) {
        checkArgument(bytes);
        return Float.intBitsToFloat(toInt(bytes, 0));
    }

    public static float toFloat(byte[] bytes, int start) {
        checkArgument(bytes);
        return Float.intBitsToFloat(toInt(bytes, start));
    }

    public static double toDouble(byte[] bytes) {
        checkArgument(bytes);
        return Double.longBitsToDouble(toLong(bytes, 0));
    }

    public static double toDouble(byte[] bytes, int start) {
        checkArgument(bytes);
        return Double.longBitsToDouble(toLong(bytes, start));
    }
}
