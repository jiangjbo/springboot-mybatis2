package com.hansight.springbootmybatis2.common.elasticsearch.util;

import com.hansight.springbootmybatis2.common.elasticsearch.factory.ESRestClient;
import com.hansight.springbootmybatis2.common.utils.TimeShiftUtil;
import org.elasticsearch.action.support.IndicesOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by richie on 17-8-10.
 */
public class EsUtil {

    private final Logger logger = LoggerFactory.getLogger(EsUtil.class);
    private final int RETRY_INFINITELY = Integer.MAX_VALUE;
    public static final IndicesOptions defaultIndicesOptions = IndicesOptions.fromOptions(true,true,true,false);

    /**
     * 获取指定时间可用索引列表
     * 对于获取长时间间隔的可用索引，推荐使用此方法
     * @param indexName     eg:event
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getIndicesWithLongTime(ESRestClient esRestClient, String indexName, long startTime, long endTime) {
        List<String> indices = EsIndexNameManagement.indexNames(esRestClient,indexName + "*");
        return getIndicesWithLongTime(indexName, startTime, endTime, indices);
    }

    //判断指定时间范围内的索引是否在指定的索引列表中
    public static List<String> getIndicesWithLongTime(String indexName, long startTime, long endTime, List<String> indices) {
        Set<String> sets = new HashSet<>();
        TimeShiftUtil tsu = TimeShiftUtil.getInstance();
        while (true) {
            if (startTime > endTime) {
                if (tsu.longToStringGetYMD(startTime).equals(tsu.longToStringGetYMD(endTime))) {
                    String index = getRelIndex(indexName, startTime);
                    if(indices.contains(index)){
                        sets.add(index);
                    }
                }
                break;
            } else {
                String index = getRelIndex(indexName, startTime);
                if(indices.contains(index)){
                    sets.add(index);
                }
            }

            startTime = startTime + 1000 * 60 * 60 * 24L;
        }
        return new ArrayList<>(sets);
    }

    /**
     * 获取指定时间内的索引列表，不进行索引是否存在，可用的判断
     *
     * @param indexName
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getIndicesWithoutValidate(String indexName, long startTime, long endTime) {
        Set<String> sets = new HashSet<>();
        TimeShiftUtil tsu = TimeShiftUtil.getInstance();
        while (true) {
            if (startTime > endTime) {
                if (tsu.longToStringGetYMD(startTime).equals(tsu.longToStringGetYMD(endTime))) {
                    String index = getRelIndex(indexName, startTime);
                    sets.add(index);
                }
                break;
            } else {
                String index = getRelIndex(indexName, startTime);
                sets.add(index);
            }
            startTime = startTime + 1000 * 60 * 60 * 24L;
        }
        return new ArrayList<>(sets);
    }

    public static String getIndexWithoutValidate(String indexName, long time) {
        return getRelIndex(indexName, time);
    }


    private static String getRelIndex(String indexName, long time){
        String tableName;
        switch(indexName){
            case "event": case "anomaly": case "monitor"://按天切
                tableName = indexName + "_" + TimeShiftUtil.getInstance().longToStringGetYMD(time);break;
            case "incident_alarm": case "incident_gragh": case "incident_related"://按月切
                tableName = indexName + "_" + TimeShiftUtil.getInstance().longToStringGetYM(time);break;
            default://incident,incident_disposal,incident_merge,security_assess_based_score,vulnerability_history,asset_pending,asset_update,asset_offline,asset_ignore,asset_confirmed,audit_log
                tableName = indexName;
        }
        return tableName;
    }
}
