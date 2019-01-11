package com.hansight.springbootmybatis2.common.multilingual;

import com.hansight.spider.aop.MultilingualAop;
import com.hansight.spider.listener.MultilingualRequestListener;
import com.hansight.spider.persistence.mybatis.MultilingualMybatisInterceptor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.plugin.Interceptor;

@Configuration
public class MultilingualConfig {

    @Bean
    MultilingualAop aop() {
        return new MultilingualAop();
    }

    @Bean
    ServletListenerRegistrationBean<MultilingualRequestListener> listener(){
        ServletListenerRegistrationBean<MultilingualRequestListener> servletListenerRegistrationBean = new ServletListenerRegistrationBean<>();
        servletListenerRegistrationBean.setListener(new MultilingualRequestListener());
        return servletListenerRegistrationBean ;
    }

    @Bean
    Interceptor interceptor(){
        return new MultilingualMybatisInterceptor();
    }

}
