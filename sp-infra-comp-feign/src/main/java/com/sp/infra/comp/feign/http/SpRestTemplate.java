package com.sp.infra.comp.feign.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 自定义RestTemplate
 * @author luchao
 * @create 2021/3/23.
 */
public class SpRestTemplate extends RestTemplate {

    public void execute(String url, HttpMethod method, HttpEntity<?> requestEntity, Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(requestEntity);
        execute(url, method, requestCallback, null, uriVariables);
    }

    public void execute(String url, HttpMethod method, HttpEntity<?> requestEntity, Map<String, ?> uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(requestEntity);
        execute(url, method, requestCallback, null, uriVariables);
    }

    public void execute(String url, HttpMethod method, Object request, Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request);
        execute(url, method, requestCallback, null, uriVariables);
    }

    public void execute(String url, HttpMethod method, Object request, Map<String, ?> uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request);
        execute(url, method, requestCallback, null, uriVariables);
    }

}