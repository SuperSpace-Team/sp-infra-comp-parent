package com.sp.infra.comp.redis.lock.impl;

import com.sp.infra.comp.redis.lock.Lock;
import com.sp.infra.comp.redis.lock.model.LockRequest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Random;

/**
 * 锁实现
 * @author  Wang Chong
 */
public class RedisLockImpl implements Lock {
    public static final int TIMEOUT_MILLISECONDS_LEVEL_1 = 5000;
    public static final int TIMEOUT_MILLISECONDS_LEVEL_2 = 7000;
    public static final int TIMEOUT_MILLISECONDS_LEVEL_3 = 9000;
    private static final Random R = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockImpl.class);
    private final static int SLEEP_TIME = 50;
    private final static int RANDOM_RANGE = 17;
    /**
     * 默认三秒的超时时间
     */
    public final static int DEFAULT_TIMEOUT_MILLISECONDS = 3000;
    /**
     * 锁过期时间，默认一分钟，如果一个锁加锁超过一分钟依然没释放，自动释放
     */
    public final static int DEFAULT_EXPIRED_MILLISECONDS = 60 * 1000;
    private final RedisConnectionFactory redisConnectionFactory;
    private final String lockKey;
    private final int timeoutMilliseconds;
    private final int expiredMilliseconds;
    private boolean locked = false;

    public RedisLockImpl(@NotNull LockRequest lockRequest) {
        this.redisConnectionFactory = lockRequest.getRedisConnectionFactory();
        this.lockKey = lockRequest.getLockKey();

        this.timeoutMilliseconds = lockRequest.getTimeoutMilliseconds() == null ? DEFAULT_TIMEOUT_MILLISECONDS : lockRequest.getTimeoutMilliseconds();

        this.expiredMilliseconds = lockRequest.getExpiredMilliseconds() == null ? DEFAULT_EXPIRED_MILLISECONDS : lockRequest.getExpiredMilliseconds();
    }


    /**
     * 获取锁，如果成功，加锁
     *
     * @return 是否成功拿到锁
     * @throws InterruptedException 如果获取过程中被中断，抛出此异常
     */
    @Override
    public boolean acquire() throws InterruptedException {
        long timeout = System.currentTimeMillis() + timeoutMilliseconds;
        while (System.currentTimeMillis() <= timeout && !tryAcquire()) {
            int sleep = RANDOM_RANGE > 0 ? SLEEP_TIME + R.nextInt(RANDOM_RANGE) : SLEEP_TIME;
            Thread.sleep(sleep, R.nextInt(500));
        }

        return locked;
    }

    /**
     * 关闭并释放锁
     */
    @Override
    public void close() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            if (locked) {
                connection.del(lockKey.getBytes());
            }
        } finally {
            connection.close();
        }
    }

    private boolean tryAcquire() {
        LOGGER.debug("try to acquire redis lock for {}", lockKey);

        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            long expireTimestamp = System.currentTimeMillis() + expiredMilliseconds + 1;
            String expireTimestampStr = String.valueOf(expireTimestamp);
            boolean done = this.setNx(lockKey, expireTimestampStr);

            if (done) {
                locked = true;
                return true;
            }

            // 走到这一步表示没拿到锁，需要判断前一个锁的持有者是否超时
            byte[] bytes = connection.stringCommands().get(lockKey.getBytes());

            if (bytes == null || StringUtils.isBlank(new String(bytes))) {
                if (this.setNx(lockKey, expireTimestampStr)) {
                    locked = true;
                    return true;
                }

                return false;
            }


            String previousTimestamp = new String(bytes);

            if (Long.parseLong(previousTimestamp) < System.currentTimeMillis()) {
                // 锁已超时，没有正常释放

                byte[] set = connection.stringCommands().getSet(lockKey.getBytes(), expireTimestampStr.getBytes());
                if (set == null) {
                    locked = true;
                    return true;
                }

                String previousTimestampAfterGetSet = new String(set);
                // 多线程竞争时，只有第一个调用getSet的才能拿到锁
                if (previousTimestampAfterGetSet.equals(previousTimestamp)) {
                    locked = true;
                    return true;
                }
            }

            return false;
        } finally {
            connection.close();
        }
    }

    /**
     * 设置key
     *
     * @param lockKey            锁key
     * @param expireTimestampStr 内容
     * @return 是否成功
     */
    private boolean setNx(String lockKey, String expireTimestampStr) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        Boolean result = connection.stringCommands().setNX(lockKey.getBytes(), expireTimestampStr.getBytes());

        return result == null ? false : result;
    }
}
