package com.sp.infra.comp.sc.rest.registry.config;

import com.sp.infra.comp.rest.registry.config.ApiMetadataConfiguration;
import org.springframework.context.annotation.Import;

/**
 * SpringCloud REST API上报配置类
 */
@Import(ApiMetadataConfiguration.class)
public class SpringBoot4ApiMetadataConfiguration {

}
