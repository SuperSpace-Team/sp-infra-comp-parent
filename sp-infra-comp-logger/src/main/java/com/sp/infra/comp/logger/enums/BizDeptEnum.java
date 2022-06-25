package com.sp.infra.comp.logger.enums;

import com.sp.framework.common.base.BaseBizEnum;

/**
 * @description: 业务部门枚举
 * @author: luchao
 * @date: Created in 2/16/22 7:18 PM
 */
public enum BizDeptEnum implements BaseBizEnum {
    COMMON(0, "COMMON", "公共系统"),
    OTB(1, "OTB","OTB团队系统"),
    WMS(2, "WMS","WMS团队系统"),
    RED_GRASSLAND(3, "WMS","WMS团队系统"),
    MANAGE_DIGITAL(4, "MANAGE_DIGITAL","管理数字化系统")
    ;

    /**
     * 业务部门编码
     */
    private Integer code;

    /**
     * 适配类型名称
     */
    private String msg;

    /**
     * 适配类型名称
     */
    private String description;

    BizDeptEnum(Integer code, String msg, String description){
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
