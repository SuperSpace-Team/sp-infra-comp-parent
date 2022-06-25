package com.sp.infra.comp.logger.log;

/**
 * @description: 业务日志类型定义
 * @author: luchao
 * @date: Created in 2/16/22 8:41 PM
 */
public interface ILogBizType {
    /**
     * 业务日志类型名称
     * 例如:QUERY_OM_LOGISTICS
     * @return
     */
    String getBizLogType();

    /**
     * 业务日志类型描述
     * 例如:OM订单查询
     * @return
     */
    String getBizLogDesc();
}
