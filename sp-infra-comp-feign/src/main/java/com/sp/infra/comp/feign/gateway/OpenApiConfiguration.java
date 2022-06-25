package com.sp.infra.comp.feign.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 应用启动上传git版本信息
 * @author luchao
 * @create 2020/9/4
 */
@Configuration
@PropertySource(value="git.properties", ignoreResourceNotFound=true)
public class OpenApiConfiguration {
    /**
     * 向consul写接口开放信息的实现
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenApiRegister applicationStartup() {
        return new OpenApiRegister();
    }
}
