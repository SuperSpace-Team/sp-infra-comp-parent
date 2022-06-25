package com.sp.infra.comp.feign.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring MVC异常处理，记录异常日志
 *
 * @author luchao
 * @create 2020/6/30.
 */
@Slf4j
public class SpExceptionHandler implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception ex) {
        try {
            if(request == null){
                return null;
            }

            // 记录错误日志
            log.error(request.toString(), ex);
        } catch (Exception e) {
            log.warn("ExceptionLogger error : " + e.getMessage());
        }
        return null;
    }

}
