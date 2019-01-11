package com.hansight.springbootmybatis2.common.elasticsearch.factory;

import com.hansight.springbootmybatis2.common.interceptor.ESDataPolicyInterceptor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HanRestClient {

    private RestClient restClient;

    private static Map<String, String> DEFAULT_PARAMETERS = new HashMap<>();

    static {
        DEFAULT_PARAMETERS.put("ignore_unavailable", "true");//忽略不可用的索引，这包括不存在的索引或关闭的索引
        DEFAULT_PARAMETERS.put("expand_wildcards", "open");//设置是否扩展通配符到closed的index中，open表示只在匹配并为open的index中查询，closed表示在匹配的所有的index中查询, 默认为closed
        DEFAULT_PARAMETERS.put("allow_no_indices", "true");//是否忽略通配符匹配不到index(不存在或关闭)的情况, true表示允许, false表示不允许
    }

    public Map<String, String> getDefaultParameters(){
        return  DEFAULT_PARAMETERS;
    }

    public HanRestClient(RestClient restClient){
        this.restClient = restClient;
    }

    public synchronized void setHosts(HttpHost... hosts) {
        restClient.setHosts(hosts);
    }

    public void close() throws IOException {
        restClient.close();
    }

    public Response performRequest(String method, String endpoint, Header... headers) throws IOException {
        return restClient.performRequest(method, endpoint, Collections.<String, String>emptyMap(), null, addUserId(headers));
    }

    public Response performRequest(String method, String endpoint, Map<String, String> params, Header... headers) throws IOException {
        return restClient.performRequest(method, endpoint, params, (HttpEntity)null, addUserId(headers));
    }

    public Response performRequest(String method, String endpoint, Map<String, String> params,
                                   HttpEntity entity, Header... headers) throws IOException {
        return restClient.performRequest(method, endpoint, params, entity, HttpAsyncResponseConsumerFactory.DEFAULT, addUserId(headers));
    }

    public Response performRequest(String method, String endpoint, Map<String, String> params,
                                   HttpEntity entity, HttpAsyncResponseConsumerFactory httpAsyncResponseConsumerFactory,
                                   Header... headers) throws IOException {
        return restClient.performRequest(method,endpoint,params,entity,httpAsyncResponseConsumerFactory,addUserId(headers));
    }

    public void performRequestAsync(String method, String endpoint, ResponseListener responseListener, Header... headers) {
        restClient.performRequestAsync(method,endpoint,responseListener,addUserId(headers));
    }

    public void performRequestAsync(String method, String endpoint, Map<String, String> params,
                                    ResponseListener responseListener, Header... headers) {
        restClient.performRequestAsync(method, endpoint, params, responseListener, addUserId(headers));
    }

    public void performRequestAsync(String method, String endpoint, Map<String, String> params,
                                    HttpEntity entity, ResponseListener responseListener, Header... headers) {
        restClient.performRequestAsync(method, endpoint, params, entity, responseListener, addUserId(headers));
    }

    public void performRequestAsync(String method, String endpoint, Map<String, String> params,
                                    HttpEntity entity, HttpAsyncResponseConsumerFactory httpAsyncResponseConsumerFactory,
                                    ResponseListener responseListener, Header... headers){
        restClient.performRequestAsync(method,endpoint,params,entity,httpAsyncResponseConsumerFactory,responseListener,addUserId(headers));
    }

    public Header[] addUserId(Header... headers){
        if (ESDataPolicyInterceptor.userId.get() != null) {
            Header userID = new BasicHeader(ESDataPolicyInterceptor.USER_ID, ESDataPolicyInterceptor.userId.get());
            headers = Arrays.copyOf(headers,headers.length+1);
            headers[headers.length-1] = userID;
            return headers;
        }
        return headers;
    }
}
