package com.sp.infra.comp.logger.log.handler;

import com.sp.framework.common.enums.SystemErrorCodeEnum;
import com.sp.framework.common.vo.ResponseVO;
import com.sp.infra.comp.logger.enums.BizDeptEnum;
import com.sp.infra.comp.logger.log.model.LogResultModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @description: 默认业务日志处理实现类
 * @author: luchao
 * @date: Created in 2/16/22 7:34 PM
 */
@Component
public class DefaultLogBizHandler implements ILogBizHandler {
    @Override
    public void handle(Object methodResult, LogResultModel logResultModel) {
        //若拦截器已前置处理错误，则返回
        if(logResultModel.getSuccess() != null && StringUtils.isNotBlank(logResultModel.getErrorCode())){
            return;
        }

        if(methodResult instanceof ResponseVO){
            int code = ((ResponseVO<?>) methodResult).getCode();
            logResultModel.setSuccess(SystemErrorCodeEnum.SUCCESS.equals(code));
            logResultModel.setErrorCode(String.valueOf(code));
            logResultModel.setErrorMsg(((ResponseVO<?>) methodResult).getMsg());
        }
    }

    @Override
    public BizDeptEnum bizDeptType() {
        return BizDeptEnum.COMMON;
    }
}
