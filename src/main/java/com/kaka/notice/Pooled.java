package com.kaka.notice;

import com.kaka.util.StringUtils;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Pooled {

    static int pool_capacity = 1024;

    static {
        String v = System.getProperty("msg_pool_capacity");
        if (StringUtils.isInteger(v)) {
            pool_capacity = Integer.parseInt(v);
        }
    }

    static final Map<Class<? extends Message>, Queue<Message>> poolMap = new ConcurrentHashMap<>();

    /**
     * 获取池化的对象
     *
     * @param clazz   对象池对应的key
     * @param builder 池化对象构建器，当不能从对象池获取到对象时将返回新创建的对象
     * @param <T>     对象限定类型
     * @return 待池化的对象
     */
    public static <T extends Message> T message(Class<T> clazz, Supplier<T> builder) {
        Queue<Message> pool = poolMap.computeIfAbsent(clazz, k -> new ArrayBlockingQueue<>(pool_capacity));
        Message msg = pool.poll();
        if (msg == null) {
            T _msg = builder.get();
            _msg.poolable = true;
            return _msg;
        }
        msg.poolable = true;
        return (T) msg;
    }

    /**
     * 将对象放入对象池
     *
     * @param msg 消息对象
     */
    static void release(Message msg) {
        if (!msg.poolable) return;
        msg.poolable = false;
        Queue<Message> pool = Pooled.poolMap.get(msg.getClass());
        if (pool == null) return;
        pool.offer(msg);
    }

}
