package com.kaka.numerical;

import com.kaka.numerical.annotation.NumericField;
import com.kaka.numerical.annotation.NumericField.Converter;
import com.kaka.util.ArrayUtils;
import com.kaka.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.kaka.util.ReflectUtils.getFieldValue;
import static com.kaka.util.ReflectUtils.setFieldValue;

/**
 * 配置文件解析器
 *
 * @author zhoukai
 */
abstract public class Parser {

    /**
     * 日志记录
     */
    private static final Logger logger = Logger.getLogger(Parser.class.getTypeName());

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
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
            return;
        }
        String[] eles = att.elements();
        Class<? extends Converter> converterClass = att.converter();
        Converter<?> processor;
        if (converterClass == Converter.class) {
            processor = null;
        } else {
            processor = ReflectUtils.newInstance(converterClass);
        }
        boolean isCollectionField = false;
        Class<?> filedTypeClass = field.getType();
        Object fieldValue = getFieldValue(object, field);
        if (Collection.class.isAssignableFrom(filedTypeClass)) {
            isCollectionField = true;
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
                    Constructor<?>[] constructors = filedTypeClass.getConstructors();
                    for (Constructor<?> constructor : constructors) {
                        int modifier = constructor.getModifiers();
                        if (!Modifier.isPublic(modifier)) {
                            continue;
                        }
                        int paramCount = constructor.getParameterCount();
                        if (paramCount > 0) {
                            continue;
                        }
                        fieldValue = constructor.newInstance();
                        break;
                    }
                }
                setFieldValue(object, field, fieldValue);
            }
        }
        for (String confColName : eles) {
            confColName = confColName.trim().replaceAll(" ", "");
            String value = analyzer.getContent(confColName);
            if (isCollectionField) {
                Object resultValue = processor != null ? processor.transform(value) : null;
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
            } else {
                if (processor == null) {
                    setFieldValue(object, field, value);
                } else {
                    Object resultValue = processor.transform(value);
                    if (resultValue != null) {
                        setFieldValue(object, field, resultValue);
                    }
                }
                break;
            }
        }
    }

    /**
     * 将文本数据解析为对象<br>
     * 子类中必须调用此方法将文本反序列化为对象<br>
     *
     * @param <T>       JavaBean对象类型
     * @param InfoClass 目标对象
     * @param analyzer  字段内容分析处理器
     * @return 序列化后的JavaBean对象
     * @throws Exception 解析异常
     */
    protected <T> T doParse(Class<T> InfoClass, IAnalyzer analyzer) throws Exception {
        T object = InfoClass.getDeclaredConstructor().newInstance();
        Field[] fields = ReflectUtils.getDeclaredFields(InfoClass);
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
