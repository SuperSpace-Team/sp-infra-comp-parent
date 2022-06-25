package com.sp.infra.comp.elasticsearch.dto;

import lombok.Data;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.List;
import java.util.Map;

/**
 * 日志查询对象
 *
 * @author alexlu
 * @date 2018-09-05 17:30:43
 */
@Data
public class ElasticQueryDto {

	/**
	 * 分页查询
	 */
	private ElasticQueryPageDto queryPageDto;

	/**
	 * 排序的字段 key: 字段，value: 排序方式
	 */
	private Map<String, String> sidx;

	/**
	 * 是否先检查index是否存在
	 */
	private boolean checkIndex = false;

	/**
	 * 查询的index
	 */
	private String[] indices;

	/**
	 * 查询的字段
	 */
	private String[] includeFields;

	/**
	 * 排除的字段
	 */
	private String[] excludeFields;

	/**
	 * 更多查询
	 */
	private List<ElasticQueryOperatorDto> operators;

	/**
	 * searchAfter参数（用唯一标识_id)
	 */
	private String[] searchAfter;


	/**
	 * 聚合统计
	 */
	private AggregationBuilder aggregationBuilder;
}
