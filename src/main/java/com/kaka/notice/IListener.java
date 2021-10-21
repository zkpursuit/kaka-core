package com.kaka.notice;

/**
 * 事件监听器
 * <br>
 * 在已有类继承体系内的对象可直接继承此接口引入事件通知
 */
public interface IListener {
    /**
     * 事件消息处理方法
     *
     * @param message 事件消息
     * @param facade  事件总线
     */
    void onMessage(Message message, Facade facade);
}
