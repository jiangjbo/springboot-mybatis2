package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: jiangbo
 * @Descriotion:
 * @Date: Created in 10:59 2018/4/27
 * @Modified By:
 */
public class EsInfoManagement {

    private static Logger logger = LoggerFactory.getLogger(EsInfoManagement.class);
    private volatile static String version = "";

    private static JSONObject getClusterInfo(ESRestClient esRestClient) throws Exception {
        String method = HttpGet.METHOD_NAME;
        String endpoint = "/";
        Response response = esRestClient.getLowRestClient().performRequest(method, endpoint);
        JSONObject jsonObject = JSONArray.parseObject(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        //通过es_proxy访问es
        if(jsonObject.containsKey("upstream")){
            String esUrl = jsonObject.getJSONObject("upstream").getJSONObject("primary").getString("endpoint");
            String str = HttpClientUtils.get(esUrl);
            jsonObject = JSON.parseObject(str);
        }
        return jsonObject;
    }

    public static String initVersion(ESRestClient esRestClient) {
        while(StringUtils.isBlank(version)){
            try {
                JSONObject jsonObject = EsInfoManagement.getClusterInfo(esRestClient);
                version = jsonObject.getJSONObject("version").getString("number");
                logger.info("elasticsearch version : " + version);
            } catch (Exception e) {
                logger.error("search es version failed", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    logger.error("", e1);
                }
            }
        }
        return version;
    }

    public static String getVersion(){
        return version;
    }
}
