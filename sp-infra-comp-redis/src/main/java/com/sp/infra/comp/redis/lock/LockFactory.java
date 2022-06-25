package com.sp.infra.comp.redis.lock;


import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的工厂，生成新的锁
 *
 * @author wang chong
 */
public interface LockFactory {
    Lock getLock(String lockKey);

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁的key
     * @param timeout 获取锁超时时间
     * @param expire 锁过期时间(非ttl, 存在value中)
     * @return 锁的实例
     */
    Lock getLock(String lockKey, Long timeout, Long expire, TimeUnit timeUnit);
}
