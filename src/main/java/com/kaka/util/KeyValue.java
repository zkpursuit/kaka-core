package com.kaka.util;

/**
 * 键值对象
 *
 * @param <K> 键泛型限定类型
 * @param <V> 值泛型限定类型
 * @author zkpursuit
 */
public interface KeyValue<K, V> {

    void setKey(K key);

    void setValue(V value);

    K getKey();

    V getValue();

}
