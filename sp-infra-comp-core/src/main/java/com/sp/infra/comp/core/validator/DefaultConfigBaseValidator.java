package com.sp.infra.comp.core.validator;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 应用认证配置校验器
 * @author: luchao
 * @date: Created in 8/10/21 10:05 AM
 */
@Component
public class DefaultConfigBaseValidator extends ConfigBaseValidator {
    @Override
    public List<Error> validate(Object source) {
        return null;
    }
}
