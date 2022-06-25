package com.sp.infra.comp.core.validator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @description: POJO基本错误校验器
 * @author: luchao
 * @date: Created in 8/6/21 8:10 PM
 */
public abstract class ModelBaseErrorValidator<T> implements BizValidator<T> {
    @Override
    public boolean support(T source) {
        if(source == null){
            return false;
        }

        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = pt.getActualTypeArguments();
        if(types.length < 1){
            return false;
        }

        Class<T> cls = (Class<T>) types[0];
        return cls.isInstance(source);
    }

    @Override
    public boolean isErrorValidator() {
        return true;
    }
}
