package com.kaka.numerical;

import com.kaka.notice.Notifier;
import com.kaka.notice.Proxy;
import com.kaka.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * 数值表配置
 *
 * @param <T> 解析成为的目标类型
 * @author zkpursuit
 */
abstract public class NumericConfig<T> extends Proxy {

    private final static AtomicInteger counter = new AtomicInteger(0);
    private int index;

    @Override
    protected void onRegister() {
        super.onRegister();
        index = counter.addAndGet(1);
    }

    /**
     * 获取被注册时的顺序索引，亦可视作另一种优先级，值越小表示越先被注册 <br>
     * 此值在手动或自动扫描注解注册时都有效
     *
     * @return 被注册时的顺序索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 将文件数据转换为JavaBean对象
     *
     * @param filePath 文件路径
     * @throws Exception 解析异常
     */
    abstract public void parse(String filePath) throws Exception;

    /**
     * 缓存对象
     *
     * @param info 解析序列化后的对象
     */
    abstract protected void cacheObject(T info);

    /**
     * 解析文件之前可调用 <br>
     *
     * @param fieldDeclaringClassFun 字段声明类型处理，返回false时表示不处理Field后续逻辑，可为null
     * @param fieldResetValueFun     字段值重置函数，可为null
     */
    protected void parseBefore0(BiFunction<Field, Class<?>, Boolean> fieldDeclaringClassFun, BiConsumer<Field, Object> fieldResetValueFun) {
        Field[] fields = ReflectUtils.getDeclaredFields(this.getClass());
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            Class<?> dc = f.getDeclaringClass();
            if (dc == Notifier.class) continue;
            if (dc == Proxy.class) continue;
            if (dc == NumericConfig.class) continue;
            if (fieldDeclaringClassFun != null && !fieldDeclaringClassFun.apply(f, dc)) continue;
            Object v = ReflectUtils.getFieldValue(this, f);
            if (v == null) continue;
            if (v instanceof Collection) {
                ((Collection<?>) v).clear();
            } else if (v instanceof Map) {
                ((Map<?, ?>) v).clear();
            } else if (v instanceof AtomicReference) {
                ((AtomicReference<?>) v).set(null);
            } else if (v instanceof AtomicInteger ai) {
                ai.set(0);
            } else if (v instanceof AtomicLong al) {
                al.set(0);
            } else if (v instanceof AtomicBoolean ab) {
                ab.set(false);
            } else {
                if (v.getClass().isArray()) {
                    if (v instanceof boolean[] bs) {
                        Arrays.fill(bs, false);
                    } else if (v instanceof short[] ss) {
                        Arrays.fill(ss, (short) 0);
                    } else if (v instanceof int[] is) {
                        Arrays.fill(is, 0);
                    } else if (v instanceof long[] ls) {
                        Arrays.fill(ls, 0);
                    } else if (v instanceof float[] fs) {
                        Arrays.fill(fs, 0);
                    } else if (v instanceof double[] ds) {
                        Arrays.fill(ds, 0);
                    } else if (v instanceof Object[] os) {
                        Arrays.fill(os, null);
                    }
                }
                if (fieldResetValueFun != null) {
                    fieldResetValueFun.accept(f, v);
                }
            }
        }
    }

    /**
     * 解析文件之前
     */
    abstract protected void parseBefore();

    /**
     * 文件解析完成后
     */
    abstract protected void parseAfter();

    /**
     * 获取配置映射的类
     *
     * @return 配置映射的类
     */
    final public Class<T> getMappingClass() {
        return (Class<T>) ReflectUtils.getGenericParadigmClass(this.getClass());
    }

}
