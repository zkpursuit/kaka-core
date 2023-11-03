package com.kaka.numerical.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数值配置表解析器注解
 *
 * @author zkpursuit
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Numeric {

    /**
     * 配置文件名或路径
     *
     * @return 数值配置表标识
     */
    String src();

    /**
     * 是否忽略src大小写 <br>
     * 当此值为true时，则src与src的小写都将映射为同一个{@link com.kaka.numerical.NumericConfig}
     *
     * @return 忽略src大小写，默认忽略
     */
    boolean ignoreCase() default true;

    /**
     * 优先级，数字越大，优先级越高
     *
     * @return 优先级
     */
    int priority() default 0;

    /**
     * 被注册到的目标{@link com.kaka.notice.Facade}唯一名
     *
     * @return {@link com.kaka.notice.Facade}唯一名
     */
    String context() default "";

}
