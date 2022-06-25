package com.sp.infra.comp.logger.log.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 日志结果信息
 * @author: luchao
 * @date: Created in 2/16/22 7:30 PM
 */
@Data
@ToString
public class LogResultModel implements Serializable {
    /**
     * 业务类型
     */
    private String logType;
    /**
     * 方法名
     */
    private String method;

    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 耗时
     */
    private long cost;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误日志
     */
    private String errorMsg;

    /**
     * 请求
     */
    private String request;

    /**
     * 响应
     */
    private String response;

    /**
     * 扩展信息
     */
    private List<String> extLogInfos;

    /**
     * 部门
     */
    private String depart;
}
