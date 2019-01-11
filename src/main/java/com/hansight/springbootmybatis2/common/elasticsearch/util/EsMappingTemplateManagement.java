package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.HanRestClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
* @ClassName: EsMappingTemplateManagement 
* @Description:  ES mapping管理类
* @author xuezhi_xu@hansight.com
* @date 2016年9月26日 下午2:45:13 
*
 */
public class EsMappingTemplateManagement {
	
	private static Logger logger = LoggerFactory.getLogger(EsMappingTemplateManagement.class);

	/**
	 * 获取所有的模板名称和对应的模板
	 * @return
	 */
	public static Map<String, JSONObject> getAllEsTemplate(ESRestClient esRestClient){
		Map<String, JSONObject> templates = new HashMap<>();
		try {
			HanRestClient restClient = esRestClient.getLowRestClient();
			String method = HttpGet.METHOD_NAME;
			String endpoint = "/_template";
			Response response = restClient.performRequest(method, endpoint);
			JSONObject jsonObject = JSONArray.parseObject(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
			for(String templateName : jsonObject.keySet()){
				JSONObject templateObject = jsonObject.getJSONObject(templateName);
				templates.put(templateName, templateObject);
			}
		}catch(Exception e){
			logger.error("get template error error message : ", e);
		}
		return templates;
	}

	//根据字段类型，获取template中所有的字段
	public static Map<String, Set<String>> getAllEsTemplateFieldByType(ESRestClient esRestClient, String fieldType){
		Map<String, Set<String>> ipFields = new HashMap<>();
		try {
			Map<String, JSONObject> templates = getAllEsTemplate(esRestClient);
			for(String templateName : templates.keySet()){
				JSONObject templateObject = templates.get(templateName);
				String template = templateObject.getString("template");
				JSONObject mappings = templateObject.getJSONObject("mappings");
				if(StringUtils.isBlank(template) || mappings == null){
					continue;
				}
				Set<String> ips = new HashSet<>();
				ipFields.put(template, ips);
				for(String mappingName : mappings.keySet()){
					JSONObject properties = mappings.getJSONObject(mappingName).getJSONObject("properties");
					if(properties == null){
						continue;
					}
					for(String fieldName : properties.keySet()){
						if(fieldType.equalsIgnoreCase(properties.getJSONObject(fieldName).getString("type"))){
							ips.add(fieldName);
						}
					}
				}
			}
		}catch(Exception e){
			logger.error("get template error error message : ", e);
		}
		return ipFields;
	}

}
