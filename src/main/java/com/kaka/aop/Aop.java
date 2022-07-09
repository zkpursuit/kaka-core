package com.kaka.aop;

abstract public class Aop {

    /**
     * 注册切面类
     *
     * @param aspectClass 切面类
     */
    abstract public void registerAspect(Class<?> aspectClass);

    /**
     * 注册被拦截的对象，此对象中的某些方法包含拦截器注解
     *
     * @param targetClass 拦截器类
     */
    abstract public void registerInterceptTarget(Class<?> targetClass);

    /**
     * 被代理对象是否已准备被代理处置
     *
     * @param clasz 被代理对象类
     * @return true表示准备好被代理
     */
    abstract public boolean isPrepared(Class<?> clasz);

    /**
     * 创建被切面代理后的对象
     *
     * @param clasz 类
     * @param <T>   类的泛型限定
     * @return 类的实例化对象
     */
    abstract public <T> T createInstance(Class<? extends T> clasz);

}
