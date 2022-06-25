package com.sp.infra.comp.rest.registry.config;

import com.alibaba.fastjson.JSONObject;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.sp.infra.comp.consul.common.CommonConsulClient;
import com.sp.infra.comp.logger.log.CompLogger;
import com.sp.infra.comp.rest.registry.constant.RegistryConstant;
import com.sp.infra.comp.rest.registry.meta.MetadataLoader;
import com.sp.infra.comp.logger.log.ThrowableStackLogger;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class MetadataRegister implements ApplicationListener<ContextRefreshedEvent> {

  /** 要求框架中有全局的consulClient */
  private ConsulClient consulClient = CommonConsulClient.getDefaultConsulClient();

  @Value(value = "${spring.application.name:}")
  private String appName;

  @Value(value = "${git.commit.id:}")
  private String commitId;

  @Value(value = "${git.build.time:}")
  private String buildTime;

  private static final String SPECIFIED_REQUEST_MAPPING_HANDLER_MAPPING_ID =
      "requestMappingHandlerMapping";

  private static final Class SPECIFIED_REQUEST_MAPPING_HANDLER_MAPPING_TYPE =
      RequestMappingHandlerMapping.class;

  private static final String BEAN_PREFIX = "com.superspace";

  public MetadataRegister() {
    CompLogger.infox(RegistryConstant.LOG_TAG, "MetadataRegister is initialized ...");
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    if (!(event.getApplicationContext() instanceof WebApplicationContext)) {
      return;
    }
    try {
      doRegister(event);
    } catch (Exception e) {
      ThrowableStackLogger.logCompError(RegistryConstant.LOG_TAG, e);
    }
  }

  private void doRegister(ContextRefreshedEvent event) {
    if (consulClient == null) {
      CompLogger.warnx(RegistryConstant.LOG_TAG, "consulClient for setting api metadata is null");
      return;
    }

    if (appName == null || "".equals(appName)) {
      CompLogger.warnx(RegistryConstant.LOG_TAG, "appName for setting api metadata is null");
      return;
    }

    Long buildTimeLong =
        (buildTime == null || "".equals(buildTime)) ? null : new DateTime(buildTime).getMillis();

    CompLogger.infox(
        RegistryConstant.LOG_TAG,
        "appName= " + appName + ", commitId= " + commitId + ", buildTime= " + buildTimeLong);

    Object bean = null;
    try {
      bean = event.getApplicationContext().getBean(SPECIFIED_REQUEST_MAPPING_HANDLER_MAPPING_ID);
    } catch (BeansException e) {
      Map<String, RequestMappingHandlerMapping> beans =
          event
              .getApplicationContext()
              .getBeansOfType(SPECIFIED_REQUEST_MAPPING_HANDLER_MAPPING_TYPE);
      if (beans != null && !beans.isEmpty()) {
        bean = beans.values().stream().findFirst().get();
      }
    }

    if (bean == null || !(bean instanceof RequestMappingHandlerMapping)) {
      CompLogger.warnx(RegistryConstant.LOG_TAG, "specified requestMappingHandlerMapping : {} is unexpected", bean);
      return;
    }

    RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping) bean;

    Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

    if (handlerMethods != null && !handlerMethods.isEmpty()) {
      handlerMethods.keySet().stream()
          .forEach(
              requestMappingInfo -> {
                if (requestMappingInfo != null) {
                  Set<String> paths = requestMappingInfo.getPatternsCondition().getPatterns();
                  for (String path : paths) {
                    if (requestMappingInfo.getMethodsCondition().getMethods().size() == 0) {
                      for (RequestMethod reqMethod : RequestMethod.values()) {
                        MetadataLoader.getInstance().addRegistryMetadata(path, reqMethod.name());
                      }

                    } else {
                      for (RequestMethod requestMethod :
                          requestMappingInfo.getMethodsCondition().getMethods()) {
                        MetadataLoader.getInstance()
                            .addRegistryMetadata(path, requestMethod.name());
                      }
                    }
                  }
                }
              });
    }

    // 往consul写当前服务开放的api配置数据
    String key = MetadataStore.PREFIX_KEY + appName;
    Response<GetValue> response = consulClient.getKVValue(key);

    MetadataStore storeTemp = new MetadataStore();
    storeTemp.setCommitId(commitId);
    storeTemp.setBuildTime(buildTimeLong);
    storeTemp.setMetadatas(
        new ArrayList<>(MetadataLoader.getInstance().getRegistryMetadatas().values()));

    if (response.getValue() == null) {
      // kv不存在，新增一条
      PutParams putParams = new PutParams();
      putParams.setCas(0L);
      Response<Boolean> r = null;
      try {
        r = consulClient.setKVValue(key, JSONObject.toJSONString(storeTemp), putParams);
      } catch (Exception e) {
        ThrowableStackLogger.logCompError(RegistryConstant.LOG_TAG, e);
        return;
      }
      if (r.getValue()) {
        CompLogger.infox(RegistryConstant.LOG_TAG, "success created api-metadata in consul : {}", storeTemp);
      } else {
        CompLogger.infox(RegistryConstant.LOG_TAG, "api-metadata in consul was existed : {} ", storeTemp);
      }
    } else {
      // kv存在
      MetadataStore origin = parseStoreTemp(response.getValue());
      if (buildTimeLong != null && origin != null && buildTimeLong.equals(origin.getBuildTime())) {
        CompLogger.infox(RegistryConstant.LOG_TAG, "same buildTime  {}  for service  {}", buildTimeLong, appName);
        return;
      } else {
        // 更新
        CompLogger.infox(
            RegistryConstant.LOG_TAG,
            "different buildTime : {} origin : {}",
            buildTimeLong,
            origin == null ? "-1" : origin.getBuildTime());
        Boolean r = null;
        try {
          r = updateKV(key, storeTemp, response.getConsulIndex());
        } catch (Exception e) {
          ThrowableStackLogger.logCompError(RegistryConstant.LOG_TAG, e);
          r = false;
        }
        if (r) {
          CompLogger.debugx(
              RegistryConstant.LOG_TAG,
              "origin metadata store : {}， updated  metadata store : {}",
              origin,
              storeTemp);
        } else {
          CompLogger.debugx(
              RegistryConstant.LOG_TAG,
              "origin metadata store : {}  fail to update metadata store :{}",
              origin,
              storeTemp);
        }
      }
    }
  }

  private Boolean updateKV(String key, MetadataStore storeTemp, Long cas) {
    PutParams putParams = new PutParams();
    putParams.setCas(cas);
    Response<Boolean> r =
        consulClient.setKVValue(key, JSONObject.toJSONString(storeTemp), putParams);
    return r.getValue();
  }

  private MetadataStore parseStoreTemp(GetValue lockKeyContent) {
    if (lockKeyContent == null) {
      return null;
    }
    String decodedValue = lockKeyContent.getDecodedValue(StandardCharsets.UTF_8);
    return JSONObject.parseObject(decodedValue, MetadataStore.class);
  }
}
