package com.kaka.notice;

/**
 * 事件消息本地缓存，缓存对象必须常驻内存<br>
 * 无论本地是否缓存事件消息，当收到远端事件消息时都将由事件总线分发处理，如果事件总线中存在此事件处理器，则将执行事件处理逻辑<br>
 * <br>
 * 应该尽可能使用其它缓存框架来限定对象的缓存时间，当未消费到事件消息时可适当自动移除缓存
 * <br>
 * 非消息队列不可用的情况，都能消费事件消息，从而从本地缓存中移除对应的事件消息
 */
public interface RemoteMessageCache {
    /**
     * 写入本地缓存
     *
     * @param id      事件消息唯一标识
     * @param message 事件消息
     */
    void add(String id, Message message);

    /**
     * 获取本地缓存事件消息
     *
     * @param id 事件消息唯一标识
     * @return 事件消息
     */
    Message get(String id);

    /**
     * 移除本地缓存
     *
     * @param id 事件消息唯一标识
     * @return 移除的事件消息
     */
    Message remove(String id);
}
