package com.sp.infra.comp.rocketmq.utils;

import com.sp.infra.comp.rocketmq.properties.MqMetaInfo;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Optional;

public class MqMetaInfoConverter {

    public static MqMetaInfo fromExt(MqMetaInfo mqMetaInfo, MessageExt ext) {
        mqMetaInfo = Optional.ofNullable(mqMetaInfo).orElse(new MqMetaInfo());
        mqMetaInfo.setQueueId(ext.getQueueId());
        mqMetaInfo.setStoreSize(ext.getStoreSize());
        mqMetaInfo.setQueueOffset(ext.getQueueOffset());
        mqMetaInfo.setSysFlag(ext.getSysFlag());
        mqMetaInfo.setBornTimestamp(ext.getBornTimestamp());
        mqMetaInfo.setBornHost(ext.getBornHost());
        mqMetaInfo.setStoreTimestamp(ext.getStoreTimestamp());
        mqMetaInfo.setStoreHost(ext.getStoreHost());
        mqMetaInfo.setMsgId(ext.getMsgId());
        mqMetaInfo.setCommitLogOffset(ext.getCommitLogOffset());
        mqMetaInfo.setBodyCRC(ext.getBodyCRC());
        mqMetaInfo.setReconsumeTimes(ext.getReconsumeTimes());
        mqMetaInfo.setPreparedTransactionOffset(ext.getPreparedTransactionOffset());

        mqMetaInfo.setTopic(ext.getTopic());
        mqMetaInfo.setFlag(ext.getFlag());
        mqMetaInfo.setProperties(ext.getProperties());
        mqMetaInfo.setBody(ext.getBody());
        mqMetaInfo.setTransactionId(ext.getTransactionId());

        return mqMetaInfo;
    }
}
