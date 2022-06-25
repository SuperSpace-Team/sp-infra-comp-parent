package com.sp.infra.comp.redis.springcache.config;

import lombok.Data;

@Data
public class Expire {
    /**
     * cache namespace
     */
    private String namespace;

    /**
     * second
     */
    private Integer ttl;
}