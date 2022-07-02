package com.sp.infra.comp.consul.common;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.annotation.Bean;

/**
 * 自定义Consul客户端
 * @author luchao
 * @create 2022/05/31
 */
@Slf4j
public class CustomConsulClient {

    @Bean
    public ConsulClient consulClient(ConsulProperties consulProperties) {
        log.info("ConsulProperties host = " +consulProperties.getHost());

        String host = System.getenv("DOCKER_CONSUL_HOST");
        log.info("init ConsulClient, DOCKER_CONSUL_HOST = " + host);
        if(host != null) {
            log.info("register ConsulClient use DOCKER_CONSUL_HOST = " + host);
            return new ConsulClient(host, consulProperties.getPort());
        }
        log.info("register ConsulClient use " + consulProperties.getHost());
        ConsulClient consulClient = null;
        try {
            consulClient = new ConsulClient(consulProperties.getHost(), consulProperties.getPort());
            Response<String> response = consulClient.getStatusLeader();
            log.info("consul status leader : " + response.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            return consulClient;
        }
    }
}
