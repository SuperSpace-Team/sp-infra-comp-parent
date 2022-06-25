package com.sp.infra.comp.redis.lock.model;

import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 锁请求
 * @author Wang Chong
 */
public class LockRequest {
    private RedisConnectionFactory redisConnectionFactory;
    private String lockKey;
    private Integer timeoutMilliseconds;
    private Integer expiredMilliseconds;

    public LockRequest() {
    }

    public LockRequest(LockRequestBuilder builder) {
        this.redisConnectionFactory = builder.redisConnectionFactory;
        this.lockKey = builder.lockKey;
        this.timeoutMilliseconds = builder.timeoutMilliseconds;
        this.expiredMilliseconds = builder.expiredMilliseconds;
    }

    public RedisConnectionFactory getRedisConnectionFactory() {
        return redisConnectionFactory;
    }

    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public Integer getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    public void setTimeoutMilliseconds(Integer timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    public Integer getExpiredMilliseconds() {
        return expiredMilliseconds;
    }

    public void setExpiredMilliseconds(Integer expiredMilliseconds) {
        this.expiredMilliseconds = expiredMilliseconds;
    }
}
