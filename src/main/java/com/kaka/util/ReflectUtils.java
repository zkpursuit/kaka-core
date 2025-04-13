package com.kaka.util;

import java.lang.reflect.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 反射工具
 *
 * @author zkpursuit
 */
public final class ReflectUtils {

    /**
     * 匹配get或set方法名的正则表达式
     */
    private static final String REGEX = "[a-zA-Z]";

    /**
     * 设置对象字段值
     *
     * @param obj   对象
     * @param field 字段
     * @param value 新的字段值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        if (obj == null) {
            return;
        }
        if (field == null) {
            return;
        }
        try {
            int modif = field.getModifiers();
            if (!Modifier.isPublic(modif) || Modifier.isFinal(modif)) {
                field.setAccessible(true);
            }
            Class<?> fieldCls = field.getType();
            Object newValue = TypeUtils.cast(value, fieldCls);
            if (fieldCls.isPrimitive() && newValue == null) {
                return;
            }
            field.set(obj, newValue);
        } catch (IllegalAccessException ex) {
            setFieldValueByMethod(obj, field.getName(), value);
        }
    }

    /**
     * 设置对象字段值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @param value     新的字段值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null) {
            return;
        }
        Class<?> objClass = obj.getClass();
        Field attributeField;
        try {
            attributeField = ReflectUtils.getDeclaredField(objClass, fieldName);
            ReflectUtils.setFieldValue(obj, attributeField, value);
        } catch (SecurityException ex) {
            setFieldValueByMethod(obj, fieldName, value);
        }
    }

    /**
     * 通过set方法设置字段值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @param value     新的字段值
     */
    public static void setFieldValueByMethod(Object obj, String fieldName, Object value) {
        if (obj == null) {
            return;
        }
        if (fieldName == null) {
            return;
        }
        fieldName = fieldName.trim();
        if (fieldName.isEmpty()) {
            return;
        }
        String method_name = convertToMethodName(fieldName, obj.getClass(), true);
        Method[] methods = obj.getClass().getMethods();
        try {
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(method_name)) {
                    Class<?>[] paramClasses = method.getParameterTypes();
                    if (paramClasses.length == 1) {
                        Class<?> paramCls = paramClasses[0];
                        Object newValue = TypeUtils.cast(value, paramCls);
                        if (paramCls.isPrimitive() && newValue == null) {
                            return;
                        }
                        int modif = method.getModifiers();
                        if (!Modifier.isPublic(modif)) {
                            method.setAccessible(true);
                        }
                        method.invoke(obj, newValue);
                        return;
                    }
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 通过字段名获取set或get方法名
     *
     * @param attribute 字段名
     * @param objClass  对象类型
     * @param isSet     是否为set方法
     * @return set或get方法名
     */
    public static String convertToMethodName(String attribute, Class<?> objClass, boolean isSet) {
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(attribute);
        StringBuilder sb = new StringBuilder();
        if (isSet) {
            sb.append("set");
        } else {
            try {
                Field attributeField = objClass.getDeclaredField(attribute);
                if (attributeField.getType() == boolean.class || attributeField.getType() == Boolean.class) {
                    sb.append("is");
                } else {
                    sb.append("get");
                }
            } catch (NoSuchFieldException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (attribute.charAt(0) != '_' && m.find()) {
            sb.append(m.replaceFirst(m.group().toUpperCase()));
        } else {
            sb.append(attribute);
        }
        return sb.toString();
    }

    /**
     * 获取字段值
     *
     * @param object 对象
     * @param field  字段
     * @return 字段值
     */
    public static Object getFieldValue(Object object, Field field) {
        if (object == null) {
            return null;
        }
        if (field == null) {
            return null;
        }
        int modif = field.getModifiers();
        if (!Modifier.isPublic(modif) || Modifier.isFinal(modif)) {
            field.setAccessible(true);
        }
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 获取字段值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        if (fieldName == null) {
            return null;
        }
        Object value = null;
        Field field = ReflectUtils.getDeclaredField(obj.getClass(), fieldName);
        if (field != null) {
            int modif = field.getModifiers();
            if (!Modifier.isPublic(modif) || Modifier.isFinal(modif)) {
                field.setAccessible(true);
            }
            try {
                value = field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                String methodName = convertToMethodName(fieldName, obj.getClass(), false);
                Method method = ReflectUtils.getDeclaredMethod(obj.getClass(), methodName);
                if (method != null) {
                    int modif = method.getModifiers();
                    if (!Modifier.isPublic(modif)) {
                        method.setAccessible(true);
                    }
                    value = method.invoke(obj);
                }
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        return value;
    }

    /**
     * 通过方法名设置值
     *
     * @param obj        类实例
     * @param methodName 方法名
     * @param args       方法参数
     */
    public static void setValueByMethod(Object obj, String methodName, Object... args) {
        try {
            Method method;
            if (args.length == 0) {
                method = ReflectUtils.getDeclaredMethod(obj.getClass(), methodName);
            } else {
                Class<?>[] parameterTypes = new Class[args.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                }
                method = ReflectUtils.getDeclaredMethod(obj.getClass(), methodName, parameterTypes);
            }
            if (method != null) {
                int modif = method.getModifiers();
                if (!Modifier.isPublic(modif)) {
                    method.setAccessible(true);
                }
                method.invoke(obj, args);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 通过方法名获得值，保证方法不带参数
     *
     * @param obj        类实例
     * @param methodName 方法名
     * @return 方法返回值
     */
    public static Object getValueByMethod(Object obj, String methodName) {
        try {
            Method method = ReflectUtils.getDeclaredMethod(obj.getClass(), methodName);
            if (method != null) {
                int modif = method.getModifiers();
                if (!Modifier.isPublic(modif)) {
                    method.setAccessible(true);
                }
                return method.invoke(obj);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    /**
     * 循环向上转型, 获 * @param object : 子类对象
     *
     * @param srcClass       类
     * @param methodName     : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Class<?> srcClass, String methodName, Class<?>... parameterTypes) {
        for (Class<?> clazz = srcClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException | SecurityException e) {
                //错误后直接循环，从父类中获取
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获 * @param object : 子类对象
     *
     * @param srcClass  类
     * @param fieldName : 父类中 * @return 父类中
     * @return 父类中的属性
     */
    public static Field getDeclaredField(Class<?> srcClass, String fieldName) {
        Class<?> clazz = srcClass;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException e) {
                //错误后直接循环，从父类中获取
            }
        }
        return null;
    }

    /**
     * 获取已声明的属性
     *
     * @param beanClass 类对象
     * @param ancestor  是否向父级追溯
     * @return 已声明的方法集合
     */
    public static Field[] getDeclaredFields(Class<?> beanClass, boolean ancestor) {
        Field[] fields = null;
        Class<?> clazz = beanClass;
        do {
            Field[] fs = clazz.getDeclaredFields();
            if (fields == null) {
                fields = fs;
            } else {
                Field[] newFs = new Field[fields.length + fs.length];
                System.arraycopy(fields, 0, newFs, 0, fields.length);
                System.arraycopy(fs, 0, newFs, fields.length, fs.length);
                fields = newFs;
            }
            if (!ancestor) {
                break;
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return fields;
    }

    /**
     * 获取已声明的属性，包括父级以及其上
     *
     * @param beanClass 类对象
     * @return 已声明的方法集合
     */
    public static Field[] getDeclaredFields(Class<?> beanClass) {
        return getDeclaredFields(beanClass, true);
    }

    /**
     * 获取已声明的方法，包括父级以及其上
     *
     * @param clasz    类对象
     * @param ancestor 是否向父级追溯
     * @return 已声明的方法集合
     */
    public static Method[] getDeclaredMethods(Class<?> clasz, boolean ancestor) {
        Method[] methods = null;
        Class<?> clazz = clasz;
        do {
            Method[] ms = clazz.getDeclaredMethods();
            if (methods == null) {
                methods = ms;
            } else {
                Method[] newms = new Method[methods.length + ms.length];
                System.arraycopy(methods, 0, newms, 0, methods.length);
                System.arraycopy(ms, 0, newms, methods.length, ms.length);
                methods = newms;
            }
            if (!ancestor) {
                break;
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);
        return methods;
    }

    /**
     * 获取已声明的方法，包括父级以及其上
     *
     * @param clasz 类对象
     * @return 已声明的方法集合
     */
    public static Method[] getDeclaredMethods(Class<?> clasz) {
        return getDeclaredMethods(clasz, true);
    }

    /**
     * 遍历class的所有父class，包括初始class本身
     *
     * @param clasz    初始class
     * @param consumer 父class访问器
     */
    public static void traversalSuperClasses(Class<?> clasz, Consumer<Class<?>> consumer) {
        while (clasz != null && clasz != Object.class) {
            consumer.accept(clasz);
            clasz = clasz.getSuperclass();
        }
    }

    /**
     * 遍历class的所有接口class
     *
     * @param clasz    初始class
     * @param consumer 接口class访问器
     */
    public static void traversalInterfaceClasses(Class<?> clasz, Consumer<Class<?>> consumer) {
        traversalSuperClasses(clasz, superClass -> {
            Class<?>[] interfaces = superClass.getInterfaces();
            for (Class<?> cls : interfaces) {
                consumer.accept(cls);
            }
        });
    }

    /**
     * 获取泛型参数类型 <br>
     * 此方法仅对编码时显示写入泛型有效，动态创建对象时无法获得泛型 <br>
     *
     * @param cls 带泛型的类
     * @return 泛型参数类型，找不到泛型类时默认使用Object.class
     */
    public static Class<?> getGenericParadigmClass(Class<?> cls) {
        return getGenericParadigmClass(cls, Object.class);
    }

    /**
     * 获取泛型参数类型 <br>
     * 此方法仅对编码时显示写入泛型有效，动态创建对象时无法获得泛型 <br>
     *
     * @param cls          带泛型的类
     * @param defaultClass 如果从带泛型的类这个参数中未找到泛型类，将指定此类
     * @return 泛型类
     */
    public static Class<?> getGenericParadigmClass(Class<?> cls, Class<?> defaultClass) {
        return getGenericParadigmClass(cls, 0, defaultClass);
    }

    /**
     * 获取泛型参数类型 <br>
     * 此方法仅对编码时显示写入泛型有效，动态创建对象时无法获得泛型 <br>
     *
     * @param cls          带泛型的类
     * @param genericIndex 泛型约束索引，表示第几个泛型
     * @param defaultClass 如果从带泛型的类这个参数中未找到泛型类，将指定此类
     * @return 泛型类
     */
    public static Class<?> getGenericParadigmClass(Class<?> cls, int genericIndex, Class<?> defaultClass) {
        do {
            Type genType = cls.getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                return (Class<?>) params[genericIndex];
            }
            cls = (Class<?>) genType;
        } while (cls != Object.class);
        return defaultClass;
    }

    /**
     * 实例化对象
     *
     * @param cls  对象类型
     * @param args 构造方法参数
     * @return 对象实例
     */
    public static <T> T newInstance(Class<T> cls, Object... args) {
        Constructor<T> con;
        if (args.length == 0) {
            try {
                con = cls.getDeclaredConstructor();
            } catch (NoSuchMethodException | SecurityException e) {
                try {
                    return cls.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new RuntimeException("无法找到无参构造方法", e);
                }
            }
        } else {
            Class<?>[] argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    argTypes[i] = args[i].getClass();
                }
            }
            try {
                con = cls.getDeclaredConstructor(argTypes);
            } catch (NoSuchMethodException | SecurityException | IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            }
        }
        int modifier = con.getModifiers();
        if (!Modifier.isPublic(modifier)) {
            con.setAccessible(true);
        }
        try {
            return con.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 获取参数对象的Class
     *
     * @param args 参数列表
     * @return 参数元素对应的Class列表
     */
    public static Class<?>[] toClass(Object... args) {
        if (args == null) {
            return null;
        }
        if (args.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            classes[i] = args[i] == null ? null : args[i].getClass();
        }
        return classes;
    }

}
