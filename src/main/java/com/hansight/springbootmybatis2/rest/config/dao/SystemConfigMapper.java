package com.hansight.springbootmybatis2.rest.config.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper {

    @Select("SELECT config FROM system_config WHERE type = 'PRIMARY_LANGUAGE'")
    public String systemLanguage();

}
