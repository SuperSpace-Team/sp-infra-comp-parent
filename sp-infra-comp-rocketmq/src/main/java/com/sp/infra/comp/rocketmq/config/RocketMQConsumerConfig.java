package com.sp.infra.comp.rocketmq.config;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import com.sp.framework.common.utils.JsonUtils;
import com.sp.infra.comp.rocketmq.listen.AbstractMessageListener;
import com.sp.infra.comp.rocketmq.listen.DefaultMessageListener;
import com.sp.infra.comp.rocketmq.listen.OrderlyMessageListener;
import com.sp.infra.comp.rocketmq.model.DelayMsg;
import com.sp.infra.comp.rocketmq.processor.MessageProcessor;
import com.sp.infra.comp.rocketmq.properties.TopicAndTagInfo;
import com.sp.infra.comp.rocketmq.properties.TopicAndTagProperties;
import com.sp.infra.comp.rocketmq.utils.RocketMQProducerUtil;
import com.sp.infra.comp.rocketmq.utils.RocketMQUtils;
import com.sp.infra.comp.rocketmq.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RocketMQConsumerConfig {

    public static final String MQ_ADDRESS_INVALID_MSG = "MQ地址未配置";
    @Autowired
    private SpringContextUtil springContextUtil;

    @Value("${rocketmq.namesrvAddr:#{null}}")
    private String namesrvAddr;

    @Value("${rocketmq.consumer.groupName:#{null}}")
    private String groupName;

    @Value("${rocketmq.consumer.orderlyGroupName:#{null}}")
    private String orderlyGroupName;

    @Value("${rocketmq.consumer.broadcastGroupName:#{null}}")
    private String broadcastGroupName;

    @Value("${rocketmq.consumer.consumeThreadMin:20}")
    private int consumeThreadMin;

    @Value("${rocketmq.consumer.consumeThreadMax:64}")
    private int consumeThreadMax;

    @Resource
    private TopicAndTagProperties topicAndTagProperties;

    @Autowired
    private RocketMQProducerUtil rocketMQProducerUtil;


    @Bean("scheduleMQPushConsumer")
    @ConditionalOnProperty(name="rocketmq.enableDelay",havingValue = "true")
    public DefaultMQPushConsumer getScheduleRocketMQConsumer() throws MQClientException {
        String groupName = RocketMQUtils.SP_SCHEDULE_GROUP_NAME;
        Assert.notNull(namesrvAddr, MQ_ADDRESS_INVALID_MSG);
        DefaultMQPushConsumer consumer = buildDefaultMQPushConsumer(groupName);
        consumer.subscribe(RocketMQUtils.SP_SCHEDULE_TOPIC, RocketMQUtils.SP_SCHEDULE_TAG);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
                MessageExt messageExt = list.get(0);
                String message = new String(messageExt.getBody());
                DelayMsg delayMsg = JsonUtils.readObject(message, DelayMsg.class);
                String topic = delayMsg.getTopic();
                String contentText = delayMsg.getContentText();
                Date eventDelayAt = delayMsg.getEventDelayAt();
                String keys = delayMsg.getKeys();
                String tags = delayMsg.getTags();
                rocketMQProducerUtil.sendScheduleMessage(topic, tags, keys, contentText, eventDelayAt);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("concurrentlyMQPushConsumer")
    public DefaultMQPushConsumer getRocketMQConsumer() {
        List<TopicAndTagInfo> topicAndTagInfos = Optional.ofNullable(topicAndTagProperties.getTopicAndTagInfos()).orElse(Collections.emptyList());
        if (noNeedToCreateConsumer(topicAndTagInfos, groupName)) {
            return null;
        }

        Assert.notNull(namesrvAddr, MQ_ADDRESS_INVALID_MSG);

        DefaultMQPushConsumer consumer = buildDefaultMQPushConsumer(groupName);
        //监听类
        DefaultMessageListener messageListener = new DefaultMessageListener();
        //注册消息处理器
        registerHandler(topicAndTagInfos, messageListener);
        //注册监听器
        consumer.registerMessageListener(messageListener);
        subscribeTopicAndTag(topicAndTagInfos, consumer);
        return consumer;
    }


    @Bean("orderlyMQPushConsumer")
    public DefaultMQPushConsumer getOrderlyRocketMQConsumer() {
        List<TopicAndTagInfo> topicAndTagInfos = Optional.ofNullable(topicAndTagProperties.getOrderlyTopicAndTagInfos()).orElse(Collections.emptyList());
        if (noNeedToCreateConsumer(topicAndTagInfos, orderlyGroupName)) {
            return null;
        }
        Assert.notNull(namesrvAddr, "MQ地址未配置");

        DefaultMQPushConsumer consumer = buildDefaultMQPushConsumer(orderlyGroupName);
        //监听类
        OrderlyMessageListener messageListener = new OrderlyMessageListener();
        //注册消息处理器
        registerHandler(topicAndTagInfos, messageListener);
        //注册监听器
        consumer.registerMessageListener(messageListener);
        subscribeTopicAndTag(topicAndTagInfos, consumer);
        return consumer;
    }

    @Bean("broadcastMQPushConsumer")
    public DefaultMQPushConsumer getBroadcastRocketMQConsumer() {
        List<TopicAndTagInfo> topicAndTagInfos = Optional.ofNullable(topicAndTagProperties.getBroadcastTopicAndTagInfos()).orElse(Collections.emptyList());
        if (noNeedToCreateConsumer(topicAndTagInfos, broadcastGroupName)) {
            return null;
        }
        Assert.notNull(namesrvAddr, "MQ地址未配置");

        DefaultMQPushConsumer consumer = buildBroadcastMQPushConsumer(broadcastGroupName);

        //监听类
        DefaultMessageListener messageListener = new DefaultMessageListener();
        //注册消息处理器
        registerHandler(topicAndTagInfos, messageListener);
        //注册监听器
        consumer.registerMessageListener(messageListener);
        subscribeTopicAndTag(topicAndTagInfos, consumer);
        return consumer;
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
     * 消费者订阅topic tag
     *
     * @param topicAndTagInfos
     * @param consumer
     */
    private void subscribeTopicAndTag(List<TopicAndTagInfo> topicAndTagInfos, DefaultMQPushConsumer consumer) {
        try {
            for (TopicAndTagInfo topicAndTagInfo : topicAndTagInfos) {
                consumer.subscribe(topicAndTagInfo.getTopic(), topicAndTagInfo.getTag());
            }
            consumer.start();
            log.info("consumer is start !!! groupName:{},topicAndTag:{},namesrvAddr:{}", groupName, JSON.toJSONString(topicAndTagInfos), namesrvAddr);
        } catch (MQClientException e) {
            log.error("consumer is start !!! groupName:{},topicAndTag:{},namesrvAddr:{}", groupName, JSON.toJSONString(topicAndTagInfos), namesrvAddr, e);
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * 注册消息处理器
     *
     * @param topicAndTagInfos
     * @param messageListen
     */
    private void registerHandler(List<TopicAndTagInfo> topicAndTagInfos, AbstractMessageListener messageListen) {
        for (TopicAndTagInfo topicAndTagInfo : topicAndTagInfos) {
            MessageProcessor msgProcessor = (MessageProcessor) springContextUtil.getBean(topicAndTagInfo.getProcessorHandle());
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

    /**
     * 创建消费者
     *
     * @return
     */
    private DefaultMQPushConsumer buildDefaultMQPushConsumer(String groupName) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.setVipChannelEnabled(false);
        //批次中最大信息条数(默认也是1)
        consumer.setConsumeMessageBatchMaxSize(1);
        return consumer;
    }

    private DefaultMQPushConsumer buildBroadcastMQPushConsumer(String groupName) {
        DefaultMQPushConsumer consumer = buildDefaultMQPushConsumer(groupName);
        consumer.setMessageModel(MessageModel.BROADCASTING);
        return consumer;
    }
}
