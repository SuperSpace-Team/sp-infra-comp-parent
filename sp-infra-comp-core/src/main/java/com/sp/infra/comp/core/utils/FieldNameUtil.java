package com.sp.infra.comp.core.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.sp.infra.comp.core.convertor.FieldGetter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

 /**
 * 利用SerializedLambda 获取方法名称
 *
 * @author alexlu
 * @date 2019-07-05 17:30:43
 */
@Slf4j
public class FieldNameUtil {
    private static Map<Class, SerializedLambda> CLASS_LAMDBA_CACHE = new ConcurrentHashMap<>();
    private static Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    private static Converter<String, String> camelConverter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL);

    /**
     * 转换成下划线
     * @param fn
     * @param <T>
     * @return
     */
    public static <T> String convertToUnderScore(FieldGetter<T> fn) {
        String methodName = getMethodName(getSerializedLambda(fn));
        String prefix = getPrefix(methodName);
        return converter.convert(methodName.substring(prefix.length()));
    }


    /**
     * 转换驼峰
     * @param fn
     * @param <T>
     * @return
     */
    public static <T> String convertToCamel(FieldGetter<T> fn) {
        String methodName = getMethodName(getSerializedLambda(fn));
        String prefix = getPrefix(methodName);
        return camelConverter.convert(methodName.substring(prefix.length()));
    }

    private static String getMethodName(SerializedLambda serializedLambda) {
        SerializedLambda lambda = serializedLambda;
        return lambda.getImplMethodName();
    }

    private static String getPrefix(String methodName) {
        String prefix = "";
        if (methodName.startsWith("get")) {
            prefix = "get";
        } else if (methodName.startsWith("is")) {
            prefix = "is";
        }
        return prefix;
    }


    public static SerializedLambda getSerializedLambda(Serializable fn) {
        SerializedLambda lambda = CLASS_LAMDBA_CACHE.get(fn.getClass());
        if (lambda == null) {
            try {
                Method method = fn.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                lambda = (SerializedLambda) method.invoke(fn);
                CLASS_LAMDBA_CACHE.put(fn.getClass(), lambda);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lambda;
    }
}
