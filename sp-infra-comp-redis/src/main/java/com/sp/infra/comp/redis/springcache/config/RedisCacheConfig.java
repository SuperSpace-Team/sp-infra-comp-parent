package com.sp.infra.comp.redis.springcache.config;

import com.sp.infra.comp.redis.utils.FastJsonRedisSerializer;
import com.sp.infra.comp.redis.utils.StringRedisSerializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lys
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "cache.redis", name = "enabled", havingValue = "true")
public class RedisCacheConfig {

    @Resource
    private RedisConnectionFactory factory;

    @Resource
    private CacheRedisExpireProperties cacheExpireProperties;

    // redis缓存的有效时间单位是秒
    @Value("${cache.redis.default.expire:3600}")
    private long redisDefaultExpiration;

    RedisSerializer redisSerializer = new StringRedisSerializer();
    FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();


    /**
     * 重写RedisCacheManager的getCache方法，实现设置key的有效时间
     * 重写RedisCache的get方法，实现触发式自动刷新
     * <p>
     * 自动刷新方案：
     * 1、获取缓存后再获取一次有效时间，拿这个时间和我们配置的自动刷新时间比较，如果小于这个时间就刷新。
     * 2、每次创建缓存的时候维护一个Map，存放key和方法信息（反射）。当要刷新缓存的时候，根据key获取方法信息。
     * 通过获取其代理对象执行方法，刷新缓存。
     *
     * @return
     */
    @Bean
    @Primary
    public RedisCacheManager cacheManager() {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(getRedisCacheConfigurationWithTtl(Duration.ofSeconds(redisDefaultExpiration)))
                .withInitialCacheConfigurations(getRedisCacheConfigurationMap())
                .build();
    }


    /**
     * 获取自定义的缓存超时时间配置
     *
     * @return
     */
    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        if (Objects.isNull(cacheExpireProperties) || Objects.isNull(cacheExpireProperties.getExpires())) {
            return redisCacheConfigurationMap;
        }
        //expire time config
        List<Expire> expires = cacheExpireProperties.getExpires();

        for (Expire expire : expires) {
            if (StringUtils.isBlank(expire.getNamespace()) || Objects.isNull(expire.getTtl())) {
                continue;
            }
            redisCacheConfigurationMap.put(StringUtils.trim(expire.getNamespace()), getRedisCacheConfigurationWithTtl(Duration.ofSeconds(expire.getTtl())));
        }
        return redisCacheConfigurationMap;
    }

    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Duration duration) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(buildSerializer(redisSerializer))
                // .serializeValuesWith(buildSerializer(redisSerializer))
                .serializeValuesWith(buildSerializer(fastJsonRedisSerializer))
                // 打开后 null 存储异常
                // .disableCachingNullValues()
                .entryTtl(duration);
        return redisCacheConfiguration;
    }

    /**
     * 获取序列化工具
     *
     * @param redisSerializer
     * @return
     */
    private RedisSerializationContext.SerializationPair buildSerializer(RedisSerializer redisSerializer) {
        return RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer);
    }
}
