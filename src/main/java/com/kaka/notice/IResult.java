package com.kaka.notice;

import java.io.Serializable;

/**
 *
 * @author zkpursuit
 * @param <V>
 */
public interface IResult<V> extends Serializable {
    void set(V result);
    V get();
}
