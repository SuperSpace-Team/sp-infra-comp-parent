package com.sp.infra.comp.elasticsearch.utils;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


/**
 * index 基本操作：创建索引，新增数据
 *
 * @author alexlu on 2019/7/17
 */
@Component
@Slf4j
public class ElasticSearchOperationUtils {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	private ElasticPerformRequestUtils elasticPerformRequestUtils;

	/**
	 * 创建索引
	 *
	 * @param indexName index
	 * @return 是否成功
	 */
	public boolean createIndex(String indexName) {
		if (elasticPerformRequestUtils.indexExist(indexName)) {
			throw new SystemException(ConstantsUtils.INDEX_EXISTING);
		}
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
		return createIndex(createIndexRequest);
	}

	/**
	 * 创建索引
	 *
	 * @param createIndexRequest
	 * @return 是否成功
	 */
	public boolean createIndex(CreateIndexRequest createIndexRequest) {
		try {
			CreateIndexResponse indexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			if (!indexResponse.isAcknowledged()) {
				log.info("创建索引失败");
			}
			return indexResponse.isAcknowledged();
		} catch (IOException e) {
			log.error("创建索引时出现异常", e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除index
	 *
	 * @param indexName index
	 * @return boolean 是否成功
	 */
	public boolean deleteIndex(String indexName) {
		boolean flag;
		if (!elasticPerformRequestUtils.indexExist(indexName)) {
			return true;
		}
		try {
			DeleteIndexRequest request = new DeleteIndexRequest(indexName);
			restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
			flag = true;
		} catch (IOException e) {
			log.error("删除index时出现异常", e);
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据ID删除数据
	 *
	 * @param indexName index
	 * @param type      type
	 * @return boolean 是否删除成功
	 */
	public boolean deleteRecordByCondition(String indexName, String type, List<String> ids) {
		try {
			BulkRequest bulkRequest = new BulkRequest();
			for (String id : ids) {
				DeleteRequest deleteRequest = getDeleteRequest(indexName, type);
				deleteRequest.id(id);
				bulkRequest.add(deleteRequest);
			}
			restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("删除数据时出现异常", e);
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 单条插入(同步)返回ID
	 *
	 * @param indexName index
	 * @param data      data
	 * @return ID 数据id
	 */
	public String insertOne(String indexName, String type, Map<String, Object> data) {
		return insertOneForResponse(indexName, type, data).getId();
	}

	/**
	 * 同步单条插入&超时时间检测
	 *
	 * @param indexName index
	 * @param data      data
	 * @param overTime  overTime
	 * @return boolean 是否插入成功
	 */
	public boolean insertOneSync(String indexName, String type, Map<String, Object> data, long overTime) {
		Callable<Boolean> callable = () -> {
			insertOneForResponse(indexName, type, data);
			return true;
		};
		return getTimeOut(callable, overTime);
	}

	/**
	 * 单条异步插入
	 *
	 * @param indexName index
	 * @param data      data
	 * @return boolean  请求成功,不代表结果
	 */
	public boolean insertOneASync(String indexName, String type, Map<String, Object> data) {
		ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse indexResponse) {

			}

			@Override
			public void onFailure(Exception e) {
				log.error("异步单条插入失败", e);
			}
		};
		IndexRequest indexRequest = getIndexRequest(indexName, type);
		indexRequest.source(data);
		restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
		return true;
	}

	/**
	 * 批量插入
	 *
	 * @param indexName index
	 * @param data      data
	 * @return boolean 是否插入成功
	 */
	public boolean insertBulkSync(String indexName, String type, List<Map<String, Object>> data) {
		getBulkInsertSync(indexName, type, data);
		return true;
	}


	/**
	 * 批量插入
	 *
	 * @param indexName index
	 * @param data      data
	 * @param idFieldName  id列表
	 * @return boolean 是否插入成功
	 */
	public boolean insertBulkSyncData(String indexName, String type, List<?> data,String idFieldName) {
		getBulkInsertSyncData(indexName, type, data,idFieldName);
		return true;
	}

	/**
	 * 同步批量插入&超时时间
	 *
	 * @param indexName index
	 * @param data      data
	 * @param overTime  overtime
	 * @return boolean 是否插入成功过
	 */
	public boolean insertBulkSync(String indexName, String type, List<Map<String, Object>> data, long overTime) {
		Callable<Boolean> callable = () -> {
			getBulkInsertSync(indexName, type, data);
			return true;
		};
		return getTimeOut(callable, overTime);
	}

	/**
	 * 异步批量插入
	 *
	 * @param indexName index
	 * @param data      data
	 * @return boolean 请求成功，不代插入表结果
	 */
	public boolean insertBulkASync(String indexName, String type, List<Map<String, Object>> data) {
		ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
			@Override
			public void onResponse(BulkResponse bulkItemResponses) {

			}

			@Override
			public void onFailure(Exception e) {
				log.error("异步批量插入失败", e);
			}
		};
		BulkRequest bulkRequest = new BulkRequest();
		for (Map<String, Object> map : data) {
			IndexRequest indexRequest = getIndexRequest(indexName, type);
			indexRequest.source(map);
			bulkRequest.add(indexRequest);
		}
		restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
		return true;
	}

	/**
	 * 单条更新(同步)
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 * @return boolean 是否更新成功过
	 */
	public boolean upsertOneSyncById(String indexName, String type, Map<String, Object> data, String id) {
		upsertOneForResponse(indexName, type, data, id);
		return true;
	}


    /**
     * 单条更新(同步)
     *
     * @param indexName index
     * @param object
     * @param id        id
     * @return boolean 是否更新成功过
     */
    public boolean upsertOneSyncById(String indexName, String type, Object  object, String id) {
        upsertOneForResponse(indexName, type, object, id);
        return true;
    }




    /**
	 * 同步单条更新的具体实现&超时时间
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 * @param overTime  overTime
	 * @return boolean 是否更新成功
	 */
	public boolean upsertOneSyncById(String indexName, String type, Map<String, Object> data, String id, long overTime) {
		Callable<Boolean> callable = () -> {
			upsertOneForResponse(indexName, type, data, id);
			return null;
		};
		return getTimeOut(callable, overTime);
	}

	/**
	 * 异步单条更新
	 *
	 * @param indexName index
	 * @param data      data
	 * @param id        id
	 * @return boolean 请求结果，不代表数据插入结果
	 */
	public boolean upsertOneForASyncResponse(String indexName, String type, Map<String, Object> data, String id) {
		ActionListener<UpdateResponse> listener = new ActionListener<UpdateResponse>() {
			@Override
			public void onResponse(UpdateResponse updateResponse) {

			}

			@Override
			public void onFailure(Exception e) {
				log.error("{} 异步修改执行失败", indexName, e);
			}
		};
		UpdateRequest request = getUpdateRequest(indexName, type);
		request.id(id);
		request.doc(data);
		request.upsert(data);
		restHighLevelClient.updateAsync(request, RequestOptions.DEFAULT, listener);
		return true;
	}


	private UpdateResponse upsertOneForResponse(String indexName, String type, Map<String, Object> data, String id) {
		UpdateResponse response = null;
		try {
			UpdateRequest request = getUpdateRequest(indexName, type);
			request.id(id);
			request.doc(data);
			request.upsert(data);
			response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("{} 根据ID：{} 更新数据失败", indexName, id);
			e.printStackTrace();
		}
		return response;
	}

    private UpdateResponse upsertOneForResponse(String indexName, String type, Object jsonData, String id) {
        UpdateResponse response = null;
        try {
            UpdateRequest request = getUpdateRequest(indexName, type);
            request.id(id);
            request.doc(JSON.toJSONString(jsonData),XContentType.JSON);
            request.upsert(JSON.toJSONString(jsonData),XContentType.JSON);
            response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("{} 根据ID：{} 更新数据失败", indexName, id);
            e.printStackTrace();
        }
        return response;
    }

	private void getBulkInsertSyncData(String indexName, String type, List<?> dataList,String idFieldNmae) {
		try {
			BulkRequest bulkRequest = new BulkRequest();

			dataList.forEach(data->{
				UpdateRequest request = getUpdateRequest(indexName, type);
				try {
					Object o = FieldUtils.readField(data, idFieldNmae, true);
					request.id(String.valueOf(o));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				request.doc(JSON.toJSONString(data),XContentType.JSON);
				request.upsert(JSON.toJSONString(data),XContentType.JSON);
				bulkRequest.add(request);
			});

			restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("{} 批量插入失败", indexName, e);
		}
	}

	private void getBulkInsertSync(String indexName, String type, List<Map<String, Object>> data) {
		try {
			BulkRequest bulkRequest = new BulkRequest();
			for (Map<String, Object> map : data) {
				IndexRequest indexRequest = getIndexRequest(indexName, type);
				indexRequest.source(map);
				bulkRequest.add(indexRequest);
			}
			restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("{} 批量插入失败", indexName, e);
		}
	}

	private boolean getTimeOut(Callable<?> task, long timeOut) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		FutureTask<?> futureTask = (FutureTask<?>) executorService.submit(task);
		executorService.execute(futureTask);
		try {
			futureTask.get(timeOut, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private IndexResponse insertOneForResponse(String indexName, String type, Map<String, Object> data) {
		IndexResponse indexResponse = null;
		try {
			IndexRequest indexRequest = getIndexRequest(indexName, type);
			indexRequest.source(data);
			indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("{} 插入数据失败", indexName, e);
		}
		return indexResponse;
	}

	private IndexRequest getIndexRequest(String indexName, String type) {
		IndexRequest indexRequest = new IndexRequest(indexName);
		if (!StringUtils.isEmpty(type)) {
			indexRequest.type(type);
		}
		return indexRequest;
	}

	private UpdateRequest getUpdateRequest(String indexName, String type) {
		UpdateRequest request = new UpdateRequest();
		request.index(indexName);
		if (!StringUtils.isEmpty(type)) {
			request.type(type);
		}
		return request;
	}

	private DeleteRequest getDeleteRequest(String indexName, String type) {
		DeleteRequest request = new DeleteRequest(indexName);
		if (!StringUtils.isEmpty(type)) {
			request.type(type);
		}
		return request;
	}


}
