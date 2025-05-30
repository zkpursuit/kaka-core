package com.kaka.numerical;

import com.kaka.numerical.NumericField.BiConverter;
import com.kaka.numerical.NumericField.Converter;
import com.kaka.util.ArrayUtils;
import com.kaka.util.MethodAccessor;
import com.kaka.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import static com.kaka.util.ReflectUtils.getFieldValue;
import static com.kaka.util.ReflectUtils.setFieldValue;

/**
 * 配置文件解析器
 *
 * @author zkpursuit
 */
abstract public class Parser {

    /**
     * 为对象的字段赋值
     *
     * @param <T>      对象限定类型
     * @param object   对象
     * @param field    对象字段
     * @param analyzer 赋值分析器
     * @throws Exception 解析异常
     */
    protected <T> void doParse(T object, Field field, IAnalyzer analyzer) throws Exception {
        NumericField att = field.getAnnotation(NumericField.class);
        if (att == null) {
            String value = analyzer.getContent(field.getName());
            try {
                setFieldValue(object, field, value);
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
            return;
        }
        String[] elements = att.elements();
        Class<? extends FieldConverter> converterClass = att.converter();
        FieldConverter converter = converterClass == FieldConverter.class ? null : MethodAccessor.newInstance(converterClass);
        if (converter == null) {
            String value = analyzer.getContent(elements[0].trim().replaceAll(" ", ""));
            setFieldValue(object, field, value);
        } else if (converter instanceof BiConverter<?> biConverter) {
            Object fieldValue = getFieldValue(object, field);
            Class<?> fieldClass = field.getType();
            for (int i = 0; i < elements.length; i++) {
                String element = elements[i].trim();
                String value = analyzer.getContent(element);
                Object resultValue = biConverter.convert(element, value, i, elements.length, object, field);
                if (fieldValue == null && resultValue != null && (resultValue.getClass() == fieldClass || fieldClass.isAssignableFrom(resultValue.getClass()))) {
                    setFieldValue(object, field, resultValue);
                    fieldValue = resultValue;
                }
            }
        } else {
            Converter<?> siConverter = (Converter<?>) converter;
            int fieldType = 0; //字段类型，1为列表，2为map
            Class<?> filedTypeClass = field.getType();
            Object fieldValue = getFieldValue(object, field);
            if (Collection.class.isAssignableFrom(filedTypeClass)) {
                fieldType = 1;
                if (fieldValue == null) {
                    if (filedTypeClass.isInterface() || Modifier.isAbstract(filedTypeClass.getModifiers())) {
                        if (java.util.SortedSet.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.TreeSet<>();
                        } else if (java.util.LinkedHashSet.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.LinkedHashSet<>();
                        } else if (java.util.Set.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.HashSet<>();
                        } else if (java.util.Stack.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.Stack<>();
                        } else if (java.util.LinkedList.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.LinkedList<>();
                        } else if (java.util.Queue.class.isAssignableFrom(filedTypeClass)) {
                            fieldValue = new java.util.LinkedList<>();
                        } else {
                            fieldValue = new java.util.ArrayList<>();
                        }
                    } else {
                        fieldValue = MethodAccessor.newInstance(filedTypeClass);
                    }
                    setFieldValue(object, field, fieldValue);
                }
            } else if (Map.class.isAssignableFrom(filedTypeClass)) {
                fieldType = 2;
            }
            for (String element : elements) {
                element = element.trim();
                String value = analyzer.getContent(element);
                if (fieldType == 1) {
                    Object resultValue = siConverter.convert(value);
                    if (resultValue != null && fieldValue != null) {
                        Collection<Object> collection = (Collection<Object>) fieldValue;
                        if (resultValue.getClass().isArray()) {
                            int len = ArrayUtils.getLength(resultValue);
                            for (int i = 0; i < len; i++) {
                                Object arrVal = ArrayUtils.get(resultValue, i);
                                if (arrVal != null) {
                                    collection.add(arrVal);
                                }
                            }
                        } else {
                            collection.add(resultValue);
                        }
                    }
                } else if (fieldType == 2) {
                    Object resultValue = siConverter.convert(value);
                    if (resultValue != null) {
                        if (fieldValue == null) {
                            setFieldValue(object, field, resultValue);
                            fieldValue = resultValue;
                        } else {
                            Map map = (Map) fieldValue;
                            map.putAll((Map) resultValue);
                        }
                    }
                } else {
                    Object resultValue = siConverter.convert(value);
                    if (resultValue != null) {
                        setFieldValue(object, field, resultValue);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 将文本数据解析为对象<br>
     * 子类中必须调用此方法将文本反序列化为对象<br>
     *
     * @param <T>       JavaBean对象类型
     * @param infoClass 目标对象
     * @param analyzer  字段内容分析处理器
     * @return 序列化后的JavaBean对象
     * @throws Exception 解析异常
     */
    protected <T> T doParse(Class<T> infoClass, IAnalyzer analyzer) throws Exception {
        T object = MethodAccessor.newInstance(infoClass);
        Field[] fields = ReflectUtils.getDeclaredFields(infoClass);
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) && Modifier.isFinal(modifier)) {
                continue;
            }
            doParse(object, field, analyzer);
        }
        return object;
    }

}
