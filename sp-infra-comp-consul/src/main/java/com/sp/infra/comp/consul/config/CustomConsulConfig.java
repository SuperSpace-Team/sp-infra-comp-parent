package com.sp.infra.comp.consul.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sirius
 * @date 2019/12/05
 */
@Configuration
public class CustomConsulConfig {
    /**
     * 环境标示获取
     *
     * @return
     */
    @Bean
    public EnvReader envReader() {
        return new EnvReader();
    }
}
