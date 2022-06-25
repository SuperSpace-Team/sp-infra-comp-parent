package com.sp.infra.comp.feign.definitions;

import java.lang.annotation.*;

/**
 * API鉴权注解
 * 用来标注某个API是对外开放的,可以根据此信息来生成用于API网关的过滤配置信息
 *
 * @author luchao
 * @create 2020/8/29.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OpenAPI {

    /**
     * 该接口开放访问的产品
     * @return
     */
    String[] products() default {};

    /**
     * 该接口访问需要的权限
     * @return
     */
    String[] authorities() default {};

}
