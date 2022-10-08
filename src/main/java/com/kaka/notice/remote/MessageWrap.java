package com.kaka.notice.remote;

import com.kaka.notice.Message;

import java.io.Serializable;

/**
 * 事件消息包装类，用于消息队列中序列化传输
 *
 * @author zkpursuit
 */
public class MessageWrap implements Serializable {
    public final String id;
    public final Message message;

    /**
     * 构造方法
     *
     * @param id      事件消息唯一标识，用于本地缓存
     * @param message 事件消息
     */
    public MessageWrap(String id, Message message) {
        this.id = id;
        this.message = message;
    }
}
