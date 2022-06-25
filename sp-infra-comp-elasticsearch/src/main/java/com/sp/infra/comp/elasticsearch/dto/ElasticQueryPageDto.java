package com.sp.infra.comp.elasticsearch.dto;

import lombok.Data;

/**
 * 日志查询对象
 *
 * @author alexlu
 * @date 2018-09-05 17:30:43
 */
@Data
public class ElasticQueryPageDto {

	/**
	 * 当前页码
	 */
	private int currPage = 1;

	/**
	 * 每页条数，最大100
	 */
	private int limit = 100;

	/**
	 * 查询起始位置
	 */
	private int offset = 0;

}
