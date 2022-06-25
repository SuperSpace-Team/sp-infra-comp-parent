package com.sp.infra.comp.rest.registry.config;

/**
 * @author alexlu
 * @date 2019/05/25
 */

import com.sp.infra.comp.logger.log.CompLogger;
import com.sp.infra.comp.rest.registry.condition.SpringWebMvcCondition;
import com.sp.infra.comp.rest.registry.constant.RegistryConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * REST API上报配置类
 * For springmvc-servlet
 */
@Primary
@Configuration
@Conditional(SpringWebMvcCondition.class)
public class ApiMetadataConfiguration {
  public ApiMetadataConfiguration() {
    CompLogger.infox(RegistryConstant.LOG_TAG, "ApiMetadataConfiguration is loaded successfully ...");
  }

  @Bean
  public MetadataRegister metadataRegister() {
    return new MetadataRegister();
  }
}
