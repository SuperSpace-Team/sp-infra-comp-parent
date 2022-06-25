package com.sp.infra.comp.core.validator;

import com.sp.framework.common.base.BaseBizEnum;
import com.sp.framework.common.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;

 /**
 * @description: 数据校验工具类
 * @author: luchao
 * @date: Created in 6/22/21 10:52 PM
 */
public abstract class Assert {

    private Assert() {
        throw new IllegalStateException("Illegal State Exception");
    }

    /**
     * 方法名字有歧义，请使用notBlank()
     *
     * @param str
     * @param message
     */
    @Deprecated
    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 方法名字有歧义，请使用notNull()
     *
     * @param object
     * @param message
     */
    @Deprecated
    public static void isNull(Object object, String message) {
        if (object == null) {
            throw new BusinessException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    public static void isTrue(boolean expression, int errorCode, String message) {
        if (!expression) {
            throw new BusinessException(errorCode, message);
        }
    }

    public static void isTrue(boolean expression, BaseBizEnum error) {
        if (!expression) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    public static void notNull(Object obj, int code, String message) {
        if (obj == null) {
            throw new BusinessException(code, message);
        }
    }

    public static void notNull(Object obj, BaseBizEnum error) {
        if (obj == null) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }

    public static void notBlank(String obj, String message) {
        if (StringUtils.isBlank(obj)) {
            throw new BusinessException(message);
        }
    }

    public static void notBlank(String obj, int code, String message) {
        if (StringUtils.isBlank(obj)) {
            throw new BusinessException(code, message);
        }
    }

    public static void notBlank(String obj, BaseBizEnum error) {
        if (StringUtils.isBlank(obj)) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Object[] array, int code, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new BusinessException(code, message);
        }
    }

    public static void notEmpty(Object[] array, BaseBizEnum error) {
        if (ObjectUtils.isEmpty(array)) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, int code, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException(code, message);
        }
    }

    public static void notEmpty(Collection<?> collection, BaseBizEnum error) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, int code, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BusinessException(code, message);
        }
    }

    public static void notEmpty(Map<?, ?> map, BaseBizEnum error) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BusinessException(error.getCode(), error.getMsg());
        }
    }
}
