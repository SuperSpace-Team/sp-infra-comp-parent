package com.sp.infra.comp.elasticsearch.utils;

/**
 * 常量
 *
 * @author alexlu
 */
public class ConstantsUtils {

	private static String illegalStateException = "Utility class";
	public static final String OK = "OK";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String SEARCH_ERROE_MESSAGE = "查询时出现错误";
	public static final String INDEX_NOT_EXISTS = "不存在查询的Index";
	public static final String INDEX_EXISTING = "索引已存在";
	public static final String SIDX_NOT_EXISTS = "需要设定排序字段";

	/**
	 * query 查询对象参数
	 */
	public abstract class QueryPartams {
		private QueryPartams() {
			throw new IllegalStateException(illegalStateException);
		}

		/**
		 * 当前页数
		 */
		public static final int CURR_PAGE_MIN = 1;
		public static final String ASC = "asc";
		public static final String DESC = "desc";
		public static final String INDEX = "_index";
		/**
		 * 最大返回结果
		 */
		public static final long MAX_RESULT_COUNT = 10000;
	}

	/**
	 * 查询类型
	 */
	public enum QueryTypeEnum {

		/**
		 * 精确匹配字段查询; 指定查询一个字段对应单个词条
		 */
		TERM_QUERY("eq", "termQuery"),
		/**
		 * 精确匹配字段查询(多term查询); 查询一个字段对应多个词条
		 */
		IN_TERMS("in", "termsQuery"),
		/**
		 * 当前字段值范围查询
		 */
		RANGE_QUERY("rangeQuery", "rangeQuery"),
		/**
		 * SHOULD_IN_TERM 查询
		 */
		SHOULD_IN_TERM("sin", "shouldInTerm"),
		/**
		 * IN Wildcard 查询
		 */
		SHOULD_IN_WILDCARD("shouldInWildcard", "shouldInWildcard"),
		/**
		 * 匹配非当前值的查询 not in,多个值逗号分割
		 */
		MUST_NOT_TERM("notIn", "mustNotTerm"),
		/**
		 * 匹配非当前值的查询 not in,多个值逗号分割
		 */
		MUST_NOT_WILDCARD("mustNotWildcard", "mustNotWildcard"),
		/**
		 * 模糊查询
		 */
		WILDCARD_QUERY("like", "wildcardQuery"),
		/**
		 * 解析查询
		 */
		QUERY_STRING_QUERY("queryStringQuery", "queryStringQuery"),

		/**
		 * 解析查询-多查询
		 */
		QUERY_STRING_MULTI_QUERY("multiQuery", "queryStringValueInKeys");

		private String value;
		private String describe;

		QueryTypeEnum(String value, String describe) {
			this.value = value;
			this.describe = describe;
		}

		public String getValue() {
			return value;
		}

		public String getDescribe() {
			return describe;
		}
	}

	/**
	 * 运算符
	 */
	public enum LogicalRelationEnum {
		GT("gt", ">"),
		GTE("gte", ">="),
		LT("lt", "<"),
		LTE("lte", "<=");

		private String value;
		private String describe;

		LogicalRelationEnum(String value, String describe) {
			this.value = value;
			this.describe = describe;
		}

		public String getValue() {
			return value;
		}

		public String getDescribe() {
			return describe;
		}
	}

}
