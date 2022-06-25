package com.sp.infra.comp.feign.gateway;

import com.alibaba.fastjson.JSONObject;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.sp.infra.comp.feign.definitions.OpenApiBean;
import com.sp.infra.comp.feign.definitions.OpenApiConsulStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.*;
import sun.misc.BASE64Decoder;

import java.util.ArrayList;

/**
 * 应用启动结束之后，将加载的openapi数据存储到consul
 *
 * @author luchao
 * @create 2021/9/4
 */
@Slf4j
public class OpenApiRegister implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    private ConsulClient consulClient;

    @Autowired
    private Environment environment;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent().getParent() != null) {
            log.warn(event.getApplicationContext() + " is not root context");
            return;
        }

        if(consulClient == null) {
            log.warn("consulClient is null");
            return;
        }

        String appName = environment.getProperty("spring.application.name");
        String commitId = environment.getProperty("git.commit.id");
        String buildTime = environment.getProperty("git.build.time");
        Long buildTimeLong = buildTime == null ? null : new DateTime(buildTime).getMillis();
        log.info("appName=" + appName + ", commitId=" + commitId + ", buildTime=" + buildTimeLong);

        for(OpenApiBean api : OpenApiLoadData.getInstance().getData().values()) {
            log.info(api.toString());
        }

        // 往consul写当前服务开放的api配置数据
        String key = OpenApiConsulStore.PREFIX_KEY + appName;
        Response<GetValue> response = consulClient.getKVValue(key);

        OpenApiConsulStore openApiTemp = new OpenApiConsulStore();
        openApiTemp.setCommitId(commitId);
        openApiTemp.setBuildTime(buildTimeLong);
        openApiTemp.setData(new ArrayList<>(OpenApiLoadData.getInstance().getData().values()));

        if(response.getValue() == null) {
            // kv不存在，新增一条
            PutParams putParams = new PutParams();
            putParams.setCas(0L);
            Response<Boolean> r = consulClient.setKVValue(key, JSONObject.toJSONString(openApiTemp), putParams);
            if(r.getValue()) {
                log.info("created : " + openApiTemp);
            } else {
                log.info("existed : " + openApiTemp);
            }
        } else {
            // kv存在
            OpenApiConsulStore origin = parseOpenApiTemp(response.getValue());
            if(buildTimeLong != null && buildTimeLong.equals(origin.getBuildTime())) {
                log.info("same buildTime : " + buildTimeLong);
                return;
            } else {
                // 更新
                log.info("different buildTime : " + buildTimeLong + ", origin : " + origin.getBuildTime());
                Boolean r = updateKV(key, openApiTemp, response.getConsulIndex());
                if(r) {
                    log.info("origin  : " + origin);
                    log.info("updated : " + openApiTemp);
                } else {
                    log.info("origin info  : " + origin);
                    log.info("to update    : " + openApiTemp);
                    log.info("been updated : " + parseOpenApiTemp(consulClient.getKVValue(key).getValue()));
                }
            }
        }
    }

    public Boolean updateKV(String key, OpenApiConsulStore openApiTemp, Long cas) {
        PutParams putParams = new PutParams();
        putParams.setCas(cas);
        Response<Boolean> r = consulClient.setKVValue(key, JSONObject.toJSONString(openApiTemp), putParams);
        return r.getValue();
    }

    @SneakyThrows
    public OpenApiConsulStore parseOpenApiTemp(GetValue lockKeyContent) {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] v = decoder.decodeBuffer(lockKeyContent.getValue());
        String lockKeyValueDecode = new String(v);
        return JSONObject.parseObject(lockKeyValueDecode, OpenApiConsulStore.class);
    }

}