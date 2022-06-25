package com.sp.infra.comp.rocketmq.properties;

/**
 * Topic和Tag信息
 */

import lombok.Data;

@Data
public class TopicAndTagInfo {

    /**
     * MQ主题
     */
    private String topic;

    /**
     * MQ tag
     */
    private String tag;

    /**
     * 处理类(注册在spring上下文)
     */
    private String processorHandle;
}
