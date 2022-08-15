package com.kaka.notice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基于计步器异步处理结果
 *
 * @param <V> 泛型参数
 * @author zkpursuit
 */
public class AsynLatchResult<V> extends AsynResult<V> {
    private final transient CountDownLatch cdl;

    public AsynLatchResult() {
        this.cdl = new CountDownLatch(1);
    }

    @Override
    protected boolean _isDone() {
        return this.cdl.getCount() == 0;
    }

    @Override
    public boolean isDone() {
        return this._isDone();
    }

    @Override
    public V get() {
        try {
            this.cdl.await();
        } catch (InterruptedException e) {
            return null;
        }
        return (V) this.result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.cdl.await(timeout, unit)) {
            return (V) this.result;
        }
        return null;
    }

    @Override
    public void set(V result) {
        this.cdl.countDown();
        this.result = result;
    }
}

