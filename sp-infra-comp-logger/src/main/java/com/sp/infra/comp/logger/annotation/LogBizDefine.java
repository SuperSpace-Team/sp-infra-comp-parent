package com.sp.infra.comp.logger.annotation;

import com.sp.infra.comp.logger.log.handler.DefaultLogBizHandler;
import com.sp.infra.comp.logger.log.handler.ILogBizHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 业务日志埋点注解
 * @author: luchao
 * @date: Created in 2/16/22 7:07 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LogBizDefine {
    /**
     * 日志业务类型
     * @return
     */
    String bizType() default "";

    /**
     * 日志业务描述
     * @return
     */
    String bizDesc() default "";

    /**
     * 自定义业务日志处理器
     * @return
     */
    Class<? extends ILogBizHandler> handler() default DefaultLogBizHandler.class;
}
