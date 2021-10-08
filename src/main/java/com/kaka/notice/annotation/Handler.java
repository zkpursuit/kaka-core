package com.kaka.notice.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 核心事件通知框架中的控制器（业务处理器）{@link com.kaka.notice.Command}标注类型
 *
 * @author zkpursuit
 */
@Documented
@Repeatable(Handlers.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {

    /**
     * 命令号，异步事件时相同命令号下的{@link com.kaka.notice.Command}亦将根据priority优先级被同步循序执行
     *
     * @return 命令号
     */
    String cmd();

    /**
     * 命令号类型
     *
     * @return 命令号类型
     */
    Class type() default Integer.class;

    /**
     * 池化大小
     *
     * @return {@link com.kaka.notice.Command}对象池数量
     */
    int pooledSize() default -1;

    /**
     * 相同命令号下{@link com.kaka.notice.Command}执行优先级，数字越小，越是最早执行
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
