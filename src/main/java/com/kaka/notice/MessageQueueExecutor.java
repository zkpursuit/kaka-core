package com.kaka.notice;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 本地事件消息队列执行器
 * <br> 此执行器当在第一次调用addMessage后将一直执行下去，直到事件队列中的所有事件被执行完毕。
 * <br> 当需要暂停执行时可继承此类自行实现，比如增加一个AtomicBoolean对象标记是否执行。
 *
 * @author zkpursuit
 */
public class MessageQueueExecutor {
    //事件队列
    protected final Queue<Message> queue;
    //事件调度中心
    protected final Facade facade;
    //事件队列是否正在被消费执行
    protected final AtomicBoolean doing;

    /**
     * 构造方法
     *
     * @param facade 事件调度中心
     */
    public MessageQueueExecutor(Facade facade) {
        this.facade = facade;
        this.queue = initQueue();
        this.doing = new AtomicBoolean(false);
    }

    protected Queue<Message> initQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * 添加事件消息到执行队列
     *
     * @param message 事件消息
     */
    public void addMessage(Message message) {
        queue.add(message);
        if (doing.get()) return;
        execute();
    }

    /**
     * 执行事件消息
     */
    protected void execute() {
        if (queue.isEmpty()) {
            this.doing.set(false);
            return;
        }
        this.doing.set(true);
        final Message event = queue.poll();
        Executor threadPoll = facade.getThreadPool();
        threadPoll.execute(() -> {
            facade.sendMessage(event);
            execute();
        });
    }

}
