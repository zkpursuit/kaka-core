package com.kaka.notice;

import com.kaka.util.ExceptionUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于计步器异步处理结果
 *
 * @param <V> 泛型参数
 * @author zkpursuit
 */
public class AsynLatchResult<V> extends AsynResult<V> {
    protected final transient CountDownLatch cdl;

    public AsynLatchResult() {
        this.cdl = new CountDownLatch(1);
    }

    /**
     * 内部调用，尽可能的防止锁嵌套，避免死锁
     *
     * @return 是否赋值结果
     */
    @Override
    protected boolean _isDone() {
        return this.cdl.getCount() == 0;
    }

    /**
     * 结果是否处理完成
     *
     * @return true处理完成
     */
    @Override
    public boolean isDone() {
        return this._isDone();
    }

    /**
     * 获取事件通知处理结果具体数值
     * <br> 如未设置结果，则线程一直等待。
     *
     * @return 处理结果具体数值
     */
    @Override
    public V get() {
        try {
            this.cdl.await();
        } catch (InterruptedException e) {
            ExceptionUtils.processInterruptedException();
            this.cdl.countDown();
            return null;
        }
        return (V) this.result;
    }

    /**
     * 获取事件通知处理结果具体数值
     *
     * @param timeout 超时时间
     * @param unit    超时时间类型
     * @return 事件处理结果
     * @throws Exception {@link InterruptedException}或者{@link TimeoutException}
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws Exception {
        if (this.cdl.await(timeout, unit)) {
            return (V) this.result;
        }
        this.set(null);
        throw new TimeoutException("Getting result timeout");
    }

    /**
     * 设置事件处理结果
     *
     * @param result 事件处理结果
     */
    @Override
    public void set(V result) {
        this.cdl.countDown();
        this.result = result;
    }
}

