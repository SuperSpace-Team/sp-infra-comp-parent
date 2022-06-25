package com.sp.infra.comp.core.validator;

import com.sp.infra.comp.core.model.AppConfigInfo;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @description: 应用认证配置校验器
 * @author: luchao
 * @date: Created in 8/6/21 8:43 PM
 */
public abstract class ConfigBaseValidator implements BizValidator<Object> {
    @Override
    public boolean support(Object source) {
        if(source instanceof List){
            List lt = (List) source;
            if(CollectionUtils.isEmpty(lt)){
                return false;
            }

            if(lt.get(0) instanceof AppConfigInfo){
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isErrorValidator() {
        return false;
    }
}
