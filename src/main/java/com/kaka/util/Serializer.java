package com.kaka.util;

/**
 * 序列化器接口
 *
 * @author zkpursuit
 */
public interface Serializer<T> {
    byte[] serialize(T obj);

    T deserialize(byte[] bytes);
}
