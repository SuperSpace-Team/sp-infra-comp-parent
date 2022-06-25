package com.sp.infra.comp.feign.hystrix;

/**
 * 异常包装接口
 * @author luchao
 * @create 2021/7/28.
 */
public interface ExceptionWrapper {

    /**
     * 如果exception是业务异常，就用HystrixBadRequestException包装一下
     *
     * @param exception 被包装前的异常
     * @return
     */
    Exception wrap(Exception exception);
}
