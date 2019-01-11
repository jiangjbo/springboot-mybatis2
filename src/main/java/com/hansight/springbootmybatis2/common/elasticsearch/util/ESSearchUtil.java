package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 10:11 2018/7/22
 * @Modified By:
 */
@Component
public class ESSearchUtil {

    @Autowired
    private ESRestClient esRestClient;
    @Autowired
    private ESSearchResultUtil esSearchResultUtil;

    private static Logger logger = LoggerFactory.getLogger(ESSearchUtil.class);

    public JSONObject search(String indexName, long startTime, long endTime, int size, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return search(getIndices(indexName, startTime, endTime), null, size, null, queryBuilder, aggregationBuilders);
    }

    public JSONObject search(String indexName, long startTime, long endTime, String type, int size, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return search(getIndices(indexName, startTime, endTime), type == null ? null : new String[]{type}, size, null, queryBuilder, aggregationBuilders);
    }

    public JSONObject search(String indexName, long startTime, long endTime, String[] types, int size, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return search(getIndices(indexName, startTime, endTime), types, size, null, queryBuilder, aggregationBuilders);
    }

    public JSONObject search(String indexName, long startTime, long endTime, int size, String[] fetchFields, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return search(getIndices(indexName, startTime, endTime), null, size, fetchFields, queryBuilder, aggregationBuilders);
    }

    public JSONObject search(String[] indicesName, String[] type, int size, String[] fetchFields, QueryBuilder queryBuilder, AggregationBuilder[] aggregationBuilders, SortBuilder... sortBuilders) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indicesName);
        if(type != null && type.length > 0){
            searchRequest.types(type);
        }
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(buildSearchSource(size, fetchFields, queryBuilder, aggregationBuilders, sortBuilders));
        JSONObject result = null;
        //不涉及聚合操作
        if(aggregationBuilders == null || aggregationBuilders.length == 0){
            SearchResponse searchResponse = esRestClient.getRestHighLevelClient().search(searchRequest);
            result =  esSearchResultUtil.parseResponse(searchResponse);
        } else {
            String responseJson =  ESLowClientUtil.search(esRestClient.getLowRestClient(), searchRequest);
            result =  esSearchResultUtil.parseResponse(responseJson, searchRequest);
        }
        return result;
    }

    public JSONObject searchFixedIndex(String indexName, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return search(StringUtils.split(indexName,","), null, size, fetchFields, queryBuilder, null, sortBuilders);
    }

    public JSONObject searchFixedIndex(String indexName, int size, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilder) throws IOException {
        return searchFixedIndex(indexName, null, size, queryBuilder, aggregationBuilder);
    }

    public JSONObject searchFixedIndex(String indexName, String type, int size, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return searchFixedIndex(indexName, type, size, null, queryBuilder, aggregationBuilders);
    }

    public JSONObject searchFixedIndex(String indexName, String type, int size, String[] fetchFields, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilders) throws IOException {
        return search(StringUtils.split(indexName,","), type == null ? null : new String[]{type}, size, fetchFields, queryBuilder, aggregationBuilders);
    }

    public SearchResponse searchResponse(String indexName, long startTime, long endTime, int size, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return searchResponse(indexName, startTime, endTime, size, null, queryBuilder, sortBuilders);
    }

    public SearchResponse searchResponse(String indexName, long startTime, long endTime, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return searchResponse(indexName, startTime, endTime, new String[0], size, fetchFields, queryBuilder, sortBuilders);
    }

    public SearchResponse searchResponse(String indexName, long startTime, long endTime, String type, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return searchResponse(indexName, startTime, endTime, type == null ? null : new String[]{type}, size, fetchFields, queryBuilder, sortBuilders);
    }

    public SearchResponse searchResponse(String indexName, long startTime, long endTime, String[] types, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        SearchRequest searchRequest = new SearchRequest(getIndices(indexName, startTime, endTime));
        if(types != null && types.length > 0){
            searchRequest.types(types);
        }
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(buildSearchSource(size, fetchFields, queryBuilder, null, sortBuilders));
        return esRestClient.getRestHighLevelClient().search(searchRequest);
    }

    public SearchResponse searchResponseFixedIndex(String indexName, int size, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return searchResponseFixedIndex(indexName, size, null, queryBuilder, sortBuilders);
    }

    public SearchResponse searchResponseFixedIndex(String indexName, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        return searchResponseFixedIndex(indexName, null, size, fetchFields, queryBuilder, sortBuilders);
    }

    public SearchResponse searchResponseFixedIndex(String indexName, String type, int size, String[] fetchFields, QueryBuilder queryBuilder, SortBuilder... sortBuilders) throws IOException {
        String[] indices = indexName.split(",");
        SearchRequest searchRequest = new SearchRequest(indices);
        if(StringUtils.isNoneBlank(type)){
            searchRequest.types(type);
        }
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(buildSearchSource(size, fetchFields, queryBuilder, null, sortBuilders));
        return esRestClient.getRestHighLevelClient().search(searchRequest);
    }

    protected SearchSourceBuilder buildSearchSource(int size, String[] fetchFields, QueryBuilder queryBuilder, AggregationBuilder[] aggregationBuilders, SortBuilder... sortBuilders){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if(queryBuilder != null){
            searchSourceBuilder.query(queryBuilder);
        }
        if(fetchFields != null && fetchFields.length > 0){
            searchSourceBuilder.fetchSource(fetchFields, null);
        }
        if(aggregationBuilders != null && aggregationBuilders.length > 0){
            Arrays.stream(aggregationBuilders).forEach(searchSourceBuilder::aggregation);

        }
        if(sortBuilders != null && sortBuilders.length > 0){
            for(SortBuilder sortBuilder : sortBuilders){
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        searchSourceBuilder.size(size);
        return searchSourceBuilder;
    }

    private String[] getIndices(String indexName, long startTime, long endTime){
        List<String> indices = null;
        //对过长的时间间隔，从间隔中获取可用的索引列表
        if((endTime - startTime) > 365 * 24 * 60 * 60 * 1000L){
            indices = EsUtil.getIndicesWithLongTime(esRestClient, indexName, startTime, endTime);
            //当前indexName下无可用索引，用indexName_*模式匹配，避免无索引查询
            if(indices.isEmpty()){
                indices = Collections.singletonList(indexName + "_*");
            }
        }else{
            indices = EsUtil.getIndicesWithoutValidate(indexName, startTime, endTime);
        }
        return indices.toArray(new String[]{});
    }
}
