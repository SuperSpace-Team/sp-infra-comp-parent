package com.sp.infra.comp.feign.filter;

import com.sp.infra.comp.feign.definitions.RequestHeaderConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * Feign拦截器
 * 添加请求头X-SP-FeignOrigin:请求来源
 *
 * @author luchao
 * @create 2020/11/15.
 */
@Slf4j
public class OriginServiceRequestInterceptor implements RequestInterceptor {

    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * Called for every request. Add data using methods on the supplied {@link RequestTemplate}.
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        template.header(RequestHeaderConstant.FEIGN_ORIGIN, applicationName);
        log.debug("request headers : " + template.headers());
    }
}
