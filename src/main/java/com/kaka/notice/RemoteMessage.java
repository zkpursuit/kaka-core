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
     * @param bodyMessage   待发送给远端处理的事件消息，必须为{@link com.kaka.notice.Message}或其子类对象
     */
    protected RemoteMessage(Object what, String bodyMessageId, Object bodyMessage) {
        super(what, bodyMessage);
        this.id = bodyMessageId;
    }

    /**
     * 获取包裹的事件对象
     *
     * @param <T> 事件对象类型限定
     * @return 事件对象
     */
    public <T extends Message> T getBodyMessage() {
        return (T) this.getBody();
    }
}
