package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response.EsResponseHandlerInterface;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response.EsResponseHandler_2x;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response.EsResponseHandler_5x;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response.EsResponseHandler_6x;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 17:24 2018/8/16
 * @Modified By:
 */
@Component
public class ESSearchResultUtil {

    private static Logger logger = LoggerFactory.getLogger(ESSearchResultUtil.class);

    private volatile EsResponseHandlerInterface esDSLHandlerInterface;

    @Autowired
    EsResponseHandler_2x esResponseHandler_2x;
    @Autowired
    EsResponseHandler_5x esResponseHandler_5x;
    @Autowired
    EsResponseHandler_6x esResponseHandler_6x;

    @PostConstruct
    public void init(){
        String version = EsInfoManagement.getVersion();
        String firstVersion = version.substring(0, 1);
        switch (firstVersion){
            case "2" :
                esDSLHandlerInterface = esResponseHandler_2x;break;
            case "5" :
                esDSLHandlerInterface = esResponseHandler_5x;break;
            case "6" :
                esDSLHandlerInterface = esResponseHandler_6x;break;
            default :
                esDSLHandlerInterface = esResponseHandler_5x;break;
        }
        logger.info("new EsDSLHandler instance success.{}", esDSLHandlerInterface.toString());
    }

    //返回值格式可参考ElasticsearchTestSuite.java
    public JSONObject parseResponse(String responseJson, SearchRequest searchRequest){
        return esDSLHandlerInterface.parseResponse(responseJson, searchRequest);
    }

    //从responseJson获取aggregations
    public JSONArray getAggregations(JSONObject responseJson, String key){
        return esDSLHandlerInterface.getAggregations(responseJson, key);
    }

    //此方法适用于不带聚合的查询，带聚合的查询不应该用返回值为SearchResponse的方法
    public JSONObject parseResponse(SearchResponse searchResponse){
        return esDSLHandlerInterface.parseResponse(searchResponse);
    }

    public List<Map<String, Object>> parseHit(SearchResponse searchResponse){
        return esDSLHandlerInterface.parseHit(searchResponse);
    }

    //根据request来解析response,样例可参考ElasticsearchTestSuite.java
    public JSONObject parseAggregations(String responseJson, SearchRequest searchRequest){
        return esDSLHandlerInterface.parseAggregations(responseJson, searchRequest);
    }

    public JSONObject parseAggregations(String responseJson, String requestJson){
        return esDSLHandlerInterface.parseAggregations(responseJson, requestJson);
    }

}
