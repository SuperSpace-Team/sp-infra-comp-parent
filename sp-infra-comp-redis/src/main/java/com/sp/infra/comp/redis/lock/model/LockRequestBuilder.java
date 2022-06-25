package com.sp.infra.comp.redis.lock.model;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 锁请求Builder
 * @author  Wang Chong
 */
public class LockRequestBuilder {
    RedisConnectionFactory redisConnectionFactory;
    String lockKey;
    Integer timeoutMilliseconds;
    Integer expiredMilliseconds;

    public LockRequestBuilder onConnection(@NotNull RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
        return this;
    }

    public LockRequestBuilder withLockKey(@NotNull String lockKey) {
        this.lockKey = lockKey;
        return this;
    }

    public LockRequestBuilder timeout(@NotNull Integer timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
        return this;
    }

    public LockRequestBuilder expire(@NotNull Integer expiredMilliseconds) {
        this.expiredMilliseconds = expiredMilliseconds;
        return this;
    }

    public LockRequest build() {
        if (this.redisConnectionFactory == null) {
            throw new RuntimeException("没有Redis连接池");
        }

        if (StringUtils.isBlank(this.lockKey)) {
            throw new RuntimeException("没有锁key");
        }

        return new LockRequest(this);
    }
}
