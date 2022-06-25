package com.sp.infra.comp.logger.aspect;

import com.sp.framework.common.constant.CommonConstants;
import com.sp.framework.common.enums.SystemErrorCodeEnum;
import com.sp.framework.common.exception.BusinessException;
import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.logger.annotation.LogBizDefine;
import com.sp.infra.comp.logger.enums.BizDeptEnum;
import com.sp.infra.comp.logger.log.handler.ILogBizHandler;
import com.sp.infra.comp.logger.log.model.LogResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @description: 业务日志埋点处理切面
 * @author: luchao
 * @date: Created in 2/16/22 7:47 PM
 */
@Aspect
@Order(999)
@Component
@Slf4j
public class LogAspect {
    @Around("@annotation(logBizDefine)")
    public void around(ProceedingJoinPoint joinPoint, LogBizDefine logBizDefine) throws Throwable{
        Object result = null;
        LogResultModel logModel = new LogResultModel();
        logModel.setSuccess(false);
        long startTime = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();
        }catch (BusinessException | SystemException e){
            logModel.setErrorCode(String.valueOf(((SystemException)e).getCode()));
            logModel.setErrorMsg(e.getMessage());
            throw e;
        }catch (NullPointerException e){
            logModel.setErrorCode(String.valueOf(SystemErrorCodeEnum.GET_INSTANCE_ERROR.getCode()));
            logModel.setErrorMsg(SystemErrorCodeEnum.GET_INSTANCE_ERROR.getMsg());
            throw e;
        }catch (Exception e){
            logModel.setErrorCode(String.valueOf(SystemErrorCodeEnum.SYSTEM_ERROR.getCode()));
            logModel.setErrorMsg(SystemErrorCodeEnum.SYSTEM_ERROR.getMsg());
            throw e;
        }finally {
            MethodSignature mdSig = (MethodSignature) joinPoint.getSignature();
            String logBizType = logBizDefine.bizType();
            Method targetMethod = mdSig.getMethod();
            logModel.setMethod(targetMethod.getDeclaringClass().getSimpleName()
                    + CommonConstants.TAG_DOT + targetMethod.getName());
            if(StringUtils.isBlank(logBizType)){
                logBizType = targetMethod.getName();
            }

            logModel.setLogType(logBizType);
            logModel.setDepart(BizDeptEnum.COMMON.getMsg());
            handleResult(logBizDefine, result, logModel);
            logModel.setCost(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 处理执行结果
     * @param logBizDefine
     * @param result
     * @param logModel
     */
    private void handleResult(LogBizDefine logBizDefine, Object result, LogResultModel logModel) {
        try {
            ILogBizHandler logBizHandler = logBizDefine.handler().newInstance();
            if(logBizHandler == null){
                return;
            }

            if(logBizHandler.bizDeptType() == null) {
                log.error("LogBizHandler.bizDeptType cannot be empty!");
                return;
            }

            logModel.setDepart(logBizHandler.bizDeptType().getMsg());
            logBizHandler.handle(result, logModel);
        }catch (Exception e){
            log.error("LogAspect.handleResult() execute error.", e);
        }
    }
}
