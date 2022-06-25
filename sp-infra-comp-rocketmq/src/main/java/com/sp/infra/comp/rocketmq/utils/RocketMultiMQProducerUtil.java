package com.sp.infra.comp.rocketmq.utils;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.rocketmq.config.RocketMultiProducerConfig;
import com.sp.infra.comp.rocketmq.model.DelayMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Wang CHong
 */
@Slf4j
public class RocketMultiMQProducerUtil {
    private DefaultMQProducer defaultMQProducer;

    public RocketMultiMQProducerUtil(String instanceName) {
        DefaultMQProducer producer = RocketMultiProducerConfig.producerMaps.get(instanceName);
        this.defaultMQProducer = producer;
    }

    /**
     * 发送某天的定时消息
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @param targetDate
     * @return
     */
    public SendResult sendScheduleMessage(String topic, String tags, String keys, String contentText, Date targetDate) {
        checkParam(topic, tags, keys, contentText);

        String schedule_topic = RocketMQUtils.SP_SCHEDULE_TOPIC;
        DelayMsg delayMsg = DelayMsg.builder()
                .eventDelayAt(targetDate)
                .eventSendTime(new Date())
                .index(RocketMQUtils.count.get())
                .keys(keys)
                .tags(tags)
                .contentText(contentText)
                .topic(topic).build();

        String delayContent = JSON.toJSONString(delayMsg);
        Message message = new Message(schedule_topic, RocketMQUtils.SP_SCHEDULE_TAG, UUID.randomUUID().toString(), delayContent.getBytes());
        int level = RocketMQUtils.getLevel(targetDate);
        if (level > 0) {
            message.setDelayTimeLevel(level);
        } else {
            message = new Message(topic, tags, keys, contentText.getBytes());
        }
        generateTraceId(message);
        try {
            SendResult sendResult = this.defaultMQProducer.send(message);
            return sendResult;
        } catch (Exception e) {
            log.error("rocketMq message send error :{}", e.getMessage());
            e.printStackTrace();
            throw new SystemException("rocketMq message send error", e);
        }
    }

   /**
     * 发送普通消息的方法
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @return
     * @throws
     */
    public SendResult sendMessage(String topic, String tags, String keys, String contentText) {
        checkParam(topic, tags, keys, contentText);
        Message message = new Message(topic, tags, keys, contentText.getBytes());
        generateTraceId(message);
        try {
            SendResult sendResult = this.defaultMQProducer.send(message);
            return sendResult;
        } catch (Exception e) {
            log.error("rocketMq message send error :{}", e.getMessage());
            e.printStackTrace();
            throw new SystemException("rocketMq message send error", e);
        }
    }

    private void generateTraceId(Message message) {
        if (Objects.isNull(message)) {
            return;
        }
        try {
            message.putUserProperty(LogMdcUtil.TRACE_ID, LogMdcUtil.getOrDefaultMdc());
        } catch (Exception e) {
            //
        }
    }

    /**
     * 发送事务消息的方法
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @return
     * @throws
     */
    public SendResult sendMessageInTransaction(String topic, String tags, String keys, String contentText) {
        return this.sendMessageInTransaction(topic, tags, keys, contentText, null);
    }


    /**
     * 发送事务消息的方法
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @return
     * @throws
     */
    public SendResult sendMessageInTransaction(String topic, String tags, String keys, String contentText, Object arg) {
        checkParam(topic, tags, keys, contentText);
        Message message = new Message(topic, tags, keys, contentText.getBytes());
        generateTraceId(message);
        try {
            SendResult sendResult = this.defaultMQProducer.sendMessageInTransaction(message, arg);
            return sendResult;
        } catch (Exception e) {
            log.error("rocketMq transaction message send error :{}", e.getMessage());
            e.printStackTrace();
            throw new SystemException("rocketMq transaction message send error", e);
        }
    }


    public SendResult sendMessageOrderly(String topic, String tags, String keys, String contentText) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return sendMessageOrderly(topic, tags, keys, contentText, 1);
    }


    /**
     * 发送顺序消息
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @param queueIndex
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
    public SendResult sendMessageOrderly(String topic, String tags, String keys, String contentText, int queueIndex) throws InterruptedException,
            RemotingException,
            MQClientException, MQBrokerException {
        checkParam(topic, tags, keys, contentText);
        Message message = new Message(topic, tags, keys, contentText.getBytes());
        generateTraceId(message);
        //queueIndex 当作args传入MessageQueueSelector中
        return this.defaultMQProducer.send(message, (list, msg, index) -> list.get((Integer) index), queueIndex);
    }

    public SendResult sendMessageByHashOrderly(String topic, String tags, String keys, String contentText, String shardingKey) throws InterruptedException,
            RemotingException,
            MQClientException, MQBrokerException {
        checkParam(topic, tags, keys, contentText);
        Message message = new Message(topic, tags, keys, contentText.getBytes());
        generateTraceId(message);
        //queueIndex 当作args传入MessageQueueSelector中
        return this.defaultMQProducer.send(message, new SelectMessageQueueByHash(), shardingKey);
    }


    private void checkParam(String topic, String tags, String keys, String contentText) {
        if (StringUtils.isBlank(topic)) {
            throw new SystemException("topic is blank");
        }
        if (StringUtils.isBlank(tags)) {
            // throw new RRException("tags is blank");
        }
        if (StringUtils.isBlank(keys)) {
            // throw new RRException("keys is blank");
        }
        if (StringUtils.isBlank(contentText)) {
            throw new SystemException("contentText is blank");
        }
    }
}
