package com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 15:08 2018/8/31
 * @Modified By:
 */
public interface EsResponseHandlerInterface {

    Logger logger = LoggerFactory.getLogger(EsResponseHandlerInterface.class);

    //返回值格式可参考ElasticsearchTestSuite.java
    JSONObject parseResponse(String responseJson, SearchRequest searchRequest);

    //此方法适用于不带聚合的查询，带聚合的查询不应该用返回值为SearchResponse的方法
    JSONObject parseResponse(SearchResponse searchResponse);

    //从responseJson获取aggregations
    JSONArray getAggregations(JSONObject responseJson, String key);

    List<Map<String, Object>> parseHit(SearchResponse searchResponse);

    //根据request来解析response,样例可参考ElasticsearchTestSuite.java
    JSONObject parseAggregations(String responseJson, SearchRequest searchRequest);

    JSONObject parseAggregations(String responseJson, String requestJson);

    class AggField {
        public final String fieldName;
        public final String srcFieldName;
        public final String aggName;
        public final AggType aggType;
        public boolean isParse;

        public AggField(String fieldName, String srcFieldName, String aggName, AggType aggType) {
            this.fieldName = fieldName;
            this.srcFieldName = srcFieldName;
            this.aggName = aggName;
            this.aggType = aggType;
            this.isParse = true;
        }

        public AggField(String fieldName, String srcFieldName, String aggName, AggType aggType, String specialField) {
            this.fieldName = fieldName;
            this.srcFieldName = srcFieldName;
            this.aggName = aggName;
            this.aggType = aggType;
            this.isParse = true;
        }

        public boolean isParse() {
            return isParse;
        }

        public void setParse(boolean parse) {
            isParse = parse;
        }
    }

    enum AggType {
        BUCKET, METRIC
    }

}
