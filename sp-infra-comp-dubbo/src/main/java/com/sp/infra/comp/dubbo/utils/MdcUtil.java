package com.sp.infra.comp.dubbo.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @description: MDC日志操作工具类
 * @author: luchao
 * @date: Created in 2/18/22 10:13 PM
 */
public class MdcUtil {
    public static final String TRACE_ID = "TRACE-ID";

    public static boolean insertMDC(String uniqueId) {
        MDC.put(TRACE_ID, uniqueId);
        return true;
    }

    public static final String generateTraceId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static final String getOrDefaultMdc() {
        String traceId = MDC.get(TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = generateTraceId();
            insertMDC(traceId);
        }
        return traceId;
    }
}
