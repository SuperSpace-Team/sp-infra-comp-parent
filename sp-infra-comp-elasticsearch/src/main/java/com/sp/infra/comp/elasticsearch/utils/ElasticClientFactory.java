package com.sp.infra.comp.elasticsearch.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * es client 连接
 * 对client进行配置，Client配置为单例模式，在Spring中的生命周期随着容器开始结束而开始结束。在定义bean创建和销毁方法后会自动关闭连接。
 *
 * @author alexlu on 2018/8/14
 */
@Slf4j
public class ElasticClientFactory {

	public static int CONNECT_TIMEOUT_MILLIS;
	public static int SOCKET_TIMEOUT_MILLIS;
	public static int CONNECTION_REQUEST_TIMEOUT_MILLIS;
	public static int MAX_CONN_PER_ROUTE;
	public static int MAX_CONN_TOTAL;
	public static int MAX_RETRY_TIMEOUT_MILLIS;

	/**
	 * es集群地址 xxx:xx,xxx:xx
	 */
	private static HttpHost[] HTTP_HOSTS;
	private RestClientBuilder builder;
	/**
	 * 低级客户端
	 */
	private RestClient restClient;
	/**
	 * 高级客户端
	 */
	private RestHighLevelClient restHighLevelClient;

	private static ElasticClientFactory elasticClientFactory = new ElasticClientFactory();

	private ElasticClientFactory() {
	}

	public static ElasticClientFactory build(HttpHost[] httpHosts, Integer connectTimeOut, Integer socketTimeOut,
	                                         Integer connectionRequestTime, Integer maxConnectNum, Integer maxConnectPerRoute, Integer maxRetryTimeoutMillis) {
		HTTP_HOSTS = httpHosts;
		CONNECT_TIMEOUT_MILLIS = connectTimeOut;
		SOCKET_TIMEOUT_MILLIS = socketTimeOut;
		CONNECTION_REQUEST_TIMEOUT_MILLIS = connectionRequestTime;
		MAX_CONN_TOTAL = maxConnectNum;
		MAX_CONN_PER_ROUTE = maxConnectPerRoute;
		MAX_RETRY_TIMEOUT_MILLIS = maxRetryTimeoutMillis;
		return elasticClientFactory;
	}

	/**
	 * 构建客户端
	 */
	public void init() {
		builder = RestClient.builder(HTTP_HOSTS).setMaxRetryTimeoutMillis(MAX_RETRY_TIMEOUT_MILLIS);
		setConnectTimeOutConfig();
		setMutiConnectConfig();
		// 低级客户端
		restClient = builder.build();
		// 高级客户端
		restHighLevelClient = new RestHighLevelClient(builder);
	}

	/**
	 * 配置连接时间延时
	 */
	public void setConnectTimeOutConfig() {
		builder.setRequestConfigCallback(requestConfigBuilder -> {
			requestConfigBuilder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
			requestConfigBuilder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
			requestConfigBuilder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
			return requestConfigBuilder;
		});
	}

	/**
	 * 使用异步httpclient时设置并发连接数
	 */
	public void setMutiConnectConfig() {
		builder.setHttpClientConfigCallback(httpClientBuilder -> {
			httpClientBuilder.setMaxConnTotal(MAX_CONN_TOTAL);
			httpClientBuilder.setMaxConnPerRoute(MAX_CONN_PER_ROUTE);
			return httpClientBuilder;
		});
	}

	/**
	 * 构建低级客户端
	 *
	 * @return
	 */
	public RestClient getClient() {
		return restClient;
	}

	/**
	 * RestHighLevelClient实例需要低级客户端构建器来构建
	 * 高级客户端将在内部创建低级客户端，用来执行基于提供的构建器的请求，并管理其生命周期。
	 *
	 * @return
	 */
	public RestHighLevelClient getRhlClient() {
		return restHighLevelClient;
	}

	/**
	 * 关闭RestClient，以便它使用的所有资源得到正确释放，以及底层的http客户端实例及其线程
	 */
	public void close() {
		if (restClient != null) {
			try {
				restClient.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error("关闭RestClient时出现错误", e);
			}
		}
	}

}
