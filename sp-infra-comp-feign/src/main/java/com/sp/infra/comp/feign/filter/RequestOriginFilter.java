package com.sp.infra.comp.feign.filter;

import com.sp.infra.comp.feign.definitions.RequestHeaderConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求来源过滤器
 * @author luchao
 * @create 2020/10/26.
 */
@Slf4j
@WebFilter
public class RequestOriginFilter implements Filter {

    public static Map<String, String[]> REQUEST_INFO_MAP = new HashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestOriginFilter init success");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String origin = request.getHeader(RequestHeaderConstant.FEIGN_ORIGIN);
        String path = request.getServletPath();
        String method = request.getMethod();

        if(StringUtils.isBlank(origin)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if(log.isDebugEnabled()) {
            log.debug("method={}, path={}, origin={}", method, path, origin);
        }

        // 判断当前method的path是否有内部访问控制
        if(REQUEST_INFO_MAP.get(path) == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 判断来源服务是否在定义的允许服务名中（OriginService注解中配置的服务）
        for(String serviceName : REQUEST_INFO_MAP.get(path)) {
            if(serviceName.equals(origin)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setStatus(401);
    }

    @Override
    public void destroy() {
        log.info("RequestOriginFilter destroy success");
    }
}
