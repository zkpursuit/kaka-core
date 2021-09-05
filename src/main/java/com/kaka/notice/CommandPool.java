package com.kaka.notice;

import com.kaka.util.ObjectPool;

import java.util.Objects;

/**
 * {@link Command}对象池
 * <br> 此类在类包外不可访问
 *
 * @author zkpursuit
 */
class CommandPool extends ObjectPool<Command> {

    private final Facade context;
    final Class<? extends Command> cls;
    final int priority;

    /**
     * 构造方法
     *
     * @param context  事件调度中心
     * @param maxSize  对象池大小，-1表示不进行池化
     * @param cls      池化的类
     * @param priority 执行优先级，数字小的先执行
     */
    CommandPool(Facade context, int maxSize, Class<? extends Command> cls, int priority) {
        super(maxSize);
        this.context = context;
        this.cls = cls;
        this.priority = priority;
    }

    CommandPool(Class<? extends Command> cls) {
        this(null, 0, cls, 0);
    }

    @Override
    protected Command newObject() {
        return (Command) this.context.createObject(cls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandPool that = (CommandPool) o;
        return cls.equals(that.cls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cls);
    }

}
