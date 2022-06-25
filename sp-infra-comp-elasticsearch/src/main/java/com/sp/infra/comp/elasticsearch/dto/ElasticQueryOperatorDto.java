package com.sp.infra.comp.elasticsearch.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志查询对象
 * <p>
 * a < 10
 * a < 10 && a != 5
 *
 * @author alexlu
 * @date 2018-09-05 17:30:43
 */
@Data
public class ElasticQueryOperatorDto<T> implements Serializable {

	private static final long serialVersionUID = 8223188500904314565L;

	/**
	 * 查询字段
	 */
	private String key;

	/**
	 * 操作符号 =,!=,like,in,not in,>,>=
	 */
	private String operate;

	/**
	 * 查询值,operator 对应值（可以是String，String[]，JSON）
	 */
	private T value;

	/**
	 * 操作符号 <,<=
	 */
	private String operateByRight;

	/**
	 * 查询值 operatorByRight 对应值
	 */
	private String valueByRight;
}
