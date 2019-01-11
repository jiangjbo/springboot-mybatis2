package com.hansight.springbootmybatis2.rest.config.service;

import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.rest.config.dao.SystemConfigMapper;
import com.hansight.springbootmybatis2.rest.config.enums.LanguageEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    @Autowired
    SystemConfigMapper systemConfigMapper;

    public LanguageEnum systemLanguage(){
        String config = systemConfigMapper.systemLanguage();
        if(StringUtils.isBlank(config)){
            return LanguageEnum.CN;
        }
        JSONObject jsonObject = JSONObject.parseObject(systemConfigMapper.systemLanguage());
        return LanguageEnum.getEnum(jsonObject.getString("primary_language"));
    }
}
