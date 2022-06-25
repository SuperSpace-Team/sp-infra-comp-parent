package com.sp.infra.comp.core.validator;

import com.sp.framework.common.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Validator校验工具类
 * 参考文档：http://docs.jboss.org/hibernate/validator/5.4/reference/en-US/html_single/
 */
public class ValidatorUtils {
    private ValidatorUtils() {
        throw new IllegalStateException("Illegal State Exception");
    }

    private static Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 校验对象
     *
     * @param object 待校验对象
     * @param groups 待校验的组
     * @throws BusinessException 校验不通过，则报BusinessException异常
     */
    public static void validateEntity(Object object, Class<?>... groups)
            throws BusinessException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (constraintViolations.isEmpty()) {
            return;
        }

        StringBuilder stbValidateMsg = new StringBuilder();
        for (ConstraintViolation<Object> constraint : constraintViolations) {
            stbValidateMsg.append(constraint.getMessage()).append("<br>");
        }

        String msgStr = stbValidateMsg.toString();
        if (StringUtils.isNotBlank(msgStr)) {
            msgStr = msgStr.substring(0, msgStr.length() - 4);
        }

        throw new BusinessException(msgStr);
    }
}
