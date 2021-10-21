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
    public final String eventHanderClass;

    CallbackResult(V result, String eventHanderClass) {
        this.set(result);
        this.eventHanderClass = eventHanderClass;
    }
}
