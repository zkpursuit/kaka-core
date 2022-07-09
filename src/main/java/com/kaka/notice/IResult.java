package com.kaka.notice;

import java.io.Serializable;

/**
 * 事件结果
 *
 * @param <V> 泛型参数
 * @author zkpursuit
 */
public interface IResult<V> extends Serializable {
    void set(V result);

    V get();
}
