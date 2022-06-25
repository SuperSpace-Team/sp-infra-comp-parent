package com.sp.infra.comp.feign.definitions;

import java.lang.annotation.*;

/**
 * 用来标注某个API允许被哪些服务访问
 *
 * @author luchao
 * @create 2020/10/26.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OriginService {

    /**
     * 该接口开放访问的服务名
     * @return
     */
    String[] name() default {};
}
