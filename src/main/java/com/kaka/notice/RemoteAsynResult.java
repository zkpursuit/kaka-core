package com.kaka.notice;

import java.util.concurrent.TimeUnit;

/**
 * 远程消息队列异步执行结果
 *
 * @param <V> 结果数据类型
 */
class RemoteAsynResult<V> extends AsynResult<V> {

    RemoteAsynResult() {
    }

    public boolean isDone() {
        return this._isDone();
    }

    @Override
    public V get() {
        return (V) this.result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException {
        return this.get();
    }

    @Override
    public void set(V result) {
        this.result = result;
    }

}
