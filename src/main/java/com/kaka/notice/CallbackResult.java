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

    CallbackResult(V result, String eventHandlerClass) {
        this.set(result);
        this.eventHandlerClass = eventHandlerClass;
    }
}
