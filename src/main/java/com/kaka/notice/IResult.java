package com.kaka.notice;

import java.io.Serializable;

/**
 * 事件结果
 *
 * @param <V> 泛型参数
 * @author zkpursuit
 */
public interface IResult<V> extends Serializable {

    /**
     * 设置结果
     *
     * @param result 结果
     */
    void set(V result);

    /**
     * 获取结果
     *
     * @return 结果
     */
    V get();
}
