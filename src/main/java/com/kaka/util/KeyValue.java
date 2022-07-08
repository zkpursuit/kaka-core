package com.kaka.util;

/**
 * @param <K>
 * @param <V>
 * @author zkpursuit
 */
public interface KeyValue<K, V> {

    void setKey(K key);

    void setValue(V value);

    K getKey();

    V getValue();

}
