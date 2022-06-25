package com.sp.infra.comp.core.convertor;

import java.io.Serializable;

/**
 * 获取方法名称 FunctionalInterface
 * @param <T>
 */
@FunctionalInterface
public interface FieldGetter<T> extends Serializable {
    Object get(T source);
}
