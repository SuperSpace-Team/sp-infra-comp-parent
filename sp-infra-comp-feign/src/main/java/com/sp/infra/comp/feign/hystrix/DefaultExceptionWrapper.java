package com.sp.infra.comp.feign.hystrix;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sp.framework.common.exception.BusinessException;

/**
 * 业务异常包装类
 * 抛出被熔断异常
 * @author luchao
 * @create 2021/7/28.
 */
public class DefaultExceptionWrapper implements ExceptionWrapper {

    @Override
    public Exception wrap(Exception exception) {
        if(exception instanceof BusinessException) {
            return new HystrixBadRequestException(exception.getMessage(), exception);
        }

//        if(exception instanceof RuntimeException) {
//            throw (RuntimeException) exception;
//        }
        return exception;
    }
}
