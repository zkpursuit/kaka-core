package com.kaka.notice;

/**
 * 消息观察者接口
 *
 * @author zkpursuit
 */
public interface IMediator {

    /**
     * 处理消息监听
     *
     * @param msg 通知消息
     */
    void handleMessage(Message msg);

    /**
     * 监听的消息列表，非Event实现
     *
     * @return 感兴趣的消息通知集合
     */
    Object[] listMessageInterests();

}
