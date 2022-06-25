package com.sp.infra.comp.rocketmq.config;
/*
 * Created by lys on 2019/4/10 18:07
 */

import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.rocketmq.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@SuppressWarnings("all")
public class RocketMQProducerConfig {

    /**
     * 只有设置了生产者组, 才认为创建生产者
     */
    @Value("${rocketmq.producer.groupName:#{null}}")
    private String producerGroupName;

    /**
     * RocketMq 集群地址
     */
    @Value("${rocketmq.namesrvAddr:#{null}}")
    private String nameserAddr;

    /**
     * 最大消息长度
     */
    @Value("${rocketmq.producer.maxMessageSize:131072}")
    private int maxMessageSize;

    /**
     * 发送消息超时时间
     */
    @Value("${rocketmq.producer.sendMsgTimeout:10000}")
    private int sendMsgTimeout;

    @Autowired
    private SpringContextUtil springContextUtil;

    /**
     * 如果没有设置回调接口, 则创建非事务生产者
     */
    @Value("${rocketmq.transaction.listener:#{null}}")
    private String transactionListenerStr;

    @Value("${rocketmq.transaction.producer.corePoolSize:4}")
    private int corePoolSize;
    @Value("${rocketmq.transaction.producer.maximumPoolSize:8}")
    private int maximumPoolSize;
    @Value("${rocketmq.transaction.producer.keepAliveSecond:120}")
    private int keepAliveTime;
    @Value("${rocketmq.transaction.producer.queueCapacity:2000}")
    private int queueCapacity;
    @Value("${rocketmq.transaction.producer.threadName:#{null}}")
    private String threadName;


    private DefaultMQProducer producer;

    @Bean(name = "defaultMQProducer")
    @Primary
    public DefaultMQProducer getRocketMQProducer() {
        boolean transactionFlag = false;

        if (Objects.isNull(producerGroupName) || producerGroupName.trim().length() == 0) {
            log.info("RocketMQProducerConfig-getRocketMQProducer():{}", "can not find producer group name [" + "rocketmq.producer.groupName" + "], will not create RocketMq producer");
            return null;
        }

        if (Objects.isNull(transactionListenerStr) || transactionListenerStr.trim().length() == 0) {
            log.info("RocketMQProducerConfig-getRocketMQProducer():{}", "create none transaction producer...");
        } else {
            log.info("RocketMQProducerConfig-getRocketMQProducer():{}", "create transaction producer...");
            transactionFlag = true;
        }

        if (Objects.isNull(nameserAddr)) {
            throw new SystemException(5000001, "rocketmq.namesrvAddr is not setted");
        }

        if (transactionFlag) {
            producer = new TransactionMQProducer(producerGroupName);
            TransactionMQProducer transactionMQProducer = (TransactionMQProducer) this.producer;

            ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity), r -> {
                Thread thread = new Thread(r);
                if (null == threadName || threadName.trim().length() == 0) {
                    threadName = nameserAddr + "-thread";
                }
                thread.setName(threadName);
                return thread;
            });

            transactionMQProducer.setExecutorService(executorService);
            TransactionListener transactionListener = getTransactionListener();
            if (Objects.isNull(transactionListener)) {
                log.error("RocketMQProducerConfig-getRocketMQProducer {}", "can not find callabck class, please implement org.apache.rocketmq.client.producer.TransactionListener Interface, and delegate to spring context");
                throw new SystemException(5000005, "can not find callabck class, please make sure rocketmq.transaction.listener is config");
            }
            transactionMQProducer.setTransactionListener(transactionListener);
        } else {
            producer = new DefaultMQProducer(producerGroupName);
        }

        producer.setNamesrvAddr(nameserAddr);
        producer.setMaxMessageSize(maxMessageSize);
        producer.setSendMsgTimeout(sendMsgTimeout);
        producer.setVipChannelEnabled(false);
        try {
            producer.start();
            log.info("rocketMQ is start !!groupName : {},nameserAddr:{}", producerGroupName, nameserAddr);
        } catch (MQClientException e) {
            log.error(String.format("rocketMQ start error,{}", e.getMessage()));
            e.printStackTrace();
        }
        return producer;
    }

    @Bean(name = "transactionListener")
    public TransactionListener getTransactionListener() {
        if (null == transactionListenerStr || transactionListenerStr.trim().length() == 0) {
            return null;
        }

        if (Objects.isNull(producerGroupName)) {
            return null;
        }

        try {
            return ((TransactionListener) springContextUtil.getBean(transactionListenerStr));
        } catch (BeansException e) {
            e.printStackTrace();
            throw new SystemException(5000005, "事务回调类设置错误, 请设置 rocketmq.transaction.listener");
        }
    }
}
