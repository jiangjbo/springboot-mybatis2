package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.hansight.springbootmybatis2.common.interceptor.ESDataPolicyInterceptor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 10:11 2018/7/22
 * @Modified By:
 */
public class ESSearchUtilWithoutDataPolicy extends ESSearchUtil{

    private static Logger logger = LoggerFactory.getLogger(ESSearchUtilWithoutDataPolicy.class);

    private static volatile ESSearchUtilWithoutDataPolicy esSearchUtil;

    private static Lock lock = new ReentrantLock();

    private ESSearchUtilWithoutDataPolicy() {}

    public static ESSearchUtilWithoutDataPolicy getESSearchUtil(){
        if(esSearchUtil == null) {
            try {
                lock.lock();
                if(esSearchUtil == null){
                    esSearchUtil = new ESSearchUtilWithoutDataPolicy();
                    logger.info("new ESSearchUtilWithoutDataPolicy instance success.");
                }
            } finally {
                lock.unlock();
            }
        }
        return esSearchUtil;
    }

    @Override
    protected SearchSourceBuilder buildSearchSource(int size, String[] fetchFields, QueryBuilder queryBuilder, AggregationBuilder[] aggregationBuilders, SortBuilder... sortBuilders){
        ESDataPolicyInterceptor.userId.set(null);//设置userId为null,不进行权限控制
        return super.buildSearchSource(size, fetchFields, queryBuilder, aggregationBuilders, sortBuilders);
    }
}
