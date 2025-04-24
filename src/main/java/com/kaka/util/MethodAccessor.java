package com.kaka.util;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class MethodAccessor {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodType methodType0 = MethodType.methodType(void.class);
    private static final String methodName0 = "get";
    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    /**
     * 动态通过LambdaMetafactory寻找无参构造函数
     *
     * @param <T> 类型限定
     * @return 构造方法Lambda函数
     */
    public static <T> Supplier<T> constructSupplier(Class<? extends T> cls) {
        try {
            MethodHandle methodHandle = lookup.findConstructor(cls, methodType0);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    methodName0,
                    MethodType.methodType(Supplier.class),
                    methodHandle.type().generic(),
                    methodHandle,
                    methodHandle.type());
            return (Supplier<T>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * 通过LambdaMetafactory方式获取实例，失败后用反射获取实例 <br>
     * 仅限无参构造方法
     *
     * @param cls 类型
     * @param <T> 类型限定
     * @return 类型实例
     */
    public static <T> T newInstance(Class<?> cls) {
        Object v = cache.get(cls);
        if (v != null) {
            if (v instanceof Supplier<?> supplier) {
                return (T) supplier.get();
            }
            Constructor<T> constructor = (Constructor<T>) v;
            try {
                return constructor.newInstance();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        } else {
            try {
                Supplier<?> supplier = constructSupplier(cls);
                cache.put(cls, supplier);
                return (T) supplier.get();
            } catch (Throwable throwable) {
                try {
                    Constructor<?> constructor = ReflectUtils.getConstructor0(cls);
                    cache.put(cls, constructor);
                    return (T) constructor.newInstance();
                } catch (Throwable throwable1) {
                    throw new RuntimeException(throwable1);
                }
            }
        }
    }

}
