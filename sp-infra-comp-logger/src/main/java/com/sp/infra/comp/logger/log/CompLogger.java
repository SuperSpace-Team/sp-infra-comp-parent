package com.sp.infra.comp.logger.log;

import com.sp.infra.comp.logger.enums.LogLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @Description 组件统一日志操作类
 * @Author alexlu
 * @date 2021/08/13
 */
@Slf4j
public class CompLogger {
    private static final String model =
            "{\"datetime\":\"{}\",\"type\":\"{}\",\"timestamp\":\"{}\",\"level\":\"{}\",\"message\":\"{}\"}";
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");

    private static final String additionMode(
            String message, String type, Long timestamp, String level) {
        return "{\"datetime\":\""
                + dateTimeFormatter.print(timestamp)
                + "\",\"type\":\""
                + type
                + "\",\"timestamp\":\""
                + timestamp
                + "\",\"level\":\""
                + level
                + "\",\"message\":\""
                + message
                + "\"}";
    }

    public static void infox(String type, String message, Object... messagex) {
        log.info(
                additionMode(message, type, System.currentTimeMillis(), LogLevelEnum.INFO.getValue()), messagex);
    }

    public static void info(String type, String message) {
        long currentTimeMillis = System.currentTimeMillis();
        log.info(
                model,
                dateTimeFormatter.print(currentTimeMillis),
                type,
                currentTimeMillis,
                LogLevelEnum.INFO.getValue(),
                message);
    }

    public static void debugx(String type, String message, Object... messagex) {
        log.debug(
                additionMode(message, type, System.currentTimeMillis(), LogLevelEnum.DEBUG.getValue()), messagex);
    }

    public static void debug(String type, String message) {
        long currentTimeMillis = System.currentTimeMillis();
        log.debug(
                model,
                dateTimeFormatter.print(currentTimeMillis),
                type,
                currentTimeMillis,
                LogLevelEnum.DEBUG.getValue(),
                message);
    }

    public static void warnx(String type, String message, Object... messagex) {
        log.warn(
                additionMode(message, type, System.currentTimeMillis(), LogLevelEnum.WARN.getValue()), messagex);
    }

    public static void warn(String type, String message) {
        long currentTimeMillis = System.currentTimeMillis();
        log.warn(
                model,
                dateTimeFormatter.print(currentTimeMillis),
                type,
                currentTimeMillis,
                LogLevelEnum.WARN.getValue(),
                message);
    }

    public static void errorx(String type, String message, Object... messagex) {
        log.error(
                additionMode(message, type, System.currentTimeMillis(), LogLevelEnum.ERROR.getValue()), messagex);
    }

    public static void error(String type, String message) {
        long currentTimeMillis = System.currentTimeMillis();
        log.error(
                model,
                dateTimeFormatter.print(currentTimeMillis),
                type,
                currentTimeMillis,
                LogLevelEnum.ERROR.getValue(),
                message);
    }
}

