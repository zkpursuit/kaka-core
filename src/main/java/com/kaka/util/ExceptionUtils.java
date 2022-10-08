package com.kaka.util;

import java.util.concurrent.TimeUnit;

/**
 * 异常工具类
 *
 * @author zkpursuit
 */
public final class ExceptionUtils {

    /**
     * 处理线程中断异常
     *
     * @param sleepInterval 当前线程休眠间隔时间，毫秒
     */
    public static void processInterruptedException(long sleepInterval) {
        Thread thread = Thread.currentThread();
        while (!thread.isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepInterval);
            } catch (InterruptedException ex) {
                thread.interrupt();
            }
        }
    }

    public static void processInterruptedException() {
        Thread thread = Thread.currentThread();
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    private ExceptionUtils() {
    }

}
