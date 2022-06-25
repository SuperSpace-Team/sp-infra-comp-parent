package com.sp.infra.comp.core.enums;

import com.sp.framework.common.base.BaseBizEnum;

/**
 * @description: 适配类型枚举
 * @author: luchao
 * @date: Created in 11/1/21 3:42 PM
 */
public enum AdaptTypeEnum implements BaseBizEnum {
    WebResult(1, "原云创返回类型WebResult"),
    R(2, "原云创返回类型R");

    /**
     * 适配类型编码
     */
    private Integer code;

    /**
     * 适配类型名称
     */
    private String msg;

    AdaptTypeEnum(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
