package com.sp.infra.comp.datasource;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * 默认动态数据源切面类
 * Created by luchao on 2021/8/7.
 */
public class DefaultDataSourceAroundAspect extends GenericAnnotationAroundAspect {
    @Override
    public boolean process(Method method) {
        if (method == null) {
            return false;
        }

        DataSource dataSourceAnnotation = method.getAnnotation(DataSource.class);
        //父类中找到了注解
        if (dataSourceAnnotation == null) {
            return false;
        }

        String value = dataSourceAnnotation.value();
        if(StringUtils.isBlank(value)){
            return false;
        }

        DynamicDataSourceHolders.pushDataSource(value);
        return true;
    }
}
