package com.sp.infra.comp.elasticsearch.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.http.Header;

import java.util.Collections;
import java.util.Map;

/**
 * es http 请求参数
 *
 * @author alexlu on 2018/8/24
 */
@Data
public class ElasticPerformRequestDto {
	String method;
	String endpoint;
	Map<String, String> params = Collections.emptyMap();
	Header[] headers;
	JSONObject applicationJson;
}
