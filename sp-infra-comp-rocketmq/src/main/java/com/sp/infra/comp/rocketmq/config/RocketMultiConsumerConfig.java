package com.sp.infra.comp.rocketmq.config;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.rocketmq.listen.AbstractMessageListener;
import com.sp.infra.comp.rocketmq.listen.DefaultMessageListener;
import com.sp.infra.comp.rocketmq.listen.OrderlyMessageListener;
import com.sp.infra.comp.rocketmq.processor.MessageProcessor;
import com.sp.infra.comp.rocketmq.properties.MultiConsumer;
import com.sp.infra.comp.rocketmq.properties.MultiConsumerProperties;
import com.sp.infra.comp.rocketmq.properties.TopicAndTagInfo;
import com.sp.infra.comp.rocketmq.utils.RocketMQUtils;
import com.sp.infra.comp.rocketmq.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Wang Chong
 * @Date 2019/10/27
 **/
@Configuration
@EnableConfigurationProperties(MultiConsumerProperties.class)
@Slf4j
public class RocketMultiConsumerConfig implements SmartInitializingSingleton {
    private final MultiConsumerProperties multiConsumerProperties;

    public RocketMultiConsumerConfig(MultiConsumerProperties multiConsumerProperties) {
        this.multiConsumerProperties = multiConsumerProperties;
    }

    /**
     * 监听Topic 或 groupName 没有设置, 返回空, 不需要创建消费者
     *
     * @param topicAndTagInfos
     * @param groupName
     * @return
     */
    private boolean noNeedToCreateConsumer(List<TopicAndTagInfo> topicAndTagInfos, String groupName) {
        return CollectionUtils.isEmpty(topicAndTagInfos) || StringUtils.isBlank(groupName);
    }

    /**
     * 创建消费者
     *
     * @return
     */
    private DefaultMQPushConsumer buildDefaultMQPushConsumer(MultiConsumer multiConsumer) {
        List<TopicAndTagInfo> topicAndTagInfos = Optional.ofNullable(multiConsumer.getTopicAndTagInfos()).orElse(Collections.emptyList());

        int batchSize = multiConsumer.getBatchSize();
        int consumeMode = multiConsumer.getConsumeMode();
        int consumeThreadMax = multiConsumer.getConsumeThreadMax();
        String groupName = multiConsumer.getGroupName();
        String nameAddr = multiConsumer.getNameAddr();
        String instanceName = multiConsumer.getInstanceName();
        int consumeThreadMin = multiConsumer.getConsumeThreadMin();
        if (noNeedToCreateConsumer(topicAndTagInfos, groupName)) {
            return null;
        }
        Assert.notNull(nameAddr, "MQ地址未配置");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(nameAddr);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.setVipChannelEnabled(false);
        if(StringUtils.isNotEmpty(instanceName)){
            consumer.setInstanceName(instanceName);
        }
        //批次中最大信息条数(默认也是1)
        consumer.setConsumeMessageBatchMaxSize(1);
        if (consumeMode == ConsumeMode.BROADCASTING.getCode()) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        } else {
            consumer.setMessageModel(MessageModel.CLUSTERING);
        }

        if (consumeMode == ConsumeMode.ORDERLY.getCode()) {
            //监听类
            OrderlyMessageListener messageListener = new OrderlyMessageListener();
            //注册消息处理器
            registerHandler(topicAndTagInfos, messageListener);
            //注册监听器
            consumer.registerMessageListener(messageListener);
        } else {
            //监听类
            DefaultMessageListener messageListener = new DefaultMessageListener();
            //注册消息处理器
            registerHandler(topicAndTagInfos, messageListener);
            //注册监听器
            consumer.registerMessageListener(messageListener);
        }

        subscribeTopicAndTag(topicAndTagInfos, consumer, groupName);

        return consumer;
    }

    /**
     * 消费者订阅topic tag
     *
     * @param topicAndTagInfos
     * @param consumer
     * @param groupName
     */
    private void subscribeTopicAndTag(List<TopicAndTagInfo> topicAndTagInfos, DefaultMQPushConsumer consumer, String groupName) {
        try {
            for (TopicAndTagInfo topicAndTagInfo : topicAndTagInfos) {
                consumer.subscribe(topicAndTagInfo.getTopic(), topicAndTagInfo.getTag());
            }
            consumer.start();
            log.info("consumer is start !!! groupName:{},topicAndTag:{}", groupName, JSON.toJSONString(topicAndTagInfos));
        } catch (MQClientException e) {
            log.error("consumer is start !!! groupName:{},topicAndTag:{}", groupName, JSON.toJSONString(topicAndTagInfos), e);
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * 注册消息处理器
     *
     * @param topicAndTagInfos
     * @param messageListen
     */
    protected void registerHandler(List<TopicAndTagInfo> topicAndTagInfos, AbstractMessageListener messageListen) {
        for (TopicAndTagInfo topicAndTagInfo : topicAndTagInfos) {

            MessageProcessor msgProcessor = (MessageProcessor) SpringContextUtil.getBean(topicAndTagInfo.getProcessorHandle());
            //可能是 || 分隔开的 tag列表
            String tags = topicAndTagInfo.getTag();

            if (StringUtils.isBlank(tags) || StringUtils.equalsIgnoreCase(tags.trim(), "*")) {
                messageListen.registerHandler(topicAndTagInfo.getTopic(),
                        msgProcessor);
            } else {
                String[] tagArray = tags.split("\\|\\|");
                for (String tag : tagArray) {
                    tag = tag.trim();
                    if (StringUtils.isNotBlank(tag)) {
                        messageListen.registerHandler(RocketMQUtils.concatKey(topicAndTagInfo.getTopic(), tag),
                                msgProcessor);
                    }
                }
            }
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<MultiConsumer> consumers = multiConsumerProperties.getConsumers();
        if(!CollectionUtils.isEmpty(consumers)){
            consumers.forEach(this::buildDefaultMQPushConsumer);
        }
    }
}
