package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.HanRestClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * es索引工具类
 * @ClassName: EsIndexNameManagement
 * @Description:
 * @author xuezhi_xu@hansight.com
 * @date 2016年9月26日 下午7:47:24
 *
 */
public class EsIndexNameManagement {
	private static Logger logger = LoggerFactory.getLogger(EsIndexNameManagement.class);

	public static boolean isIndexEnable(HanRestClient restClient, String indexName){
		boolean flag = false;
		if(StringUtils.isNotEmpty(indexName)){
			try {
				if(isIndexExist(restClient,indexName)&&!isIndexClose(restClient,indexName)){
					flag = true;
				}
			} catch (Exception e) {
				logger.error("",e);
				flag = false;
			}
		}
		return flag;
	}

	public static List<String> indexNames(ESRestClient esRestClient){
		return indexNames(esRestClient,"_all");
	}

	/**
	 * 根据索引名获取可用索引（close的索引不返回）
	 *
	 * @param indexPatternName
	 * @return
	 */
	public static List<String> indexNames(ESRestClient esRestClient, String indexPatternName){
		List<String> indexNames = new ArrayList<>();
		HanRestClient restClient = esRestClient.getLowRestClient();
		String method = HttpGet.METHOD_NAME;
		String endpoint = String.format("/%s/_alias",indexPatternName);
		try {
			Response response = restClient.performRequest(method,endpoint);
			JSONObject jsonObject = JSONArray.parseObject(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
			indexNames = new ArrayList<>(jsonObject.keySet());
		} catch (IOException e) {
			logger.error("",e);
		}
		return indexNames;

	}

	//404 means index does not exist, and 200 means index does
	public static boolean isIndexExist(HanRestClient restClient, String index){
		String method = HttpHead.METHOD_NAME;
		try {
			String endpoint = String.format("/%s",index);
			Response response = restClient.performRequest(method, endpoint, Collections.emptyMap());
			logger.debug(response.toString());
			if(200 == response.getStatusLine().getStatusCode()){
				return true;
			}
		} catch (IOException e) {
			logger.error("",e);
		}
		return false;
	}

	public static boolean isIndexClose(HanRestClient restClient, String index) throws Exception {
		String method = HttpGet.METHOD_NAME;
		String endpoint = String.format("/%s/_stats/docs",index);
		try{
			restClient.performRequest(method, endpoint);
		}catch (ResponseException e){
			try {
				logger.debug("", e);
				Response response = e.getResponse();
				JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
				JSONObject error = jsonObject.getJSONObject("error");
				if("closed".equals(error.get("reason"))){
					return true;
				}else{
					throw new Exception(String.valueOf(error.get("reason")));
				}
			} catch (IOException e1) {
				throw new Exception(e1);
			}
		} catch (IOException e) {
			throw new Exception(e);
		}
		return false;
	}

}
