package com.sp.infra.comp.feign.hystrix;

import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 是否处理异常熔断判断配置类
 * @author luchao
 * @create 2020/7/28.
 */
@Configuration
@ConditionalOnClass({Feign.class})
public class HystrixSupportConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "feign.hystrix.enabled", matchIfMissing = false)
    public ExceptionWrapper exceptionWrapper() {
        return new DefaultExceptionWrapper();
    }

}
