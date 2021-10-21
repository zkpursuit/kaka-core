package com.kaka.notice;

/**
 * 远程消息队列异步执行结果
 *
 * @param <V> 结果数据类型
 */
class RemoteAsynResult<V> extends AsynResult<V> {

    RemoteAsynResult() {
    }

    public boolean isDone() {
        return false;
    }

    @Override
    public V get() {
        return (V) this.result;
    }

    @Override
    public void set(V result) {
        this.result = result;
    }

}
