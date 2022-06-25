package com.sp.infra.comp.rocketmq.utils;
/*
 * Created by chenfei on 2019/4/10 18:30
 */

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import com.sp.framework.common.utils.JsonUtils;
import com.sp.infra.comp.rocketmq.config.ClientConstants;
import com.sp.infra.comp.rocketmq.model.DelayMsg;
import com.sp.infra.comp.rocketmq.processor.RequestContextHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class RocketMQProducerUtil {

    @Autowired(required = false)
    @Getter
    private DefaultMQProducer defaultMQProducer;


    /**
     * 发送某天定时消息,只支持json
     *
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

        // 消息透传的前台应用标识
        if (message != null) {
            if (RequestContextHolder.getClientId() != null) {
                message.putUserProperty(ClientConstants.HTTP_HEADER_CLIENT_ID, RequestContextHolder.getClientId());
            }
        }

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

    /**
     * 发送全局事务消息(queue = 1)
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
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

    /**
     * 发送order消息, 根据shardingKey 落在指定队列上
     *
     * @param topic
     * @param tags
     * @param keys
     * @param contentText
     * @param shardingKey
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
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
