package com.sp.infra.comp.rocketmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Data
@ConfigurationProperties(prefix = "rocketmq.consumer")
@Component
public class TopicAndTagProperties {

    /**
     * 并发消费主题和标记
     */
    private List<TopicAndTagInfo> topicAndTagInfos;

    /**
     * 顺序消费主题和标记
     */
    private List<TopicAndTagInfo> orderlyTopicAndTagInfos;

    /**
     * 广播消费主题和标记
     */
    private List<TopicAndTagInfo> broadcastTopicAndTagInfos;
}
