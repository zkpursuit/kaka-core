package com.kaka.notice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事件调度器
 *
 * @author zkpursuit
 */
public class Scheduler implements Runnable {

    Message msg;
    Facade facade;
    long startTime = -1;
    long endTime;
    long interval;
    int repeatCount;
    AtomicInteger count;
    AtomicLong prevExecTime; //执行次数尽可能的不受执行耗时影响
    String name;

    /**
     * 私有构造方法，外部不允许实例化
     */
    private Scheduler() {
        this.count = new AtomicInteger(0);
        this.prevExecTime = new AtomicLong(0);
        this.repeatCount = 1;
        this.name = "";
        this.endTime = Long.MAX_VALUE;
    }

    /**
     * 创建调度器
     *
     * @param group 定时调度任务所在组名，用于后续区分调度任务的唯一性
     * @return 调度器
     */
    public static Scheduler create(String group) {
        Scheduler scheduler = new Scheduler();
        scheduler.name = group;
        return scheduler;
    }

    /**
     * 调度器开始执行时间点
     *
     * @param startTime 以毫秒为单位的时间点
     * @return 调度器
     */
    public Scheduler startTime(long startTime) {
        this.startTime = startTime <= System.currentTimeMillis() ? -1 : startTime;
        return this;
    }

    /**
     * 调度器最终有效的时间点，此时间点不受执行耗时影响
     *
     * @param endTime 以毫秒为单位的时间点
     * @return 调度器
     */
    public Scheduler endTime(long endTime) {
        this.endTime = Math.max(endTime, this.startTime);
        return this;
    }

    /**
     * 设置调度器最大执行次数
     *
     * @param repeat 最大执行次数
     * @return 调度器
     */
    public Scheduler repeat(int repeat) {
        this.repeatCount = repeat <= 0 ? 1 : repeat;
        return this;
    }

    /**
     * 调度器执行间隔时间
     *
     * @param interval 执行间隔
     * @param unit     时间单位
     * @return 调度器
     */
    public Scheduler interval(long interval, TimeUnit unit) {
        this.interval = interval <= 0 ? 0 : unit.convert(interval, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 调度器正式执行
     */
    @Override
    final public void run() {
        if (this.startTime > this.endTime) {
            facade.cancelSchedule(name);
            return;
        }
        long last = this.prevExecTime.addAndGet(this.interval);
        try {
            facade.sendMessage(msg);
        } catch (Exception ex) {
            msg.reset();
            facade.cancelSchedule(name);
        }
        int c = this.count.addAndGet(1);
        if (c >= this.repeatCount) {
            msg.reset();
            facade.cancelSchedule(name);
        } else {
            if (last > this.endTime) {
                msg.reset();
                facade.cancelSchedule(name);
            }
        }
    }

//    @Override
//    public void reset() {
//        this.facade = null;
//        this.msg = null;
//        this.count.set(0);
//        this.prevExecTime.set(0);
//        this.repeatCount = 1;
//        this.name = "";
//        this.endTime = Long.MAX_VALUE;
//        this.interval = 0;
//    }

}
