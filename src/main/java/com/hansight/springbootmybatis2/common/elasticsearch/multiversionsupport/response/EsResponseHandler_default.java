package com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 15:26 2018/8/31
 * @Modified By:
 */
public class EsResponseHandler_default implements EsResponseHandlerInterface{

    protected List<String> bucketAgg = Arrays.asList("date_histogram", "terms", "adjacency_matrix", "children", "date_range", "diversified_sampler", "filter", "filters",
            "geo_distance", "geohash_grid", "global", "histogram", "ip_range", "missing", "nested", "range", "reverse_nested", "sampler", "significant_terms", "significant_text");

    protected List<String> metricAgg = Arrays.asList("avg", "max", "sum", "min", "value_count", "cardinality", "extended_stats", "geo_bounds", "geo_centroid",
            "percentiles", "percentile_ranks", "scripted_metric", "stats", "top_hits");

    //返回值格式可参考ElasticsearchTestSuite.java
    @Override
    public JSONObject parseResponse(String responseJson, SearchRequest searchRequest){
        JSONObject jsonObject = JSONObject.parseObject(responseJson);
        if(jsonObject.containsKey("aggregations")){
            jsonObject.replace("aggregations", parseAggregations(responseJson, searchRequest));
        }
        return jsonObject;
    }

    //此方法适用于不带聚合的查询，带聚合的查询不应该用返回值为SearchResponse的方法
    @Override
    public JSONObject parseResponse(SearchResponse searchResponse){
        JSONObject jsonObject = new JSONObject();
        searchResponse.getTook();
        jsonObject.put("hits", parseHit(searchResponse));
        return jsonObject;
    }

    @Override
    public JSONArray getAggregations(JSONObject responseJson, String key){
        JSONArray result = null;
        JSONObject aggregations = responseJson.getJSONObject("aggregations");
        if(aggregations != null){
            result = aggregations.getJSONArray(key);
        } else{
            result = new JSONArray();
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> parseHit(SearchResponse searchResponse){
        return Arrays.stream(searchResponse.getHits().getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toList());
    }

    //返回值格式可参考ElasticsearchTestSuite.java
    @Override
    public JSONObject parseAggregations(String responseJson, SearchRequest searchRequest){
        return parseAggregations(responseJson, searchRequest.source().toString());
    }

    public JSONObject parseAggregations(String responseJson, String requestJson){
        List<AggField> aggFields = new ArrayList<>();
        parseRequestAggregation(JSONObject.parseObject(requestJson), aggFields);
        return parseAggregations(responseJson, aggFields);
    }

    protected void parseRequestAggregation(JSONObject jsonObject, List<AggField> aggFields){
        JSONObject aggregations = jsonObject.getJSONObject("aggregations");
        if(aggregations != null){
            for(String key : aggregations.keySet()){
                JSONObject keyJsonObject = aggregations.getJSONObject(key);
                bucketAgg.stream().filter(keyJsonObject::containsKey).findFirst()
                        .ifPresent(aggName -> aggFields.add(new AggField(key, keyJsonObject.getJSONObject(aggName).getString("field"), aggName, AggType.BUCKET)));
                metricAgg.stream().filter(keyJsonObject::containsKey).findFirst()
                        .ifPresent(aggName -> aggFields.add(new AggField(key, keyJsonObject.getJSONObject(aggName).getString("field"), aggName, AggType.METRIC)));
                //如果包含aggregations,则进一步分析
                if(keyJsonObject.containsKey("aggregations")){
                    parseRequestAggregation(keyJsonObject, aggFields);
                }
            }
        }
    }

    protected JSONObject parseAggregations(String responseJson, List<AggField> aggFields){
        JSONObject results = new JSONObject();
        JSONObject aggregations = JSONObject.parseObject(responseJson).getJSONObject("aggregations");
        if(aggregations != null && aggFields != null && aggFields.size() > 0){
            parse(aggregations, results, aggFields);
        }
        return results;
    }

    protected void parse(JSONObject aggregations, JSONObject results, List<AggField> aggFields){
        aggregations.keySet().forEach(key -> {
            //解析bucket agg
            aggFields.stream().filter(aggField -> AggType.BUCKET.equals(aggField.aggType) && aggField.fieldName.equals(key)).findFirst()
                    .ifPresent(aggField -> parseBucketAgg(aggregations, aggField, aggFields, results));
            //解析metric agg
            aggFields.stream().filter(aggField -> AggType.METRIC.equals(aggField.aggType) && aggField.fieldName.equals(key)).findFirst()
                    .ifPresent(aggField -> parseMetricAgg(aggregations, aggField, results));
        });
    }

    protected void parseBucketAgg(JSONObject aggregations, AggField aggField, List<AggField> aggFields, JSONObject results){
        switch (aggField.aggName){
            case "terms": case "date_histogram": case "histogram": case "adjacency_matrix": case "geohash_grid":
            case "date_range": case "geo_distance": case "ip_range": case "range":
            case "parseBucketByCrimeTypes": case "significant_text": case "filters":
                parseBucketByJsonArray(aggregations, aggField, aggFields, results);break;
            case "filter": case "children": case "diversified_sampler": case "global": case "missing": case "resellers":
            case "reverse_nested": case "sampler":
                parseBucketByJsonObject(aggregations, aggField, aggFields, results);break;
            default:
                //TODO
                break;
        }
    }

    //解析数组类的bucket
    protected void parseBucketByJsonArray(JSONObject aggregations, AggField aggField, List<AggField> aggFields, JSONObject results){
        JSONArray buckets = aggregations.getJSONObject(aggField.fieldName).getJSONArray("buckets");
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<buckets.size();i++){
            JSONObject bucketResult = new JSONObject();
            JSONObject bucket = buckets.getJSONObject(i);
            switch (aggField.aggName){
                case "date_range": case "geo_distance": case "ip_range": case "range":
                    if(bucket.containsKey("to")){
                        bucketResult.put("to", bucket.get("to"));
                    }
                    if(bucket.containsKey("from")){
                        bucketResult.put("from", bucket.get("from"));
                    }
                    break;
                case "parseBucketByCrimeTypes": case "significant_text":
                    if(bucket.containsKey("score")){
                        bucketResult.put("score", bucket.get("score"));
                    }
                    if(bucket.containsKey("bg_count")){
                        bucketResult.put("bg_count", bucket.get("bg_count"));
                    }
                    break;
                default:break;
            }
            parseKey(bucket, aggField, bucketResult);
            bucketResult.put("doc_count", bucket.get("doc_count"));
            parseBucket(bucket, aggField, aggFields, bucketResult);
            jsonArray.add(bucketResult);
        }
        results.put(aggField.fieldName, jsonArray);
    }

    //解析jsonObject类的bucket
    protected void parseBucketByJsonObject(JSONObject aggregations, AggField aggField, List<AggField> aggFields, JSONObject results){
        JSONObject bucketResult = new JSONObject();
        JSONObject bucket = aggregations.getJSONObject(aggField.fieldName);
        bucketResult.put("doc_count", bucket.get("doc_count"));
        parseBucket(bucket, aggField, aggFields, bucketResult);
        results.put(aggField.fieldName, bucketResult);
    }

    //解析bucket
    protected void parseBucket(JSONObject bucket, AggField aggField, List<AggField> aggFields, JSONObject results){
        //当前bucket中是否含有metric field，解析metric agg
        aggFields.stream().filter(aggField2 ->  AggType.METRIC.equals(aggField2.aggType) && bucket.containsKey(aggField2.fieldName))
                .forEach(aggField2 -> parseMetricAgg(bucket, aggField2, results));
        //aggFields中bucket在前,metric在后，并且顺序为解析顺序,大于2，仍有待解析的bucket
        if(aggFields.stream().filter(aggField2 -> aggField2.aggType.equals(AggType.BUCKET)).count() >= 2){
            parse(bucket, results, aggFields.subList(1, aggFields.size()));
        }
    }

    protected void parseMetricAgg(JSONObject aggregations, AggField aggField, JSONObject results){
        switch (aggField.aggName){
            case "value_count": case "sum": case "max": case "min": case "avg": case "cardinality": case "scripted_metric":
                if(aggregations.getJSONObject(aggField.fieldName).get("value") != null){
                    results.put(aggField.fieldName, aggregations.getJSONObject(aggField.fieldName).get("value"));
                }
                break;
            case "top_hits":
                JSONObject topHits = aggregations.getJSONObject(aggField.fieldName);
                if(topHits != null){
                    results.put(aggField.fieldName, topHits.getJSONObject("hits").getJSONArray("hits").stream().map(hit -> ((JSONObject)hit).getJSONObject("_source")).collect(Collectors.toList()));
                }
                break;
            default://geo_bounds,geo_centroid,percentiles,percentile_ranks,extended_stats,stats
                results.putAll(aggregations);
                break;
        }
    }

    protected void parseKey(JSONObject srcJson, AggField aggField, JSONObject results){
        results.put(aggField.fieldName, srcJson.get("key"));
    }
}
