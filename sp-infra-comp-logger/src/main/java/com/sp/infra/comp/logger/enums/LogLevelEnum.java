package com.sp.infra.comp.logger.enums;

/**
 * @Description 日志等级枚举
 * @Author alexlu
 * @date 2021.08.13
 */
public enum LogLevelEnum {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private String value;

    LogLevelEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
