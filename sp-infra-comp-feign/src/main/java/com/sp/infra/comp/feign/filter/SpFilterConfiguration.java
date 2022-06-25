package com.sp.infra.comp.feign.filter;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign接口的来源过滤功能：
 *
 * 1、客户端的头信息加工（增加服务来源标志）
 * 2、服务端的过滤器（根据头信息中的服务来源标志）
 *
 * @author luchao
 *
 */
@Data
@Configuration
public class SpFilterConfiguration {

    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * 根据头信息实现服务来源过滤
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "security.origin-filter.enabled", matchIfMissing = false)
    public RequestOriginFilter requestOriginFilter() {
        return new RequestOriginFilter();
    }

    /**
     * 请求拦截器
     *
     * 添加头信息：X-SP-FeignOrigin，请求来源
     *
     * @return
     */
    @Bean
    public OriginServiceRequestInterceptor originServiceRequestInterceptor() {
        return new OriginServiceRequestInterceptor();
    }
}