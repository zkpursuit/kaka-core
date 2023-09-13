package com.kaka.notice;

import com.kaka.util.ObjectPool;

/**
 * 控制命令类接口
 *
 * @author zkpursuit
 */
public interface ICommand extends INotifier, ObjectPool.Poolable {
    /**
     * 处理消息，此方法中不要用线程处理
     * <br>
     * 由于{@link Facade}中调度事件时，此方法被执行后即刻
     * 对本对象进行池化处理，将调用reset方法；如内部再使用线程，将引发不必要的错误。
     * <br>
     * 未池化的对象因不会调用reset方法，内部可使用线程。
     *
     * @param msg 通知消息
     * @see Message Message处理方法
     */
    void execute(Message msg);
}
