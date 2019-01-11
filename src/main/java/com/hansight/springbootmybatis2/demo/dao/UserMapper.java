package com.hansight.springbootmybatis2.demo.dao;

import com.hansight.springbootmybatis2.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM system_user WHERE login_name = #{username}")
    public List<User> findByUsername(@Param("username") String username);

    @Select("SELECT config FROM system_user WHERE type = 'PRIMARY_LANGUAGE'")
    public List<User> systemLanguags();

}
