package com.sp.infra.comp.feign.definitions;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * API变更标识信息
 * 存储到Consul KV的配置内容结构
 * @author luchao
 * @create 2020/8/30.
 */
@Data
@NoArgsConstructor
public class OpenApiConsulStore {

    public static final String PREFIX_KEY = "openapi/";

    /**
     * git提交记录ID
     */
    private String commitId;

    /**
     * 最后一次构建时间
     */
    private Long buildTime;

    private List<OpenApiBean> data = new ArrayList<>();
}