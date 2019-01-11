package com.hansight.springbootmybatis2.common.elasticsearch.util;

import org.springframework.context.ApplicationContext;

/**
 * Created by ying on 2017/10/25.
 */
public class SpringUtil {
    private static ApplicationContext ac ;

    public static void setAc(ApplicationContext a){
        ac = a;
    }

    public static <T> T getBean(Class<T> clazz ){
        try {
            return ac.getBean(clazz);
        }catch (Exception e){
            return null;
        }
    }
}
