package com.sp.infra.comp.elasticsearch.config;

import com.sp.infra.comp.elasticsearch.utils.ElasticClientFactory;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.QueryPartams.MAX_RESULT_COUNT;

/**
 * Elasticsearch 配置
 *
 * @author alexlu
 * @date 2018-09-05 17:30:43
 */
@Configuration
@ComponentScan(basePackageClasses = ElasticClientFactory.class)
public class ElasticsearchConfig {

	/**
	 * es 集群地址，固定格式： 10.0.0.1:12001,10.0.0.1:12002,10.0.0.1:12003
	 */
	@Value("#{'${elasticSearch.hosts}'.split(',')}")
	private List<String> hosts;

	/**
	 * 最大连接数
	 */
	@Value("${elasticSearch.maxConnectNum}")
	private Integer maxConnectNum;

	/**
	 * 最大路由连接数
	 */
	@Value("${elasticSearch.maxConnectPerRoute}")
	private Integer maxConnectPerRoute;

	/**
	 * 连接超时时间
	 */
	@Value("${elasticSearch.connectTimeoutMillis:60000}")
	private int connectTimeoutMillis;

	/**
	 * 连接超时时间
	 */
	@Value("${elasticSearch.socketTimeoutMillis:60000}")
	private int socketTimeoutMillis;

	/**
	 * 获取连接的超时时间
	 */
	@Value("${elasticSearch.connectionRequestTimeoutMillis:30000}")
	private int connectionRequestTimeoutMillis;

	/**
	 * 请求超时时间
	 */
	@Value("${elasticSearch.maxRetryTimeoutMillis:30000}")
	@Getter
	private int maxRetryTimeoutMillis;

	/**
	 * 最大返回结果数据量
	 */
	@Value("${elasticSearch.maxResultCount:10000}")
	private long maxResultCount;

	/**
	 * 是否打印DSL
	 */
	@Value("${elasticSearch.showDsl:false}")
	@Getter
	private boolean showDsl;

	@Value("${elasticSearch.useMaxResultWindow:false}")
	@Getter
	private boolean useMaxResultWindow;


	public long getMaxResultCount() {
		return maxResultCount <= MAX_RESULT_COUNT ? maxResultCount : MAX_RESULT_COUNT;
	}

	public boolean getUseMaxResultWindow() {
		return useMaxResultWindow;
	}

	@Bean
	public HttpHost[] httpHost() {
		HttpHost[] httpHosts = new HttpHost[hosts.size()];
		String host;
		for (int i = 0; i < hosts.size(); i++) {
			host = hosts.get(i).contains("http://") ? hosts.get(i) : "http://" + hosts.get(i);
			httpHosts[i] = HttpHost.create(host);
		}
		return httpHosts;
	}

	/**
	 * 创建工厂
	 *
	 * @return
	 */
	@Bean(initMethod = "init", destroyMethod = "close")
	public ElasticClientFactory getFactory() {
		return ElasticClientFactory.
				build(httpHost(), connectTimeoutMillis, socketTimeoutMillis, connectionRequestTimeoutMillis, maxConnectNum, maxConnectPerRoute, maxRetryTimeoutMillis);
	}

	@Bean
	@Scope("singleton")
	public RestClient getRestClient() {
		return getFactory().getClient();
	}

	/**
	 * Client配置为单例模式，在Spring中的生命周期随着容器开始结束而开始结束。在定义bean创建和销毁方法后会自动关闭连接。
	 *
	 * @return
	 */
	@Bean
	@Scope("singleton")
	public RestHighLevelClient getRHLClient() {
		return getFactory().getRhlClient();
	}

}
