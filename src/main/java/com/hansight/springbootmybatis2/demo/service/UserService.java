package com.hansight.springbootmybatis2.demo.service;

import com.hansight.springbootmybatis2.demo.dao.UserMapper;
import com.hansight.springbootmybatis2.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> users(){
        return userMapper.findByUsername("admin");
    }
}
