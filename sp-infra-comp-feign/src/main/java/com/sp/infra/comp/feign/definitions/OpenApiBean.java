package com.sp.infra.comp.feign.definitions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * API接口鉴权信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiBean {
    /**
     * API URI
     */
    private String path;

    /**
     * API请求方式
     */
    private RequestMethod method;

    /**
     * API所属产品权限
     */
    private String[] products;

    /**
     * API配置拥有权限的权限字符串
     */
    private String[] authorities;
}