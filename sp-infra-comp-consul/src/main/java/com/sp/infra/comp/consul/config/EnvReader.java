package com.sp.infra.comp.consul.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@Slf4j
public class EnvReader {

    @Value("${env:local}")
    private String env;

    public static EnvReader instance;

    @PostConstruct
    public void init() {
        log.info("env : " + env);
        instance = this;
    }

    public static EnvReader getInstance() {
        return instance;
    }

    public boolean isTestEnv() {
        switch (env) {
            case "dev":
            case "integration":
            case "prod":
            case "local":
            case "stage":
            case "qa2":
            case "qa3":
            case "qa4":
            case "qa5":
            case "t1":
            case "t2":
            case "prelaunch":
                return true;
            default:
                return false;
        }
    }

    public boolean isOnlineEnv() {
        switch (env) {
            case "online":
            case "tonline":
                return true;
            default:
                return false;
        }
    }

}
