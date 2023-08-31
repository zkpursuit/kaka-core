package com.kaka.notice;

/**
 * 消息观察者，感知有兴趣的消息通知
 *
 * @author zkpursuit
 */
abstract public class Mediator extends Proxy implements IMediator {

    final static ThreadLocal<Message> messageThreadLocal = new ThreadLocal<>();

    /**
     * 构造方法
     */
    public Mediator() {
        super();
    }

    /**
     * 构造方法
     *
     * @param name 实例名
     */
    public Mediator(String name) {
        super(name);
    }

    /**
     * 设置执行回调参数
     *
     * @param params 回调参数
     */
    protected void returnCallbackResult(Object params) {
        Message msg = messageThreadLocal.get();
        if (msg == null) return;
        msg.setCallbackParams(this.getClass(), params);
    }

    /**
     * 执行事件通知
     *
     * @param msg 被执行的事件通知
     */
    void handleMessage0(Message msg) {
        messageThreadLocal.set(msg);
        this.handleMessage(msg);
        messageThreadLocal.remove();
    }

    /**
     * 处理消息监听
     *
     * @param msg 通知消息
     */
    abstract public void handleMessage(Message msg);

    /**
     * 监听的消息列表，非Event实现
     *
     * @return 感兴趣的消息通知集合
     */
    abstract public Object[] listMessageInterests();

}
