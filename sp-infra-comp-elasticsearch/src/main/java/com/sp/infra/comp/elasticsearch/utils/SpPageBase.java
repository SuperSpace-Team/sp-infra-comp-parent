package com.sp.infra.comp.elasticsearch.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类
 *
 * @author zhanghai
 * @date 2018-09-05 17:30:43
 */
@Data
@NoArgsConstructor
public class SpPageBase<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 总记录数
	 */
	private long total;

	/**
	 * 每页记录数
	 */
	private long size;

	/**
	 * 总页数
	 */
	private long pages;

	/**
	 * 当前页数
	 */
	private long current;

	/**
	 * 列表数据
	 */
	private List<T> list;

	/**
	 * 分页
	 *
	 * @param list    列表数据
	 * @param total   总记录数
	 * @param size    每页记录数
	 * @param current 当前页数
	 */
	public SpPageBase(List<T> list, long total, long size, long current) {
		this.list = list;
		this.total = total;
		this.size = size;
		this.current = current;
		this.pages = (long) Math.ceil((double) total / size);
	}

}
