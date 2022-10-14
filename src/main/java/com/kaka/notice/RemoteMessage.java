package com.kaka.notice;

/**
 * 远端事件包装
 *
 * @author zkpursuit
 */
public class RemoteMessage extends Message {
    public final String id;

    /**
     * 构造方法
     *
     * @param what          接收远端事件或远端事件处理结果的逻辑处理器事件名
     * @param bodyMessageId 远端事件唯一ID
     * @param bodyMessage   待发送给远端处理的事件消息
     */
    protected RemoteMessage(Object what, String bodyMessageId, Object bodyMessage) {
        super(what, bodyMessage);
        this.id = bodyMessageId;
    }
}
