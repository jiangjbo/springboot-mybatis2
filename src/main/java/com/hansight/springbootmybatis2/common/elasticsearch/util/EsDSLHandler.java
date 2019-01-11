package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.request.EsDSLHandlerInterface;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.request.EsDSLHandler_2x;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.request.EsDSLHandler_5x;
import com.hansight.springbootmybatis2.common.elasticsearch.multiversionsupport.request.EsDSLHandler_6x;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 13:14 2018/8/1
 * @Modified By:
 */
public class EsDSLHandler {

    private static final Logger logger = LoggerFactory.getLogger(EsDSLHandler.class);

    private static Lock lock = new ReentrantLock();

    private static volatile EsDSLHandler esDSLHandler;

    private volatile EsDSLHandlerInterface esDSLHandlerInterface;

    private EsDSLHandler(){
        initEsDSLHandlerInterface();
    }

    private void initEsDSLHandlerInterface(){
        String version = EsInfoManagement.getVersion();
        String firstVersion = version.substring(0, 1);
        switch (firstVersion){
            case "2" :
                esDSLHandlerInterface = new EsDSLHandler_2x();break;
            case "5" :
                esDSLHandlerInterface = new EsDSLHandler_5x();break;
            case "6" :
                esDSLHandlerInterface = new EsDSLHandler_6x();break;
            default :
                esDSLHandlerInterface = new EsDSLHandler_5x();break;
        }
        logger.info("new EsDSLHandler instance success.{}", esDSLHandlerInterface.toString());
    }

    public static EsDSLHandler getEsDSLHandler() throws IOException{
        if(esDSLHandler == null) {
            try {
                if(lock.tryLock(1, TimeUnit.MINUTES)){
                    if(esDSLHandler == null){
                        esDSLHandler = new EsDSLHandler();
                        logger.info("new EsDSLHandler instance success.");
                    }
                }else{
                    logger.error("EsDSLHandler instance failed. Please check the es connection.");
                    throw new IOException("EsDSLHandler instance failed. Please check the es connection.");
                }
            } catch (InterruptedException e) {
                logger.error("", e);
            } finally {
                lock.unlock();
            }
        }
        return esDSLHandler;
    }

    public String handleJson(String json){
        return esDSLHandlerInterface.handleJson(json);
    }

}
