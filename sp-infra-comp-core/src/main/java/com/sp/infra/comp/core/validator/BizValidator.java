package com.sp.infra.comp.core.validator;

import java.util.List;

/**
 * @description: 业务校验器接口方法定义
 * @author: luchao
 * @date: Created in 8/5/21 4:57 PM
 */
public interface BizValidator<T> {
    /**
     * 是否适用于校验动作
     * @param source
     * @return
     */
    boolean support(T source);

    /**
     * 是否异常/错误校验器
     * @return
     */
    boolean isErrorValidator();

    /**
     * POJO属性的校验操作
     * @param source
     * @return
     */
    List<Error> validate(T source);
}
