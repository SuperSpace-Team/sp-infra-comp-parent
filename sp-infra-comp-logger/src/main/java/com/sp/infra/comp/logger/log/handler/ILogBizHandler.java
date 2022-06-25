package com.sp.infra.comp.logger.log.handler;

import com.sp.infra.comp.logger.enums.BizDeptEnum;
import com.sp.infra.comp.logger.log.model.LogResultModel;

/**
 * @description: 业务日志处理器定义
 * @author: luchao
 * @date: Created in 2/16/22 7:16 PM
 */
public interface ILogBizHandler {
    /**
     * 处理执行结果
     * @param methodResult
     * @param logResultModel
     */
    void handle(Object methodResult, LogResultModel logResultModel);

    /**
     * 日志数据来源
     * @return
     */
    BizDeptEnum bizDeptType();
}
