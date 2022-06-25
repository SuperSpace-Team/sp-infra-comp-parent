package com.sp.infra.comp.rocketmq.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RocketMQUtils {
    private static Map<Integer, Integer> levelMap = new TreeMap<>();

    public static AtomicInteger count = new AtomicInteger(1);
    public static String SP_SCHEDULE_TOPIC = "sp_delay_schedule_topic";
    public static String SP_SCHEDULE_TAG = "delay_tag";
    public static String SP_SCHEDULE_GROUP_NAME = "sp_delay_schedule_group";


    /**
     * rocket mq 支持的level value list
     * add by daneil
     */
    private static List<Integer> levelValues = Arrays.asList(
            2 * 60 * 60, 60 * 60, 30 * 60, 20 * 60, 10 * 60, 9 * 60, 8 * 60, 7 * 60, 6 * 60,
            5 * 60, 4 * 60, 3 * 60, 2 * 60, 60, 30, 10, 5, 1
    );

    /**
     * 初始化mq level map
     */
    static {
        levelMap.put(2 * 60 * 60, 18);
        levelMap.put(60 * 60, 17);
        levelMap.put(30 * 60, 16);
        levelMap.put(20 * 60, 15);
        levelMap.put(10 * 60, 14);
        levelMap.put(9 * 60, 13);
        levelMap.put(8 * 60, 12);
        levelMap.put(7 * 60, 11);
        levelMap.put(6 * 60, 10);
        levelMap.put(5 * 60, 9);
        levelMap.put(4 * 60, 8);
        levelMap.put(3 * 60, 7);
        levelMap.put(2 * 60, 6);
        levelMap.put(60, 5);
        levelMap.put(30, 4);
        levelMap.put(10, 3);
        levelMap.put(5, 2);
        levelMap.put(1, 1);
    }

    public static String concatKey(String topic, String tag) {
        StringBuilder sb = new StringBuilder();
        return (sb.append(topic)
                .append("-")
                .append(tag))
                .toString();
    }

    /**
     * 获取目标日期的mq level
     * @param targetDate
     * @return
     */
    public static int getLevel(Date targetDate) {
        Date now = new Date();
        long secondDiff = DateUtil.between(now, targetDate, TimeUnit.SECONDS);
        for (Integer levelValue : levelValues) {
            if (secondDiff >= levelValue) {
                    Integer level = levelMap.get(levelValue);
                    log.debug("目标时间为：{},剩余{}秒，选择最大的level值为：{},MQ level为{}",targetDate,secondDiff,levelValue,level);
                    return level;
            }
        }
        return 0;
    }
}
