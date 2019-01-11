package com.hansight.springbootmybatis2.common.elasticsearch.factory;

import com.hansight.springbootmybatis2.common.elasticsearch.util.EsDSLHandler;
import com.hansight.springbootmybatis2.common.interceptor.ESDataPolicyInterceptor;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HanRestHighLevelClient extends RestHighLevelClient{

    private static final Logger logger = LoggerFactory.getLogger(HanRestHighLevelClient.class);

    private RestClient restClient;

    public HanRestHighLevelClient(RestClient restClient){
        super(restClient);
        this.restClient = restClient;
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest, Header... headers) throws IOException {
        return super.search(searchRequest, addUserId(headers));
    }

    @Override
    public void searchAsync(SearchRequest searchRequest, ActionListener<SearchResponse> listener, Header... headers) {
        super.searchAsync(searchRequest, listener, addUserId(headers));
    }
    @Override
    public SearchResponse searchScroll(SearchScrollRequest searchScrollRequest, Header... headers) throws IOException {
        return super.searchScroll(searchScrollRequest, addUserId(headers));
    }
    @Override
    public void searchScrollAsync(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> listener, Header... headers) {
        super.searchScrollAsync(searchScrollRequest, listener, addUserId(headers));
    }

    private Header[] addUserId(Header... headers){
        if (ESDataPolicyInterceptor.userId.get() != null) {
            Header userID = new BasicHeader(ESDataPolicyInterceptor.USER_ID, ESDataPolicyInterceptor.userId.get());
            headers = Arrays.copyOf(headers,headers.length+1);
            headers[headers.length-1] = userID;
            return headers;
        }
        return headers;
    }

    //去除了校验，添加了版本兼容
    @Override
    protected <Req extends ActionRequest, Resp> Resp performRequest(Req request,
                                                                    CheckedFunction<Req, Request, IOException> requestConverter,
                                                                    CheckedFunction<Response, Resp, IOException> responseConverter,
                                                                    Set<Integer> ignores, Header... headers) throws IOException {
        //对search请求做多版本es的兼容，其他格式的请求，目前来看，无需做兼容
        if(request instanceof SearchRequest){
            Request req = requestConverter.apply(request);
            HttpEntity entity = req.getEntity();
            String json = "";
            if(entity != null){
                json = EsDSLHandler.getEsDSLHandler().handleJson(EntityUtils.toString(entity, Consts.UTF_8));
                entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            }
            Map<String,String> parameters = new HashMap<>(req.getParameters());
            if(parameters.containsKey("ignore_unavailable")){
                parameters.put("ignore_unavailable", "true");//忽略不可用的索引，这包括不存在的索引或关闭的索引
            }
            Response response;
            try {
                logger.debug(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                        req.getMethod(), req.getEndpoint(), parameters, json));
                response = restClient.performRequest(req.getMethod(), req.getEndpoint(), parameters, entity, headers);
            } catch (ResponseException e) {
                //追加日志打印，便于排查访问低版本es时的问题
                logger.error(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                        req.getMethod(), req.getEndpoint(), parameters, json));
                if (ignores.contains(e.getResponse().getStatusLine().getStatusCode())) {
                    try {
                        return responseConverter.apply(e.getResponse());
                    } catch (Exception innerException) {
                        throw new IOException(e);
                    }
                }
                throw new IOException(e);
            }
            try {
                return responseConverter.apply(response);
            } catch(Exception e) {
                logger.error(EntityUtils.toString(response.getEntity(), Consts.UTF_8), e);
                throw new IOException("Unable to parse response body for " + response, e);
            }
        } else{
            return super.performRequest(request, requestConverter, responseConverter, ignores, headers);
        }
    }

    //去除了校验，添加了版本兼容
    @Override
    protected <Req extends ActionRequest, Resp> void performRequestAsync(Req request,
                                                                         CheckedFunction<Req, Request, IOException> requestConverter,
                                                                         CheckedFunction<Response, Resp, IOException> responseConverter,
                                                                         ActionListener<Resp> listener, Set<Integer> ignores, Header... headers) {
        //对search请求做多版本es的兼容，其他格式的请求，目前来看，无需做兼容
        if(request instanceof SearchRequest){
            try {
                Request req = requestConverter.apply(request);
                HttpEntity entity = req.getEntity();
                String json = "";
                if(entity != null){
                    json = EsDSLHandler.getEsDSLHandler().handleJson(EntityUtils.toString(entity, Consts.UTF_8));
                    entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
                }
                Map<String,String> parameters = new HashMap<>(req.getParameters());
                if(parameters.containsKey("ignore_unavailable")){
                    parameters.put("ignore_unavailable", "true");//忽略不可用的索引，这包括不存在的索引或关闭的索引
                }
                try {
                    logger.debug(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                            req.getMethod(), req.getEndpoint(), parameters, json));
                    ResponseListener responseListener = wrapResponseListener(responseConverter, listener, ignores);
                    restClient.performRequestAsync(req.getMethod(), req.getEndpoint(), parameters, entity, responseListener, headers);
                } catch (Exception e) {
                    //追加日志打印，便于排查访问低版本es时的问题
                    logger.error(String.format("httpMethod: %s, uri: %s, parameters; %s, dsl: %s",
                            req.getMethod(), req.getEndpoint(), parameters, json));
                    throw new Exception(e);
                }
            } catch (Exception e) {
                listener.onFailure(e);
            }
        } else{
            super.performRequestAsync(request, requestConverter, responseConverter, listener, ignores, headers);
        }
    }

    <Resp> ResponseListener wrapResponseListener(CheckedFunction<Response, Resp, IOException> responseConverter,
                                                 ActionListener<Resp> actionListener, Set<Integer> ignores) {
        return new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                try {
                    actionListener.onResponse(responseConverter.apply(response));
                } catch(Exception e) {
                    try {
                        logger.error(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
                    } catch (IOException e1) {
                        logger.error("", e1);
                    }
                    IOException ioe = new IOException("Unable to parse response body for " + response, e);
                    onFailure(ioe);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof ResponseException) {
                    ResponseException responseException = (ResponseException) exception;
                    Response response = responseException.getResponse();
                    if (ignores.contains(response.getStatusLine().getStatusCode())) {
                        try {
                            actionListener.onResponse(responseConverter.apply(response));
                        } catch (Exception innerException) {
                            //the exception is ignored as we now try to parse the response as an error.
                            //this covers cases like get where 404 can either be a valid document not found response,
                            //or an error for which parsing is completely different. We try to consider the 404 response as a valid one
                            //first. If parsing of the response breaks, we fall back to parsing it as an error.
                            actionListener.onFailure(parseResponseException(responseException));
                        }
                    } else {
                        actionListener.onFailure(parseResponseException(responseException));
                    }
                } else {
                    actionListener.onFailure(exception);
                }
            }
        };
    }

}
