package com.sp.infra.comp.redis.lock;

import java.io.Closeable;

/**
 * 分布式锁
 *
 * @author Wang Chong
 */
public interface Lock extends Closeable {
    /**
     * 获取锁，如果成功，加锁
     *
     * @return 是否成功拿到锁
     * @throws InterruptedException 如果获取过程中被中断，抛出此异常
     */
    boolean acquire() throws InterruptedException;
}
