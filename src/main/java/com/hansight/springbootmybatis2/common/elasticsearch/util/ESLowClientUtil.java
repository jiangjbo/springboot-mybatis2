package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.HanRestClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jiangbo
 * @Descriotion:
 * @Date: Created in 14:44 2018/4/2
 * @Modified By:
 */
public class ESLowClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(ESLowClientUtil.class);

    /**
     *
     * @param restClient
     * @param indexName
     * @param json      source内容
     * @return          删除的成功数
     * @throws IOException
     */
    public static int deleteByQuery(HanRestClient restClient, String indexName, String json) throws IOException {
        if(StringUtils.isBlank(indexName)){
            logger.error("es deleteByQuery must have index name");
            return 0;
        }
        String method = HttpPost.METHOD_NAME;
        String endpoint = String.format("/%s/_delete_by_query",indexName);
        HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
        Response response = restClient.performRequest(method,endpoint,restClient.getDefaultParameters(),entity);
        JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        JSONArray failures = jsonObject.getJSONArray("failures");
        if(failures !=null && failures.size()>0){
            failures.forEach( failure -> logger.error("" + failure));
        }
        return jsonObject.getInteger("deleted");
    }

    /**
     * 适用于2.x ,5.x ,6.x ，响应有差异的情况（请求有无差异皆可），需要手动处理json格式的相应
     * @param restClient
     * @param searchRequest
     * @param headers
     * @return
     * @throws IOException
     */
    public static String search(HanRestClient restClient, SearchRequest searchRequest, Header... headers) throws IOException {
        String responseStr = null;
        Request req = Request.search(searchRequest);
        if(searchRequest.indices().length == 0){
            logger.warn("es search must have index name");
            throw new IOException("es search must have index name");
        }
        String json = EsDSLHandler.getEsDSLHandler().handleJson(searchRequest.source().toString());
        HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
        Map<String,String> parameters = new HashMap<>(req.getParameters());
        parameters.put("typed_keys", "false");//false不修改返回的聚合条件的别名，如果为true，会在别名前加sterms#
        parameters.put("ignore_unavailable", "true");//忽略不可用的索引，这包括不存在的索引或关闭的索引
        logger.debug(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                req.getMethod(), req.getEndpoint(), parameters, json));
        try {
            Response response = restClient.performRequest(req.getMethod(), req.getEndpoint(),parameters,entity, headers);
            responseStr = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (IOException e) {
            //追加日志打印，便于排查访问低版本es时的问题
            logger.error(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                    req.getMethod(), req.getEndpoint(), parameters, json));
            throw new IOException(e);
        }
        return responseStr;
    }

}
