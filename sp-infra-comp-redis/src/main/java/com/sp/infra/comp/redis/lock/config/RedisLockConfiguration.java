package com.sp.infra.comp.redis.lock.config;

import com.sp.infra.comp.redis.lock.LockFactory;
import com.sp.infra.comp.redis.lock.impl.DefaultLockFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author Wang Chong
 * @Date 2019/10/8
 **/
@Configuration
public class RedisLockConfiguration {
    /**
     * 获取默认的分布式锁工厂
     *
     * @param redisConnectionFactory redis连接工厂
     * @return 锁工厂
     */
    @Bean("lockFactory")
    public LockFactory lockFactory(RedisConnectionFactory redisConnectionFactory) {
        return new DefaultLockFactoryImpl(redisConnectionFactory);
    }
}
