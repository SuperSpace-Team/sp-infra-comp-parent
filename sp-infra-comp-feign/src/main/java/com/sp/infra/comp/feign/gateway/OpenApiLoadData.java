package com.sp.infra.comp.feign.gateway;

import com.sp.infra.comp.feign.definitions.OpenApiBean;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.TreeMap;

/**
 * 根据注解加载的配置内容
 *
 * @author luchao
 * @create 2020/8/30.
 */
@Data
public class OpenApiLoadData {

    private static OpenApiLoadData openApiData = null;

    private Map<String, OpenApiBean> data = new TreeMap<>();

    private OpenApiLoadData() {
    }

    public static OpenApiLoadData getInstance() {
        if (openApiData == null) {
            openApiData = new OpenApiLoadData();
        }
        return openApiData;
    }

    public void add(String path, RequestMethod method, String[] products, String[] authorities) {
        OpenApiBean bean = new OpenApiBean(path, method, products, authorities);
        getInstance().getData().put(bean.getMethod() + "|" + bean.getPath(), bean);
    }

}
