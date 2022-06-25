package com.sp.infra.comp.rocketmq.config;

import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.rocketmq.properties.MultiProducer;
import com.sp.infra.comp.rocketmq.properties.MultiProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Wang Chong
 * @Date 2019/10/26
 **/
@Configuration
@EnableConfigurationProperties(MultiProducerProperties.class)
@Slf4j
public class RocketMultiProducerConfig implements ApplicationContextAware, SmartInitializingSingleton {
    public static ConcurrentMap<String,DefaultMQProducer> producerMaps = new ConcurrentHashMap();

    private final MultiProducerProperties multiProducerProperties;
    private ApplicationContext applicationContext;
    private final Environment environment;

    public RocketMultiProducerConfig(MultiProducerProperties multiProducerProperties, Environment environment) {
        this.multiProducerProperties = multiProducerProperties;
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private DefaultMQProducer createProducer(MultiProducer multiProducer) {
        boolean transactionFlag = false;
        DefaultMQProducer producer;
        String groupName = multiProducer.getGroupName();
        String transactionListenerStr = multiProducer.getTransactionListenerStr();
        String nameAddr = multiProducer.getNameAddr();
        int corePoolSize = multiProducer.getCorePoolSize();
        int maximumPoolSize = multiProducer.getMaximumPoolSize();
        int keepAliveTime = multiProducer.getKeepAliveTime();
        int maxMessageSize = multiProducer.getMaxMessageSize();
        int sendMsgTimeout = multiProducer.getSendMsgTimeout();
        String instanceName = multiProducer.getInstanceName();
        final String[] threadName = {multiProducer.getThreadName()};
        int queueCapacity = multiProducer.getQueueCapacity();

        if (Objects.isNull(groupName) || groupName.trim().length() == 0) {
            log.info("RocketMQMultiProducerConfig-getRocketMQProducer():{}", "can not find producer group name [" + "rocketmq.producer.groupName" + "], will not create RocketMq producer");
            return null;
        }
        if (Objects.isNull(transactionListenerStr) || transactionListenerStr.trim().length() == 0) {
            log.info("RocketMQMultiProducerConfig-getRocketMQProducer():{}", "create none transaction producer...");
        } else {
            log.info("RocketMQMultiProducerConfig-getRocketMQProducer():{}", "create transaction producer...");
            transactionFlag = true;
        }


        if (Objects.isNull(nameAddr)) {
            throw new SystemException(5000001, "rocketmq.namesrvAddr is not setted");
        }

        if (transactionFlag) {
            producer = new TransactionMQProducer(groupName);
            TransactionMQProducer transactionMQProducer = (TransactionMQProducer) producer;

            ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity), r -> {
                Thread thread = new Thread(r);
                if (null == threadName[0] || threadName[0].trim().length() == 0) {
                    threadName[0] = nameAddr + "-thread";
                }
                thread.setName(threadName[0]);
                return thread;
            });

            transactionMQProducer.setExecutorService(executorService);
            TransactionListener transactionListener = (TransactionListener) this.applicationContext.getBean(transactionListenerStr);
            if (Objects.isNull(transactionListener)) {
                log.error("RocketMQMultiProducerConfig-getRocketMQProducer {}", "can not find callabck class, please implement org.apache.rocketmq.client.producer.TransactionListener Interface, and delegate to spring context");
                throw new SystemException(5000005, "can not find callabck class, please make sure rocketmq.transaction.listener is config");
            }
            transactionMQProducer.setTransactionListener(transactionListener);
        } else {
            producer = new DefaultMQProducer(groupName);
        }

        producer.setNamesrvAddr(nameAddr);
        producer.setMaxMessageSize(maxMessageSize);
        producer.setSendMsgTimeout(sendMsgTimeout);
        producer.setVipChannelEnabled(false);
        producer.setInstanceName(instanceName);
        producerMaps.put(producer.getInstanceName(), producer);
        try {
            producer.start();
            log.info("rocketMQ is start !!groupName : {},nameserAddr:{}", groupName, nameAddr);
        } catch (MQClientException e) {
            log.error(String.format("rocketMQ start error,{}", e.getMessage()));
            e.printStackTrace();
        }
        return producer;
    }


    @Override
    public void afterSingletonsInstantiated() {
        List<MultiProducer> producers = multiProducerProperties.getProducers();
        if(!CollectionUtils.isEmpty(producers)){
            producers.forEach(this::createProducer);
        }
    }
}
