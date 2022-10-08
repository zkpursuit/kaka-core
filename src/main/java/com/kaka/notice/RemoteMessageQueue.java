package com.kaka.notice;

import com.kaka.notice.remote.LocalMessageCache;
import com.kaka.notice.remote.MessageWrap;

import java.util.Map;

abstract public class RemoteMessageQueue {

    Facade facade;
    protected final String beforeTopic;
    protected final String afterTopic;
    protected final LocalMessageCache localMessageCache;

    /**
     * 构造方法
     *
     * @param beforeTopic 消息队列中被事件处理器处理前的订阅主题
     * @param afterTopic  消息队列中被事件处理器处理后的订阅主题
     */
    public RemoteMessageQueue(String beforeTopic, String afterTopic) {
        this.beforeTopic = beforeTopic;
        this.afterTopic = afterTopic;
        this.localMessageCache = this.initLocalMessageCache();
    }

    /**
     * 初始化事件消息处理器缓存
     *
     * @return 事件消息处理器缓存
     */
    abstract protected LocalMessageCache initLocalMessageCache();

    /**
     * 发布时间消息到消息队列中
     *
     * @param msgWrap 事件消息包装器
     * @param topic   消息队列中订阅主题
     */
    abstract protected void publishEventMessage(MessageWrap msgWrap, String topic);

    /**
     * 消息队列消费者首次接收到事件消息，此消费者订阅于beforeTopic主题
     * <br>
     * 此方法必须由实现类的队列消费主体调用
     *
     * @param remoteMsgWrap 接收到事件消息包装器
     */
    protected void receivedBeforeEventMessage(MessageWrap remoteMsgWrap) {
        String id = remoteMsgWrap.id;
        Message localEventMessage = this.localMessageCache.remove(id);
        if (localEventMessage != null) {
            facade.sendMessage(localEventMessage);
            return;
        }
        Message remoteEventMessage = remoteMsgWrap.message;
        facade.sendMessage(remoteEventMessage);
        //发布经事件处理器处理后的事件到消息队列，让事件发送方及时清除事件本地缓存
        publishEventMessage(remoteMsgWrap, this.afterTopic);
    }

    /**
     * 消息队列消费者收到事件消息被处理后的事件消息，此消费者订阅于afterTopic主题
     * <br>
     * 此方法必须由实现类的队列消费主体调用
     *
     * @param remoteMsgWrap 接收到事件消息包装器
     */
    protected void receivedAfterEventMessage(MessageWrap remoteMsgWrap) {
        String id = remoteMsgWrap.id;
        Message remoteEventMessage = remoteMsgWrap.message;
        Message localEventMessage = this.localMessageCache.remove(id);
        if (localEventMessage == null) {
            facade.sendMessage(new Message("remote_processed_message", remoteEventMessage));
            return;
        }
        Map<Object, IResult> eventResultMap = remoteEventMessage.resultMap;
        if (eventResultMap == null) return;
        eventResultMap.forEach((Object key, IResult result) -> {
            String keyStr = String.valueOf(key);
            IResult localResult = localEventMessage.getResult(keyStr);
            if (localResult instanceof AsynResult) {
                localResult.set(result.get());
            } else if (result instanceof CallbackResult) {
                localEventMessage.setResult(keyStr, result);
                localEventMessage.callback(keyStr);
            }
        });
    }

}
