package com.sp.infra.comp.feign.exception;

import com.sp.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务异常转换处理器
 *
 * @author luchao
 * @create 2020/12/15.
 */
@Slf4j
public class SpWebResultErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest,
                                                  boolean includeStackTrace) {
        Map<String, Object> r = new HashMap<>();
        Throwable throwable = getError(webRequest);
        log.debug(throwable.toString());
        log.debug("is BusinessException ? " + (throwable instanceof BusinessException));

        if (throwable instanceof BusinessException) {
            r.put("code", ((BusinessException) throwable).getCode());
            r.put("msg", ((BusinessException) throwable).getMessage());
        } else {
            r.put("code", "500");
            r.put("msg", "当前服务繁忙，请稍后再试。");
        }

        r.put("now", System.currentTimeMillis());
        webRequest.setAttribute("javax.servlet.error.status_code", HttpStatus.OK.value(), 0);
        return r;
    }
}