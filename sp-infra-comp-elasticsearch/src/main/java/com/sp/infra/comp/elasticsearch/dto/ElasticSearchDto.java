package com.sp.infra.comp.elasticsearch.dto;

import com.sp.framework.common.exception.SystemException;
import lombok.Data;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.sp.infra.comp.elasticsearch.utils.ConstantsUtils.QueryPartams.CURR_PAGE_MIN;

/**
 * 日志查询对象
 *
 * @author alexlu
 * @date 2018-09-05 17:30:43
 */
@Data
public class ElasticSearchDto {

    /**
     * Http请求参数
     */
    private ElasticQueryDto queryDto;

    /**
     * 组合查询 BoolQueryBuilder
     */
    private BoolQueryBuilder boolQuery;

    /**
     * and or 查询
     */
    List<BoolQueryBuilder> buildBoolQueryByShouldList = new ArrayList<>();

    /**
     * 聚合统计
     */
    private AggregationBuilder aggregationBuilder;

    /**
     * 查询中的已知异常
     */
    private String errorMsg;

    /**
     * 排序字段
     */
    private List<FieldSortBuilder> listSort;

    /**
     * 查询结果
     */
    private SearchResponse searchResponse;

    /**
     * 折叠/去重
     */
    private CollapseBuilder collapse;

    public ElasticSearchDto(ElasticQueryDto queryDto) {
        this.queryDto = queryDto;
        if (queryDto.getIncludeFields() == null || queryDto.getIncludeFields().length == 0) {
            throw new SystemException("请指定需要查询的字段");
        }

        ElasticQueryPageDto queryPageDto = queryDto.getQueryPageDto();
        if (queryPageDto == null) {
            queryPageDto = new ElasticQueryPageDto();
            queryPageDto.setLimit(10000);
        }
        queryPageDto.setCurrPage(queryPageDto.getCurrPage() < CURR_PAGE_MIN ? CURR_PAGE_MIN : queryPageDto.getCurrPage());
        queryPageDto.setOffset((queryPageDto.getCurrPage() - 1) * queryPageDto.getLimit());
        queryDto.setQueryPageDto(queryPageDto);

		/**
		 * 加入聚合统计条件 by wangchong 2020/02/21
		 */
		AggregationBuilder aggregationBuilder = queryDto.getAggregationBuilder();
		if (aggregationBuilder != null) {
			this.aggregationBuilder = aggregationBuilder;
        }
    }

}
