package com.sp.infra.comp.core.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @description: 业务校验器管理实现类
 * @author: luchao
 * @date: Created in 8/5/21 4:59 PM
 */
@Slf4j
@Component
public class BizValidationManagerImpl implements BizValidationManager {
    @Autowired
    private List<BizValidator> validators;

    @Override
    public List<Error> validate(Object data) {
        List<Error> errors = validateError(data);
        if (!CollectionUtils.isEmpty(errors)) {
            return errors;
        }

        return validateWarn(data);
    }

    @Override
    public List<Error> validateError(Object data) {
        List<Error> errors = null;
        for (BizValidator v : validators) {
            if (log.isDebugEnabled()) {
                log.debug("Validator class is : {}", v.getClass().getName());
            }

            if (!v.isErrorValidator()) {
                continue;
            }

            if (v.support(data)) {
                errors = v.validate(data);
                if (!CollectionUtils.isEmpty(errors)) {
                    break;
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (!CollectionUtils.isEmpty(errors) && log.isDebugEnabled()) {
                log.debug("First error is : {}", errors.get(0).getMessage());
            }
        }
        return errors;
    }

    @Override
    public List<Error> validateWarn(Object data) {
        List<Error> errors = null;
        for (BizValidator v : validators) {
            if (v.isErrorValidator()) {
                continue;
            }

            if (v.support(data)) {
                errors = v.validate(data);
                if (!CollectionUtils.isEmpty(errors)) {
                    break;
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (!CollectionUtils.isEmpty(errors) && log.isDebugEnabled()) {
                log.debug("First warn is:{}", errors.get(0).getMessage());
            }
        }
        return errors;
    }
}
