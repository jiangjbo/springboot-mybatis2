package com.hansight.springbootmybatis2.common.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class ESDataPolicyInterceptorConfiguration implements WebMvcConfigurer{

    @Resource
    private  ESDataPolicyInterceptor esDataPolicyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 自定义拦截器，添加拦截路径和排除拦截路径
        registry.addInterceptor(esDataPolicyInterceptor).addPathPatterns("/**");
    }

}
