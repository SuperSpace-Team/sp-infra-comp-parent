package com.sp.infra.comp.rocketmq.properties;

import lombok.Data;

/**
 * @author Wang Chong
 * @Date 2019/10/26
 **/
@Data
public class MultiProducer {
    private String groupName;
    private String nameAddr;
    private int maxMessageSize = 131072;
    private int sendMsgTimeout = 10000;
    private String transactionListenerStr;
    private int corePoolSize = 4;
    private int maximumPoolSize = 8;
    private int keepAliveTime = 120;
    private int queueCapacity = 2000;
    private String threadName;
    private String instanceName;
}
