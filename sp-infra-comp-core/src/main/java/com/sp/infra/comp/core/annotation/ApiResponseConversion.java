package com.sp.infra.comp.core.annotation;

import com.sp.infra.comp.core.enums.AdaptTypeEnum;

import java.lang.annotation.*;

/**
 *
 * @ClassName: API返回体封装转换类型注解
 * @Description: 用于方法上的注解,包含此注解则返回的json格式会经过ApiResponseHandler封装转换为AdaptTypeEnum指定的真实类型
 * @author luchao
 * @date 2021/4/29
 *
 */
@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiResponseConversion {
    /**
     * 适配返回类型枚举
     * @return
     */
    AdaptTypeEnum adaptType();
}
