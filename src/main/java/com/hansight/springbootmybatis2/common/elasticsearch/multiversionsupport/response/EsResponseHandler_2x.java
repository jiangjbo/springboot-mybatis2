package com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.response;

import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.elasticsearch.util.EsInfoManagement;
import com.hansight.springbootmybatis2.common.elasticsearch.util.EsMappingTemplateManagement;
import com.hansight.springbootmybatis2.common.elasticsearch.util.SpringUtil;
import com.hansight.springbootmybatis2.common.utils.ExecutorsUtil;
import com.hansight.springbootmybatis2.common.utils.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class EsResponseHandler_2x extends EsResponseHandler_default {

    private static volatile Set<String> templateIP;

    @Autowired
    private ESRestClient esRestClient;

    @PostConstruct
    public void init() {
        String version = EsInfoManagement.getVersion();
        String firstVersion = version.substring(0, 1);
        if(firstVersion.startsWith("2")){
            templateIP = getTemplateIp();
            ScheduledExecutorService scheduledExecutorService = ExecutorsUtil.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("ESSearchResultUtil-es-template-%d").build());
            scheduledExecutorService.scheduleAtFixedRate(() ->{
                Set<String> ips = getTemplateIp();
                if(!ips.isEmpty()){
                    templateIP = ips;
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    private Set<String> getTemplateIp(){
        Set<String> ips = new HashSet<>();
        Map<String, Set<String>> ipFields = EsMappingTemplateManagement.getAllEsTemplateFieldByType(esRestClient,"ip");
        if(ipFields.isEmpty()){
            logger.error("es template don't set");
        }
        ipFields.values().forEach(ips::addAll);
        return ips;
    }

    @Override
    protected void parseKey(JSONObject srcJson, AggField aggField, JSONObject results){
        //以ip字段聚合时, 2.x, key_as_string字段返回的为ip, 5.x以上key字段返回的为ip
        if(isBucketAddress(aggField)){
            results.put(aggField.fieldName, srcJson.get("key_as_string"));
        }else{
            results.put(aggField.fieldName, srcJson.get("key"));
        }
    }

    private boolean isBucketAddress(AggField aggField){
        return AggType.BUCKET.equals(aggField.aggType) && templateIP.contains(aggField.srcFieldName);
    }

}
