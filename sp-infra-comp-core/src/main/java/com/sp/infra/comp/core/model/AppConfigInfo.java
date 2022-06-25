package com.sp.infra.comp.core.model;

import lombok.Data;

/**
 * @description: 应用/服务认证配置信息
 * @author: luchao
 * @date: Created in 8/6/21 8:54 PM
 */
@Data
public class AppConfigInfo {
    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用Key
     */
    private String appSecret;

    /**
     * 应用描述
     */
    private String description;

    public AppConfigInfo(String appCode, String appKey) {
        this.appCode = appCode;
        this.appKey = appKey;
    }
}
