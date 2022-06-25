package com.sp.infra.comp.redis.springcache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * author lys
 */
@Data
@ConfigurationProperties(prefix = "cache.redis")
@Component
public class CacheRedisExpireProperties {

    private List<Expire> expires;

}
