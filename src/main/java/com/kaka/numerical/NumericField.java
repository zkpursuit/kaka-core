package com.kaka.numerical;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * 数值配置文件序列化的对象中字段注解，标示字段的值需特殊处理后获得<br>
 *
 * @author zkpursuit
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumericField {

    /**
     * 待处理的节点元素
     *
     * @return 节点元素集合
     */
    String[] elements();

    /**
     * elements中单个节点处理器
     *
     * @return 处理器类
     */
    Class<? extends FieldConverter> converter() default FieldConverter.class;

    /**
     * 每个字段的处理器
     *
     * @param <T> 处理后的数据，如为数组，则其中的数据将被逐个添加到集合对象中，非数组则整个添加到集合对象中
     */
    interface Converter<T> extends FieldConverter {
        /**
         * 将字符串数据转换为字段定义类型
         *
         * @param value 数值表字段值
         * @return 数值表所映射的JavaBean对象字段定义的类型数据
         */
        T transform(String value);
    }

    /**
     * 每个字段的处理器
     *
     * @param <T> 处理后的数据，如为数组，则其中的数据将被逐个添加到集合对象中，非数组则整个添加到集合对象中
     */
    interface BiConverter<T> extends FieldConverter {
        /**
         * 将字符串数据转换为字段定义类型 <br>
         * 需要主动使用反射功能赋值，此方法可用于将多个数值表字段合并为一个对象
         *
         * @param title         数值表字段名，为{@link NumericField}.elements中的值
         * @param value         数值表字段值
         * @param elementIndex  当前元素索引，其中的“元素”为：{@link NumericField}.elements
         * @param elementTotals 总元素数量，其中的“元素”为：{@link NumericField}.elements
         * @param pojo          数值表映射的JavaBean对象
         * @param pojoField     JavaBean对象中的字段
         */
        T transform(String title, String value, int elementIndex, int elementTotals, Object pojo, Field pojoField);
    }

}
