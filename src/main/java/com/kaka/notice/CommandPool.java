package com.kaka.notice;

import com.kaka.util.MethodAccessor;
import com.kaka.util.ObjectPool;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@link Command}对象池
 * <br> 此类在类包外不可访问
 *
 * @author zkpursuit
 */
class CommandPool extends ObjectPool<Command> {
    final Supplier<Command> constFunc;
    final Class<? extends Command> cls;
    final int priority;


    /**
     * 构造方法
     *
     * @param cls      池化的类
     * @param maxSize  对象池大小，-1表示不进行池化
     * @param priority 执行优先级，数字小的先执行
     */
    CommandPool(Class<? extends Command> cls, int maxSize, int priority) {
        super(maxSize);
        this.cls = cls;
        this.priority = priority;
        this.constFunc = MethodAccessor.constructSupplier(cls);
    }

    CommandPool(Class<? extends Command> cls) {
        this(cls, 0, 0);
    }

    @Override
    protected Command newObject() {
        return this.constFunc.get();
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
