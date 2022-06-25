package com.sp.infra.comp.redis.lock.impl;

import com.sp.infra.comp.redis.lock.Lock;
import com.sp.infra.comp.redis.lock.LockFactory;
import com.sp.infra.comp.redis.lock.model.LockRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

import static com.sp.infra.comp.redis.utils.SplitJoinUtil.resolve;

/**
 * 默认锁工厂实现
 * 使用方法：
 * <p>
 *      <code>
 *          try (Lock lock = this.lockFactory.getLock(LOCK_KEY)) {
 *             if (!lock.acquire()) {
 *                 throw new LockFailException(LOCK_KEY);
 *             }
 *             //自己的业务逻辑
 *      }
 *      </code>
 * </p>
 * @author wang chong
 */
public class DefaultLockFactoryImpl implements LockFactory {
    private final RedisConnectionFactory redisConnectionFactory;

    public DefaultLockFactoryImpl(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @NotNull
    @Override
    public Lock getLock(@NotNull String lockKey) {
        return getLock(lockKey, null, null, null);
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁的key
     * @return 锁的实例
     */
    @NotNull
    @Override
    public Lock getLock(@NotNull String lockKey, Long timeout, Long expire, TimeUnit timeUnit) {
        return new RedisLockImpl(new LockRequestBuilder()
                .withLockKey(lockKey)
                .onConnection(redisConnectionFactory)
                .timeout(resolve(() -> (int) (timeUnit.toMillis(timeout))).orElse(RedisLockImpl.DEFAULT_TIMEOUT_MILLISECONDS))
                .expire(resolve(() -> (int) (timeUnit.toMillis(expire))).orElse(RedisLockImpl.DEFAULT_EXPIRED_MILLISECONDS))
                .build());
    }
}
