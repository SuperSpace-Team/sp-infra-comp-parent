package com.sp.infra.comp.consul.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description
 * @Author alexlu
 * @date 2021.08.13
 */
public class DefaultThreadFactory implements ThreadFactory {

    private AtomicLong threadNumber = new AtomicLong();
    private int priority;
    private String threadPrefixName;

    public DefaultThreadFactory(String threadPrefixName) {
        this.threadPrefixName = threadPrefixName;
        this.priority = Thread.NORM_PRIORITY;
    }

    public DefaultThreadFactory(int priority, String threadPrefixName) {
        this.priority = priority;
        this.threadPrefixName = threadPrefixName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, threadPrefixName + "-" + threadNumber.getAndIncrement());
        t.setDaemon(true);
        if (t.getPriority() != priority) t.setPriority(priority);
        return t;
    }
}
