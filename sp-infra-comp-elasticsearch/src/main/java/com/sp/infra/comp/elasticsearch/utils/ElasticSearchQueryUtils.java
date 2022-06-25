package com.sp.infra.comp.elasticsearch.utils;

import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.elasticsearch.config.ElasticsearchConfig;
import com.sp.infra.comp.elasticsearch.dto.ElasticQueryDto;
import com.sp.infra.comp.elasticsearch.dto.ElasticQueryOperatorDto;
import com.sp.infra.comp.elasticsearch.dto.ElasticSearchDto;
import com.sp.infra.comp.elasticsearch.dto.SearchAfterResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.LogicalRelationEnum.*;
import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.QueryPartams.*;
import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.QueryPartams.INDEX;
import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.QueryTypeEnum.*;

/**
 * elastic 查询工具类
 *
 * @author alexlu on 2018/11/2
 */
@Component
@Slf4j
public class ElasticSearchQueryUtils {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	ElasticPerformRequestUtils elasticPerformRequestUtils;

	@Autowired
	ElasticsearchConfig elasticsearchConfig;

	/**
	 * 自定义查询
	 *
	 * @param searchRequest 查询对象
	 * @param needIndex     是否检查index存在
	 * @return 返回 List集合
	 */
	public List<Map<String, Object>> searchCustomList(SearchRequest searchRequest, boolean needIndex) throws Exception {
		List<Map<String, Object>> mapList = null;
		SearchResponse searchResponse = getSearchResponse(restHighLevelClient, searchRequest);
		if (searchResponse != null) {
			mapList = getResultMapList(searchResponse.getHits().getHits(), needIndex);
		}
		return mapList;
	}

	/**
	 * 自定义查询
	 *
	 * @param searchRequest 查询对象
	 * @return 返回 SearchResponse
	 */
	public SearchResponse searchCustomResponse(SearchRequest searchRequest) throws Exception {
		SearchResponse searchResponse = getSearchResponse(restHighLevelClient, searchRequest);
		if (searchResponse != null) {
			return searchResponse;
		}
		return null;
	}

	/**
	 * 字段去重查询
	 *
	 * @param queryDto 查询对象
	 * @param field    去重字段
	 * @return 返回 List集合
	 */
	public List<String> searchDistinct(ElasticQueryDto queryDto, String field) {
		ElasticSearchDto searchDto = new ElasticSearchDto(queryDto);
		return searchDistinct(searchDto, field);
	}

	/**
	 * 查询
	 *
	 * @param queryDto
	 * @return searchResponse
	 */
	public SearchResponse searchResponse(ElasticQueryDto queryDto) {
		ElasticSearchDto searchDto = new ElasticSearchDto(queryDto);
		return searchResponse(searchDto);
	}

	/**
	 * 查询
	 *
	 * @param queryDto
	 * @return List
	 */
	public List<Map<String, Object>> search(ElasticQueryDto queryDto) {
		ElasticSearchDto searchDto = new ElasticSearchDto(queryDto);
		return searchList(searchDto);
	}

	/**
	 * 统计数据量
	 *
	 * @param queryDto
	 * @return
	 */
	public Long count(ElasticQueryDto queryDto) {
		return searchResponse(queryDto).getHits().totalHits;
	}

	/**
	 * searchAfter查询
	 *
	 * @param queryDto 请求参数
	 * @return SearchAfterResult
	 */
	public SearchAfterResult searchForSearchAfter(ElasticQueryDto queryDto) {
		if (CollectionUtils.isEmpty(queryDto.getSidx())) {
			throw new SystemException(ConstantsUtils.SIDX_NOT_EXISTS);
		}
		ElasticSearchDto searchDto = new ElasticSearchDto(queryDto);
		// searchResponse
		searchResponse(searchDto);
		SearchResponse searchResponse = searchDto.getSearchResponse();
		// getResult
		if (searchResponse != null) {
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			return getSearchAfterResult(searchHits);
		}
		return null;
	}

	/**
	 * 分页查询
	 *
	 * @param queryDto
	 * @return SpPageUtils
	 */
	public SpPageBase searchPage(ElasticQueryDto queryDto) {
		if (queryDto.getQueryPageDto() == null) {
			throw new SystemException("QueryPageDto参数不能为空");
		}
		ElasticSearchDto searchDto = new ElasticSearchDto(queryDto);
		searchList(searchDto);
		long totalHits = 0;
		if (searchDto.getSearchResponse() != null) {
			totalHits = searchDto.getSearchResponse().getHits().getTotalHits();
		}
		/**
		 * 如果不采用ES原生的最大值，则用配置文件中的最大值
		 */
		if(!elasticsearchConfig.getUseMaxResultWindow()){
			totalHits = totalHits <= elasticsearchConfig.getMaxResultCount() ? totalHits : elasticsearchConfig.getMaxResultCount();
		}
		SpPageBase page = new SpPageBase(searchList(searchDto), totalHits, searchDto.getQueryDto().getQueryPageDto().getLimit(), searchDto.getQueryDto().getQueryPageDto().getCurrPage());
		return page;
	}

	/**
	 * 根据ID查询数据
	 *
	 * @param indexName index
	 * @param id        id
	 * @return map
	 */
	public Map<String, Object> queryById(String indexName, String id) {
		Map<String, Object> map = new HashMap();
		try {
			GetRequest getRequest = new GetRequest(indexName, indexName, id);
			GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
			map = response.getSourceAsMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 统计索引中有多少数据
	 *
	 * @param indexName index
	 * @return 数据
	 */
	public long countIndexDoc(String indexName) {
		long totalHits = 0L;
		if (!elasticPerformRequestUtils.indexExist(indexName)) {
			return totalHits;
		}
		try {
			SearchRequest searchRequest = new SearchRequest(indexName);
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			SearchHits hits = searchResponse.getHits();
			totalHits = hits.totalHits;
			if (elasticsearchConfig.isShowDsl()) {
				log.info("DSL：" + searchRequest.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalHits;
	}

	/**
	 * 字段去重查询
	 *
	 * @param searchDto 查询对象
	 * @param field     去重字段
	 * @return List
	 */
	private List<String> searchDistinct(ElasticSearchDto searchDto, String field) {
		ElasticQueryDto queryDto = searchDto.getQueryDto();
		searchDto.setQueryDto(queryDto);
		checkAndSeIndicesList(searchDto.getQueryDto().isCheckIndex(), searchDto.getQueryDto().getIndices());
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(buildMustExist(field));
		searchDto.setBoolQuery(boolQuery);
		searchDto.setCollapse(collapse(field));
		boolean isError = false;
		try {
			SearchRequest searchRequest = querySearchRequest(searchDto);
			return queryResultStringByList(restHighLevelClient, searchRequest, field);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ConstantsUtils.SEARCH_ERROE_MESSAGE, e);
			isError = true;
		}
		if (isError) {
			throw new SystemException(ConstantsUtils.SEARCH_ERROE_MESSAGE);
		}
		return null;
	}

	/**
	 * 查询
	 *
	 * @param searchDto 查询对象
	 * @return List
	 */
	private List<Map<String, Object>> searchList(ElasticSearchDto searchDto) {
		searchResponse(searchDto);
		return queryResultByList(searchDto);
	}

	/**
	 * 查询
	 *
	 * @param searchDto 查询对象
	 * @return SearchRequest
	 */
	private SearchResponse searchResponse(ElasticSearchDto searchDto) {
		setSearchDto(searchDto);
		SearchRequest searchRequest = querySearchRequest(searchDto);
		queryResultByResponse(searchDto, restHighLevelClient, searchRequest);
		return searchDto.getSearchResponse();
	}

	/**
	 * 设置查询对象 并检查处理
	 */
	private void setSearchDto(ElasticSearchDto searchDto) {
		checkAndSeIndicesList(searchDto.getQueryDto().isCheckIndex(), searchDto.getQueryDto().getIndices());
		setBoolQuery(searchDto);
		setSort(searchDto);
	}

	/**
	 * 组合查询BoolQueryBuilder
	 */
	private void setBoolQuery(ElasticSearchDto searchDto) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery = setBoolQueryByOther(searchDto, boolQuery);
		for (BoolQueryBuilder builder : searchDto.getBuildBoolQueryByShouldList()) {
			boolQuery.must(builder);
		}
		searchDto.setBoolQuery(boolQuery);
	}

	/**
	 * 多条件查询
	 *
	 * @param searchDto
	 */
	private BoolQueryBuilder setBoolQueryByOther(ElasticSearchDto searchDto, BoolQueryBuilder boolQuery) {
		if (searchDto.getQueryDto().getOperators() == null) {
			return boolQuery;
		}
		for (ElasticQueryOperatorDto operatorDto : searchDto.getQueryDto().getOperators()) {
			buildBoolQuery(searchDto, boolQuery, operatorDto);
		}
		searchDto.setBoolQuery(boolQuery);
		return boolQuery;
	}

	/**
	 * 判断index是否存在，去除不存在的index
	 *
	 * @param indices
	 */
	private void checkAndSeIndicesList(boolean checkIndex, String[] indices) {
		if (checkIndex) {
			try {
				for (String indexName : indices) {
					if (!elasticPerformRequestUtils.indexExist(indexName)) {
						throw new SystemException(ConstantsUtils.INDEX_NOT_EXISTS + ":" + indexName);
					}
				}
			} catch (Exception e) {
				log.error(ConstantsUtils.SEARCH_ERROE_MESSAGE, e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置排序
	 *
	 * @param searchDto
	 */
	private void setSort(ElasticSearchDto searchDto) {
		if (searchDto.getQueryDto().getSidx() != null) {
			List<FieldSortBuilder> listSort = new ArrayList<>();
			for (Map.Entry<String, String> sidx : searchDto.getQueryDto().getSidx().entrySet()) {
				if (ASC.equals(sidx.getValue())) {
					listSort.add(new FieldSortBuilder(sidx.getKey()).order(SortOrder.ASC));
				} else if (DESC.equals(sidx.getValue())) {
					listSort.add(new FieldSortBuilder(sidx.getKey()).order(SortOrder.DESC));
				}
			}
			searchDto.setListSort(listSort);
		}
	}

	/**
	 * SearchRequest高级查询
	 */
	private SearchRequest querySearchRequest(ElasticSearchDto searchDto) {
		// 查询的Index
		SearchRequest searchRequest = new SearchRequest(searchDto.getQueryDto().getIndices());
		// 生成DSL查询语句
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 查询的字段，排除的字段
		searchSourceBuilder.fetchSource(searchDto.getQueryDto().getIncludeFields(), searchDto.getQueryDto().getExcludeFields());
		// 查询条件
		searchSourceBuilder.query(searchDto.getBoolQuery());
		// 聚合统计
		if (searchDto.getAggregationBuilder() != null) {
			searchSourceBuilder.aggregation(searchDto.getAggregationBuilder());
		}
		// 折叠
		if (searchDto.getCollapse() != null) {
			searchSourceBuilder.collapse(searchDto.getCollapse());
		}
		// 排序
		if (searchDto.getListSort() != null) {
			for (FieldSortBuilder sort : searchDto.getListSort()) {
				searchSourceBuilder.sort(sort);
			}
		}
		// searchAfter
		if (!ArrayUtils.isEmpty(searchDto.getQueryDto().getSearchAfter())) {
			searchSourceBuilder.searchAfter(searchDto.getQueryDto().getSearchAfter());
		}
		// 分页 查询起始位置,每页数量
		if (searchDto.getQueryDto().getQueryPageDto() != null) {
			searchSourceBuilder.from(searchDto.getQueryDto().getQueryPageDto().getOffset());
			searchSourceBuilder.size(searchDto.getQueryDto().getQueryPageDto().getLimit());
		}
		// 查询超时时间
		//searchSourceBuilder.timeout(new TimeValue(elasticsearchConfig.getMaxRetryTimeoutMillis(), TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		if (elasticsearchConfig.isShowDsl()) {
			log.info("DSL：" + searchRequest.toString());
		}
		return searchRequest;
	}

	/**
	 * 查询结果处理
	 *
	 * @param searchDto
	 */
	private List<Map<String, Object>> queryResultByList(ElasticSearchDto searchDto) {
		SearchResponse searchResponse = searchDto.getSearchResponse();
		if (searchResponse != null) {
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			boolean needIndex = false;
			if (Arrays.asList(searchDto.getQueryDto().getIncludeFields()).contains(INDEX)) {
				needIndex = true;
			}
			return getResultMapList(searchHits, needIndex);
		}
		return null;
	}

	/**
	 * 查询结果处理
	 * 最多处理10000条数据
	 *
	 * @param searchDto
	 * @param client
	 * @param searchRequest
	 * @return 返回 分页对象
	 */
	private void queryResultByResponse(ElasticSearchDto searchDto, RestHighLevelClient client, SearchRequest searchRequest) {
		try {
			SearchResponse searchResponse = getSearchResponse(client, searchRequest);
			if (searchResponse != null) {
				RestStatus status = searchResponse.status();
				if (status == null || status.getStatus() != RestStatus.OK.getStatus()) {
					throw new SystemException(ConstantsUtils.SEARCH_ERROE_MESSAGE + status.getStatus());
				} else {
					searchDto.setSearchResponse(searchResponse);
				}
			}
		} catch (Exception e) {
			log.error(ConstantsUtils.SEARCH_ERROE_MESSAGE, e);
			throw new SystemException(ConstantsUtils.SEARCH_ERROE_MESSAGE);
		}
	}

	/**
	 * 查询结果处理
	 *
	 * @param client
	 * @param searchRequest
	 * @return 返回 List集合
	 */
	private List<String> queryResultStringByList(RestHighLevelClient client, SearchRequest searchRequest, String columnName) throws Exception {
		List<String> list = null;
		SearchResponse searchResponse = getSearchResponse(client, searchRequest);
		if (searchResponse != null) {
			list = getResultStringList(searchResponse.getHits().getHits(), columnName);
		}
		return list;
	}

	private SearchResponse getSearchResponse(RestHighLevelClient client, SearchRequest searchRequest) throws Exception {
		SearchResponse searchResponse = client.search(searchRequest);
		if (searchResponse.status() != null && searchResponse.status().getStatus() == RestStatus.OK.getStatus()) {
			return searchResponse;
		}
		return null;
	}

	private List<Map<String, Object>> getResultMapList(SearchHit[] searchHits, boolean needIndex) {
		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> indexValue;
		if (!needIndex) {
			for (SearchHit hit : searchHits) {
				indexValue = hit.getSourceAsMap();
				mapList.add(indexValue);
			}
		} else {
			for (SearchHit hit : searchHits) {
				indexValue = hit.getSourceAsMap();
				indexValue.put(INDEX, hit.getIndex());
				mapList.add(indexValue);
			}
		}
		return mapList;
	}

	private SearchAfterResult getSearchAfterResult(SearchHit[] searchHits) {
		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> indexValue;

		for (SearchHit hit : searchHits) {
			indexValue = hit.getSourceAsMap();
			mapList.add(indexValue);
		}
		if (searchHits.length != 0) {
			String searchAfter = searchHits[searchHits.length - 1].getId();
			return new SearchAfterResult(searchAfter, mapList);
		}

		return new SearchAfterResult("", Collections.emptyList());

	}

	private List<String> getResultStringList(SearchHit[] searchHits, String columnName) {
		List<String> list = new ArrayList<>();
		for (SearchHit hit : searchHits) {
			list.add(hit.getSourceAsMap().get(columnName).toString());
		}
		return list;
	}

	public void buildBoolQuery(ElasticSearchDto searchDto, BoolQueryBuilder boolQuery, ElasticQueryOperatorDto operatorDto) {
		if (TERM_QUERY.getValue().equals(operatorDto.getOperate())) {
			// 精确匹配字段查询
			boolQuery.must(buildTermQuery(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (IN_TERMS.getValue().equals(operatorDto.getOperate())) {
			// 精确匹配字段查询(多term查询); 查询一个字段对应多个词条
			boolQuery.must(buildTermsQuery(operatorDto.getKey(), (String[]) operatorDto.getValue()));
		} else if (QUERY_STRING_QUERY.getValue().equals(operatorDto.getOperate())) {
			// 解析查询字符串 左右模糊查询
			boolQuery.must(buildQueryStringQuery(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (RANGE_QUERY.getValue().equals(operatorDto.getOperate())) {
			// 当前字段值范围查询
			boolQuery.must(buildRangeQuery(operatorDto));
		} else if (WILDCARD_QUERY.getValue().equals(operatorDto.getOperate())) {
			// 模糊查询
			boolQuery.must(buildWildcardQuery(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (GT.getValue().equals(operatorDto.getOperate())) {
			// 大于
			boolQuery.must(buildGt(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (GTE.getValue().equals(operatorDto.getOperate())) {
			// 大于等于
			boolQuery.must(buildGte(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (LT.getValue().equals(operatorDto.getOperate())) {
			// 小于
			boolQuery.must(buildLt(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (LTE.getValue().equals(operatorDto.getOperate())) {
			// 小于等于
			boolQuery.must(buildLte(operatorDto.getKey(), (String) operatorDto.getValue()));
		} else if (SHOULD_IN_TERM.getValue().equals(operatorDto.getOperate())) {
			// IN term 查询
			searchDto.getBuildBoolQueryByShouldList().add(buildShouldInTerm(operatorDto.getKey(), (String[]) operatorDto.getValue()));
		} else if (SHOULD_IN_WILDCARD.getValue().equals(operatorDto.getOperate())) {
			// IN wildcard 查询,多个值逗号分割
			searchDto.getBuildBoolQueryByShouldList().add(buildShouldInWildcard(operatorDto.getKey(), (String[]) operatorDto.getValue()));
		} else if (MUST_NOT_TERM.getValue().equals(operatorDto.getOperate())) {
			// 非查询 not in,多个值逗号分割
			searchDto.getBuildBoolQueryByShouldList().add(buildMustNotByTerm(operatorDto.getKey(), (String[]) operatorDto.getValue()));
		} else if (MUST_NOT_WILDCARD.getValue().equals(operatorDto.getOperate())) {
			// 非查询 not in,多个值逗号分割
			searchDto.getBuildBoolQueryByShouldList().add(buildMustNotByWildcard(operatorDto.getKey(), (String[]) operatorDto.getValue()));
		} else if (QUERY_STRING_MULTI_QUERY.getValue().equals(operatorDto.getOperate())) {
			// 非查询 not in,多个值逗号分割
			boolQuery.must(buildMultiTermQuery(operatorDto.getValue(),operatorDto.getKey()));
		}
	}

	/**
	 * 准确查询；指定查询一个字段对应单个词条
	 * matchQuery：会将搜索词分词，再与目标查询字段进行匹配，若分词中的任意一个词与目标字段匹配上，则可查询到。
	 * termQuery：不会对搜索词进行分词处理，而是作为一个整体与目标字段进行匹配，若完全匹配，则可查询到。
	 * <p>
	 * 构造 列 key 值为 value  查询条件
	 *
	 * @return
	 */
	private QueryBuilder buildTermQuery(String key, String value) {
		return QueryBuilders.termQuery(key, value);
	}

	/**
	 * add by daniel  2020/1/15
	 * 查询为能在多个字段上反复执行相同查询提供了一种便捷方式
	 * @param value 查询值
	 * @param key 查询的多字段,以逗号隔开
	 * @return
	 */
	 private QueryBuilder buildMultiTermQuery( Object value,String key) {
		 String[] keys = StringUtils.split(key,",");
		 return QueryBuilders.multiMatchQuery(value, keys);
	 }

	/**
	 * 准确查询(多term查询)；查询一个字段对应多个词条
	 * <p>
	 * 构造 列 key 值为 value  查询条件
	 *
	 * @return
	 */
	private QueryBuilder buildTermsQuery(String key, String... values) {
		return QueryBuilders.termsQuery(key, values);
	}

	/**
	 * 模糊查询，匹配列key，包含value的值，
	 * <p>
	 * 支持通配符匹配
	 *
	 * @return
	 */
	private QueryBuilder buildWildcardQuery(String key, String value) {
		return QueryBuilders.wildcardQuery(key, "*" + value + "*");
	}

	/**
	 * 解析查询，匹配列key，包含value的值，
	 * <p>
	 * 支持通配符匹配
	 *
	 * @return
	 */
	private QueryBuilder buildQueryStringQuery(String key, String value) {
		return QueryBuilders.queryStringQuery(value).field(key);
	}

	/**
	 * should 相当于 in
	 * <p>
	 * 多条件查询
	 *
	 * @return
	 */
	private BoolQueryBuilder buildShouldInTerm(String key, String... values) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.should(buildTermsQuery(key, values));
		return boolQuery;
	}

	/**
	 * should 相当于 in
	 * <p>
	 * 多条件查询
	 *
	 * @return
	 */
	private BoolQueryBuilder buildShouldInWildcard(String key, String... values) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (String value : values) {
			boolQuery.should(buildWildcardQuery(key, value));
		}
		return boolQuery;
	}

	/**
	 * 非查询 not in
	 * <p>
	 * 构造  列 key 值不为 value  查询条件
	 *
	 * @return
	 */
	private BoolQueryBuilder buildMustNotByTerm(String key, String... values) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (String value : values) {
			boolQuery.mustNot(buildTermsQuery(key, value));
		}
		return boolQuery;
	}

	/**
	 * 非查询 not in
	 * <p>
	 * 构造  列 key 值不为 value  查询条件
	 *
	 * @return
	 */
	private BoolQueryBuilder buildMustNotByWildcard(String key, String... values) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (String value : values) {
			boolQuery.mustNot(buildWildcardQuery(key, value));
		}
		return boolQuery;
	}

	/**
	 * 范围查询
	 * 当前字段范围查询， 默认包含上下界
	 *
	 * @return
	 */
	private QueryBuilder buildRangeQuery(ElasticQueryOperatorDto operatorDto) {
		// 下界 >=
		boolean minflag = true;
		// 上界 <=
		boolean maxflag = true;
		if (operatorDto.getOperate() != null && operatorDto.getOperateByRight() != null && GT.getValue().equals(operatorDto.getOperateByRight())) {
			// >
			minflag = false;
		}
		if (operatorDto.getOperate() != null && operatorDto.getOperateByRight() != null && LT.getValue().equals(operatorDto.getOperateByRight())) {
			// <
			maxflag = false;
		}
		return buildRangeQuery(operatorDto.getKey(), operatorDto.getValue(), operatorDto.getValueByRight(), minflag, maxflag);
	}

	/**
	 * 区间查询
	 */
	private QueryBuilder buildRangeQuery(String key, Object valueFrom, Object valueTo, boolean minflag, boolean maxflag) {
		return QueryBuilders.rangeQuery(key).from(valueFrom).to(valueTo).includeLower(minflag).includeUpper(maxflag);
	}

	/**
	 * 大于
	 */
	private QueryBuilder buildGt(String key, String value) {
		return QueryBuilders.rangeQuery(key).gt(value);
	}

	/**
	 * 大于等于
	 *
	 * @return
	 */
	private QueryBuilder buildGte(String key, String value) {
		return QueryBuilders.rangeQuery(key).gte(value);
	}

	/**
	 * 小于
	 */
	private QueryBuilder buildLt(String key, String value) {
		return QueryBuilders.rangeQuery(key).lt(value);
	}

	/**
	 * 小于等于
	 */
	private QueryBuilder buildLte(String key, String value) {
		return QueryBuilders.rangeQuery(key).lte(value);
	}

	/**
	 * 查询字段不为null的文档
	 */
	private QueryBuilder buildMustExist(String key) {
		return QueryBuilders.existsQuery(key);
	}

	/**
	 * 查询字段为null的文档
	 */
	private QueryBuilder buildMustNotExist(BoolQueryBuilder boolQuery, String key) {
		return boolQuery.mustNot(buildMustExist(key));
	}

	/**
	 * 去重统计某个字段的数（字段去重）
	 * 如果你对去重结果的精准度没有特殊要求，使用cardinality聚合函数
	 * <p>
	 * 优点：性能快，亿级别的记录在1秒内完成
	 * 缺点：存在只能保证最大40000条记录内的精确，超过的存在5%的误差，不适合需要精确去重场景
	 *
	 * @param aliasName 别名
	 * @param name      字段名
	 * @param precision 精度范围 自定义一个精度范围100-40000
	 * @return
	 */
	private CardinalityAggregationBuilder aggregationBuildersCardinality(String aliasName, String name, long precision) {
		return AggregationBuilders.cardinality(aliasName).field(name).precisionThreshold(precision);
	}

	/**
	 * 按某个字段分（去重复）
	 * 如果你对去重结果要求精确，使用termsagg聚合（类似group by）
	 * 在很多情况下，需要聚合，但搜索结果不需要。对于这些情况，可以通过设置size=0来忽略命中,设置size为0避免执行搜索的获取阶段，使请求更有效。
	 * <p>
	 * 说明：默认只聚合10个桶，size(Integer.MAX_VALUE)可以指定桶个数
	 * 优点：结果精确
	 * 缺点：只适合聚合少量桶场景（100以内），否则性能极差（十万级的桶需要分钟级完成）
	 *
	 * @param aliasName 别名
	 * @param name      字段名
	 * @param size      精度范围 自定义一个精度范围100-40000
	 * @return
	 */
	private TermsAggregationBuilder aggregationBuildersTerms(String aliasName, String name, int size) {
		return AggregationBuilders.terms(aliasName).field(name).size(size);
	}

	/**
	 * 折叠去重，类似MySQL中的distinct操作
	 *
	 * @param field
	 * @return
	 */
	private CollapseBuilder collapse(String field) {
		return new CollapseBuilder(field);
	}

}
