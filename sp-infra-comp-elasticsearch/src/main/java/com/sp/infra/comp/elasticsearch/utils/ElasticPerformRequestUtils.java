package com.sp.infra.comp.elasticsearch.utils;

import com.alibaba.fastjson.JSONObject;
import com.sp.infra.comp.elasticsearch.dto.ElasticPerformRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;



/**
 * 执行http请求 Utils
 *
 * @author alexlu on 2018/11/2
 */
@Component
@Slf4j
public class ElasticPerformRequestUtils {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	private RestClient restClient;

	/**
	 * 验证索引是否存在。
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean indexExist(String indexName) {
		boolean exist = false;
		try {
			GetIndexRequest getIndexRequest = new GetIndexRequest();
			getIndexRequest.indices(indexName);
			exist = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("验证索引是否存在时出现异常", e);
			e.printStackTrace();
		}
		return exist;
	}

	/**
	 * 修改索引副本数
	 *
	 * @param indexName
	 * @param number
	 * @return
	 */
	public boolean setNumberOfReplicas(String indexName, Integer number) {
		int numberOfReplicas = number == null || number < 0 ? 0 : number;
		ElasticPerformRequestDto performRequestDto = new ElasticPerformRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("number_of_replicas", numberOfReplicas);
		performRequestDto.setApplicationJson(jsonObject);
		performRequestDto.setMethod(ConstantsUtils.PUT);
		performRequestDto.setEndpoint(indexName + "*/_settings");
		return doRequest(performRequestDto);
	}

	/**
	 * 删除Index模版
	 *
	 * @param templateName
	 * @return
	 */
	public boolean deleteTemplate(String templateName) {
		ElasticPerformRequestDto performRequestDto = new ElasticPerformRequestDto();
		performRequestDto.setMethod(ConstantsUtils.DELETE);
		performRequestDto.setEndpoint("_template/" + templateName);
		return doRequest(performRequestDto);
	}

	private boolean doRequest(ElasticPerformRequestDto performRequestDto) {
		boolean result = false;
		try {
			result = performRequestResult(restClient, performRequestDto);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ConstantsUtils.SEARCH_ERROE_MESSAGE, e);
		}
		if (result) {
			return true;
		}
		return false;
	}

	public static boolean performRequestResult(RestClient restClient, ElasticPerformRequestDto performRequestDto) throws IOException {
		Response response = getResponseByperformRequest(restClient, performRequestDto);
		if (response == null) {
			return false;
		}
		return ConstantsUtils.OK.equals(response.getStatusLine().getReasonPhrase());
	}

	public static Response getResponseByperformRequest(RestClient restClient, ElasticPerformRequestDto performRequestDto) throws IOException {
		Response response;
		if (performRequestDto.getApplicationJson() == null) {
			response = restClient.performRequest(performRequestDto.getMethod(), performRequestDto.getEndpoint());
		} else {
			HttpEntity httpEntity = new NStringEntity(performRequestDto.getApplicationJson().toJSONString(), ContentType.APPLICATION_JSON);
			response = restClient.performRequest(performRequestDto.getMethod(), performRequestDto.getEndpoint(), performRequestDto.getParams(), httpEntity);
		}
		return response;
	}

}
