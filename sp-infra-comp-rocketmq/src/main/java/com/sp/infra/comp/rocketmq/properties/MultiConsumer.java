package com.sp.infra.comp.rocketmq.properties;

import lombok.Data;

import java.util.List;

/**
 * @author Wang Chong
 * @Date 2019/10/27
 **/
@Data
public class MultiConsumer {
    private String nameAddr;
    private String groupName;
    private int consumeThreadMin = 30;
    private int consumeThreadMax=64;
    private List<TopicAndTagInfo> topicAndTagInfos;
    /**
     * 暂时无用
     */
    private int batchSize= 1;
    private int consumeMode = 1;
    private String instanceName;
}
