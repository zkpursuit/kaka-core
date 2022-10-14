package com.kaka.notice;

import com.kaka.util.ObjectPool.Poolable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 消息通知对象，亦可理解为事件上下文对象
 *
 * @author zkpursuit
 */
public class Message implements Poolable, Serializable {

    protected Object what;
    protected Object body;
    protected Map<Object, IResult> resultMap;
    private transient Consumer<IResult<Object>> callback;

    /**
     * 构造方法
     *
     * @param what 消息通知标识
     * @param body 绑定的数据
     */
    public Message(Object what, Object body) {
        this.what = what;
        this.body = body;
    }

    /**
     * 构造方法
     *
     * @param what 消息通知标识
     */
    public Message(Object what) {
        this(what, null);
    }

    /**
     * 构造方法
     *
     * @param what     消息通知标识
     * @param body     绑定的数据
     * @param callback 事件执行完成后的回调函数，第一个泛型参数为处理本事件的类，第二个泛型参数为处理结果
     */
    public Message(Object what, Object body, Consumer<IResult<Object>> callback) {
        this(what, body);
        this.callback = callback;
    }

    public Object getWhat() {
        return this.what;
    }

    public Object getBody() {
        return this.body;
    }

    /**
     * 初始化设置事件通知处理结果
     *
     * @param <T>    数据类型限定
     * @param key    因广播事件通知，必须为处理结果定义唯一标识
     * @param result 处理结果数据容器
     * @return 处理结果数据容器
     */
    final protected <T, R extends IResult<T>> R _setResult(Object key, R result) {
        synchronized (this) {
            if (this.resultMap == null) {
                this.resultMap = new HashMap<>();
            }
            this.resultMap.put(key, result);
        }
        return result;
    }

    /**
     * 获取处理结果数据容器
     *
     * @param <T> 数据类型限定
     * @param key 因广播事件通知，必须为处理结果定义唯一标识名
     * @return 处理结果数据容器
     */
    final protected <T, R extends IResult<T>> R _getResult(Object key) {
        synchronized (this) {
            if (this.resultMap == null) {
                return null;
            }
            return (R) this.resultMap.get(key);
        }
    }

    /**
     * 初始化设置事件通知处理结果
     *
     * @param <T>    数据类型限定
     * @param name   因广播事件通知，必须为处理结果定义唯一标识名
     * @param result 处理结果数据容器
     * @return 处理结果数据容器
     */
    public <T, R extends IResult<T>> R setResult(String name, R result) {
        return this._setResult(name, result);
    }

    /**
     * 获取处理结果数据容器
     *
     * @param <T>  数据类型限定
     * @param name 因广播事件通知，必须为处理结果定义唯一标识名
     * @return 处理结果数据容器
     */
    public <T, R extends IResult<T>> R getResult(String name) {
        return this._getResult(name);
    }

    /**
     * 在执行事件的对象中设置回调参数
     *
     * @param eventHandlerClass 执行事件的对象，为{@link com.kaka.notice.Command}或者{@link com.kaka.notice.Mediator}
     * @param params            回调参数
     */
    void setCallbackParams(Class<?> eventHandlerClass, Object params) {
        String id = eventHandlerClass.getTypeName();
        this._setResult(id, new CallbackResult<>(params, id));
    }

    /**
     * 事件执行完成后的回调
     *
     * @param id 事件执行器的类TypeName，亦为回调结果唯一标识
     */
    void callback(String id) {
        if (this.callback == null) return;
        IResult<Object> result = this._getResult(id);
        if (result == null) return;
        this.callback.accept(result);
    }

    @Override
    public void reset() {
        this.what = null;
        this.body = null;
        synchronized (this) {
            if (this.resultMap != null) {
                this.resultMap.clear();
            }
        }
        if (this.callback != null) {
            this.callback = null;
        }
    }

}
