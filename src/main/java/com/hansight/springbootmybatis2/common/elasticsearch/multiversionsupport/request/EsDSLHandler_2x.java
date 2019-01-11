package com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.request;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 13:35 2018/8/1
 * @Modified By:    将默认版本的dsl转换为2.x版本的dsl,如果遇到新的不兼容情况，需要在这里添加适配代码
 */
public class EsDSLHandler_2x implements EsDSLHandlerInterface{

    @Override
    public String handleJson(String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        //用于兼容fetchSource
        parseFetchSource(jsonObject);
        //用于兼容query和agg
        parserJsonObject(jsonObject);
        return jsonObject.toJSONString();
    }

    private void parseFetchSource(JSONObject jsonObject){
        //5.x 查询保留字段默认为stored_fields， 2.x 为fields
        Object storedFields = jsonObject.get("stored_fields");
        jsonObject.remove("stored_fields");
        jsonObject.put("fields", storedFields);
    }

    private void parserJsonObject(JSONObject jsonObject){
        convertJson(jsonObject);
        for(String key :jsonObject.keySet()){
            Object value = jsonObject.get(key);
            if(value instanceof JSONObject){
                parserJsonObject((JSONObject)value);
            } else if(value instanceof JSONArray){
                parserJsonArray((JSONArray) value);
            }
        }
    }

    private void parserJsonArray(JSONArray jsonArray){
        for (Object value : jsonArray) {
            if (value instanceof JSONObject) {
                parserJsonObject((JSONObject) value);
            } else if (value instanceof JSONArray) {
                parserJsonArray((JSONArray) value);
            }
        }
    }

    private void convertJson(JSONObject jsonObject){
        //兼容2.x版本，移除exists下的权重加分，其他待补充
        if(jsonObject.containsKey("exists")){
            removeBoost(jsonObject, "exists");
        }
        //将5.x 版本的date_histogram查询翻译成 2.x的语句,可兼容2.x以上版本
        if(jsonObject.containsKey("date_histogram")){
            parseDateHistogram(jsonObject);
        }
        //当有查询语句中有脚本
        if(jsonObject.containsKey("script")){
            parseScript(jsonObject);
        }
    }

    //移除指定查询字段下的boost
    private void removeBoost(JSONObject jsonObject, String field){
        JSONObject json = jsonObject.getJSONObject(field);
        if(json.containsKey("boost")){
            json.remove("boost");
        }
    }

    private void parseDateHistogram(JSONObject jsonObject){
        JSONObject date = jsonObject.getJSONObject("date_histogram");
        if(StringUtils.isNumeric(date.getString("offset"))){
            date.put("offset",date.getLong("offset")/1000 + "s");
        }
        if(StringUtils.isNumeric(date.getString("interval"))){
            date.put("interval",date.getLong("interval")/1000 + "s");
        }
        JSONObject extend = date.getJSONObject("extended_bounds");
        if(extend.get("min") instanceof String){
            extend.put("min",Long.parseLong((String)extend.get("min")));
        }
        if(extend.get("max") instanceof String){
            extend.put("max",Long.parseLong((String)extend.get("max")));
        }
    }

    private void parseScript(JSONObject jsonObject){
        //将"source"改为"inline"
        JSONObject script = jsonObject.getJSONObject("script");
        if(script.containsKey("source")){
            //JSONObject source = script.getJSONObject("source");
            //source里包含脚本，可能很复杂，不用json解析
            Object source = script.get("source");
            if(source != null){
                script.put("inline", source);
                script.remove("source");
            }
        }
        //将"lang": "painless"改为"lang": "groovy"
        if(script.containsKey("lang")){
            script.replace("lang", "groovy");
        }
    }

}
