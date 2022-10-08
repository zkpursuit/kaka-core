package com.kaka.notice.remote;

import com.kaka.notice.Message;

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
