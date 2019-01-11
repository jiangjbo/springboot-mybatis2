package com.hansight.springbootmybatis2.demo.controller;

import com.hansight.spider.annotation.Multilingual;
import com.hansight.spider.message.MultilingualMessageFormat;
import com.hansight.springbootmybatis2.common.utils.AtomRestfulResult;
import com.hansight.springbootmybatis2.demo.pojo.User;
import com.hansight.springbootmybatis2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @Multilingual
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public AtomRestfulResult query(){
        AtomRestfulResult atomRestfulResult = new AtomRestfulResult();
        atomRestfulResult.setStatusCode(AtomRestfulResult.STATUS_CODE_SUCCESS);
        atomRestfulResult.addMessages("demo.user.query");
        atomRestfulResult.addData(userService.users());
        return atomRestfulResult;
    }

}
