package com.sp.infra.comp.elasticsearch.utils;

import com.sp.infra.comp.elasticsearch.dto.ElasticQueryDto;
import com.sp.infra.comp.elasticsearch.dto.SearchAfterResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * SearchRequest 工具类
 *
 * @author alexlu on 2018/11/2
 */
@Component
@Slf4j
public class ElasticSearchRequestUtils {


	@Autowired
	private ElasticPerformRequestUtils elasticPerformRequestUtils;

	@Autowired
	private ElasticSearchOperationUtils elasticSearchOperationUtils;

	@Autowired
	private ElasticSearchQueryUtils elasticSearchQueryUtils;

	/**
	 * 查询
	 *
	 * @param queryDto
	 * @return List
	 */
	public List<Map<String, Object>> search(ElasticQueryDto queryDto) {
		return elasticSearchQueryUtils.search(queryDto);
	}

	/**
	 * 自定义查询
	 *
	 * @param searchRequest 查询对象
	 * @param needIndex     是否检查index存在
	 * @return 返回 List集合
	 */
	public List<Map<String, Object>> searchCustomList(SearchRequest searchRequest, boolean needIndex) throws Exception {
		return elasticSearchQueryUtils.searchCustomList(searchRequest, needIndex);
	}

	/**
	 * 自定义查询
	 *
	 * @param searchRequest 查询对象
	 * @return 返回 SearchResponse
	 */
	public SearchResponse searchCustomResponse(SearchRequest searchRequest) throws Exception {
		return elasticSearchQueryUtils.searchCustomResponse(searchRequest);
	}

	/**
	 * 字段去重查询
	 *
	 * @param queryDto 查询对象
	 * @param field    去重字段
	 * @return 返回 List集合
	 */
	public List<String> searchDistinct(ElasticQueryDto queryDto, String field) {
		return elasticSearchQueryUtils.searchDistinct(queryDto, field);
	}

	/**
	 * 查询
	 *
	 * @param queryDto
	 * @return searchResponse
	 */
	public SearchResponse searchResponse(ElasticQueryDto queryDto) {
		return elasticSearchQueryUtils.searchResponse(queryDto);
	}

	/**
	 * searchAfter查询
	 *
	 * @param queryDto 请求参数
	 * @return SearchAfterResult
	 */
	public SearchAfterResult searchForSearchAfter(ElasticQueryDto queryDto) {
		return elasticSearchQueryUtils.searchForSearchAfter(queryDto);
	}

	/**
	 * 分页查询
	 *
	 * @param queryDto
	 * @return SpPageUtils
	 */
	public SpPageBase searchPage(ElasticQueryDto queryDto) {
		return elasticSearchQueryUtils.searchPage(queryDto);
	}

	/**
	 * 根据ID查询数据
	 *
	 * @param indexName index
	 * @param id        id
	 * @return map
	 */
	public Map<String, Object> findDateById(String indexName, String id) {
		return elasticSearchQueryUtils.queryById(indexName, id);
	}

	/**
	 * 统计数据量
	 *
	 * @param queryDto
	 * @return 查询数据量
	 */
	public Long count(ElasticQueryDto queryDto) {
		return elasticSearchQueryUtils.count(queryDto);
	}

	/**
	 * 统计索引中有多少数据
	 *
	 * @param indexName index
	 * @return 数据
	 */
	public long countIndexDoc(String indexName) {
		return elasticSearchQueryUtils.countIndexDoc(indexName);
	}

	/**
	 * 创建索引
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean createIndex(String indexName) {
		return elasticSearchOperationUtils.createIndex(indexName);
	}

	/**
	 * 创建索引(自定义配置)
	 *
	 * @param createIndexRequest
	 * @return 是否成功
	 */
	public boolean createIndex(CreateIndexRequest createIndexRequest) {
		return elasticSearchOperationUtils.createIndex(createIndexRequest);
	}

	/**
	 * 验证索引是否存在。
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean indexExist(String indexName) {
		return elasticPerformRequestUtils.indexExist(indexName);
	}

	/**
	 * 删除index
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean deleteIndex(String indexName) {
		return elasticSearchOperationUtils.deleteIndex(indexName);
	}

	/**
	 * 根据ID删除数据
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean deleteRecordByCondition(String indexName, List<String> ids) {
		return deleteRecordByCondition(indexName, null, ids);
	}

	public boolean deleteRecordByCondition(String indexName, String type, List<String> ids) {
		return elasticSearchOperationUtils.deleteRecordByCondition(indexName, type, ids);
	}

	/**
	 * 单条插入（同步）
	 *
	 * @param indexName index
	 * @param data      data
	 * @return 返回ID
	 */
	public String insertOne(String indexName, Map<String, Object> data) {
		return insertOne(indexName, null, data);
	}

	public String insertOne(String indexName, String type, Map<String, Object> data) {
		return elasticSearchOperationUtils.insertOne(indexName, type, data);
	}

	/**
	 * 单条插入（同步&超时时间）
	 *
	 * @param indexName index
	 * @param data      data
	 * @param overTime  超时时间
	 * @return 是否成功
	 */
	public boolean insertOneSync(String indexName, Map<String, Object> data, long overTime) {
		return insertOneSync(indexName, null, data, overTime);
	}

	public boolean insertOneSync(String indexName, String type, Map<String, Object> data, long overTime) {
		return elasticSearchOperationUtils.insertOneSync(indexName, type, data, overTime);
	}

	/**
	 * 单条插入（异步）
	 *
	 * @param indexName index
	 * @param data      data
	 */
	public boolean insertOneASync(String indexName, Map<String, Object> data) {
		return insertOneASync(indexName, null, data);
	}

	public boolean insertOneASync(String indexName, String type, Map<String, Object> data) {
		return elasticSearchOperationUtils.insertOneASync(indexName, type, data);
	}

	/**
	 * 批量插入（同步）
	 *
	 * @param indexName index
	 * @param data      data
	 * @return 是否成功
	 */
	public boolean insertBulkSync(String indexName, List<Map<String, Object>> data) {
		return insertBulkSync(indexName, null, data);
	}

	public boolean insertBulkSync(String indexName, String type, List<Map<String, Object>> data) {
		return elasticSearchOperationUtils.insertBulkSync(indexName, type, data);
	}

	/**
	 * 批量插入（同步&超时时间）
	 *
	 * @param indexName index
	 * @param data      data
	 * @param overTime  超时时间
	 * @return success
	 */
	public boolean insertBulkSync(String indexName, List<Map<String, Object>> data, long overTime) {
		return insertBulkSync(indexName, null, data, overTime);
	}

	public boolean insertBulkSync(String indexName, String type, List<Map<String, Object>> data, long overTime) {
		return elasticSearchOperationUtils.insertBulkSync(indexName, type, data, overTime);
	}

	/**
	 * 批量插入（异步）
	 *
	 * @param indexName index
	 * @param data      data
	 * @return success
	 */
	public boolean insertBulkASync(String indexName, List<Map<String, Object>> data) {
		return insertBulkASync(indexName, null, data);
	}

	public boolean insertBulkASync(String indexName, String type, List<Map<String, Object>> data) {
		return elasticSearchOperationUtils.insertBulkASync(indexName, type, data);
	}

	/**
	 * 根据ID更新数据（同步）
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 * @return success
	 */
	public boolean upsertOneSyncById(String indexName, Map<String, Object> data, String id) {
		return upsertOneSyncById(indexName, null, data, id);
	}

	public boolean upsertOneSyncById(String indexName, String type, Map<String, Object> data, String id) {
		return elasticSearchOperationUtils.upsertOneSyncById(indexName, type, data, id);
	}

	/**
	 * 根据ID更新数据(同步&超时时间)
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 * @param overTime  超时时间
	 * @return success
	 */
	public boolean upsertOneSyncById(String indexName, Map<String, Object> data, String id, long overTime) {
		return upsertOneSyncById(indexName, null, data, id, overTime);
	}

	public boolean upsertOneSyncById(String indexName, String type, Map<String, Object> data, String id, long overTime) {
		return elasticSearchOperationUtils.upsertOneSyncById(indexName, type, data, id, overTime);
	}

	/**
	 * 根据ID更新数据（异步）
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 */
	public boolean upsertOneForASyncResponse(String indexName, Map<String, Object> data, String id) {
		return upsertOneForASyncResponse(indexName, null, data, id);
	}

	public boolean upsertOneForASyncResponse(String indexName, String type, Map<String, Object> data, String id) {
		return elasticSearchOperationUtils.upsertOneForASyncResponse(indexName, type, data, id);
	}

}
