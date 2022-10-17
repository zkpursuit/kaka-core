package com.kaka.notice;

/**
 * 异步回调执行结果
 *
 * @param <V> 结果数据类型，内部已固定为Object
 */
public class CallbackResult<V> extends SyncResult<V> {

    /**
     * 事件处理器类型
     */
    public final String eventHandlerClass;

    /**
     * 构造方法
     *
     * @param result            事件处理结果
     * @param eventHandlerClass 处理事件的类，有可能是远程类，本地不存在，故此处为类的字符串表示
     */
    CallbackResult(V result, String eventHandlerClass) {
        this.set(result);
        this.eventHandlerClass = eventHandlerClass;
    }
}
