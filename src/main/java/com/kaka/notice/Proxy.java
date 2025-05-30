package com.kaka.notice;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据处理代理，一般与注解Model结合使用
 *
 * @author zkpursuit
 */
abstract public class Proxy extends Notifier {

    String name;
    public int priority;

    /**
     * 别名
     */
    private Set<String> aliasSet = null;

    public String getName() {
        return name;
    }

    /**
     * 设置优先级
     *
     * @param priority 优先级数值，数值越大表示优先级越高
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 获取优先级，值越大表示越先被注册，此值默认依赖自动扫描注解中设定的优先级 <br>
     *
     * @return 优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 添加别名
     *
     * @param alias 别名
     */
    final void addAlias(String alias) {
        if (aliasSet == null) {
            //存储自动添加的别名，一般不会超出2个
            aliasSet = new HashSet<>(8);
        }
        aliasSet.add(alias);
    }

    /**
     * 获取别名列表，通过此列表中的别名都能获得此对象
     *
     * @return 别名列表
     */
    public String[] getAliases() {
        if (aliasSet == null) {
            return new String[0];
        }
        String[] arr = new String[aliasSet.size()];
        aliasSet.toArray(arr);
        return arr;
    }

    /**
     * 构造方法
     */
    public Proxy() {
        this.name = getClass().getTypeName();
    }

    /**
     * 构造方法
     *
     * @param name 全局唯一实例名
     */
    public Proxy(String name) {
        this.name = name;
    }

    /**
     * 被注册到中央调度器时的触发函�?
     */
    protected void onRegister() {

    }

    /**
     * 被从中央调度器中移除时的触发函数
     */
    protected void onRemove() {

    }

}
