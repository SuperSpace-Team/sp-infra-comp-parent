package com.sp.infra.comp.elasticsearch.dto;

import java.util.List;
import java.util.Map;

/**
 * @package: com.sp.infra.comp.elasticsearch.dto
 * @author: chaofan.you
 * @date: 2019-06-20 18:06
 * @description:
 */
public class SearchAfterResult {

    private String searchAfter;
    private List<Map<String, Object>> searchResult;

    public SearchAfterResult() {

    }

    public SearchAfterResult(String searchAfter, List<Map<String, Object>> searchResult) {
        this.searchAfter = searchAfter;
        this.searchResult = searchResult;
    }

    public String getSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(String searchAfter) {
        this.searchAfter = searchAfter;
    }

    public List<Map<String, Object>> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<Map<String, Object>> searchResult) {
        this.searchResult = searchResult;
    }
}