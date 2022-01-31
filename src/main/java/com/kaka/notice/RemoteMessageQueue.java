package com.kaka.notice;

import com.kaka.util.ReflectUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

abstract public class RemoteMessageQueue {

    /**
     * 事件消息包装类，用于消息队列中序列化传输
     */
    public static class MessageWrap implements Serializable {
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

    /**
     * 事件消息本地缓存，缓存对象必须常驻内存，不可持久化到磁盘
     * <br>
     * 应该尽可能使用其它缓存框架来限定对象的缓存时间，当未消费到事件消息时可适当自动移除缓存
     * <br>
     * 非消息队列不可用的情况，都能消费事件消息，从而从本地缓存中移除对应的事件消息
     */
    public interface LocalMessageCache {
        /**
         * 写入本地缓存
         *
         * @param id      事件消息唯一标识
         * @param message 事件消息
         */
        void add(String id, Message message);

        /**
         * 移除本地缓存
         *
         * @param id 事件消息唯一标识
         * @return 移除的事件消息
         */
        Message remove(String id);
    }

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
