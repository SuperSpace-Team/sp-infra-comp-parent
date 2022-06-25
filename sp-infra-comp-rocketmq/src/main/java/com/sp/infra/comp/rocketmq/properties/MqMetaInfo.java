package com.sp.infra.comp.rocketmq.properties;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

@Data
public class MqMetaInfo implements Serializable {
    public static final String PROPERTY_KEYS = "KEYS";
    public static final String PROPERTY_TAGS = "TAGS";
    public static final String PROPERTY_WAIT_STORE_MSG_OK = "WAIT";
    public static final String PROPERTY_DELAY_TIME_LEVEL = "DELAY";
    public static final String PROPERTY_RETRY_TOPIC = "RETRY_TOPIC";
    public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    public static final String PROPERTY_TRANSACTION_PREPARED = "TRAN_MSG";
    public static final String PROPERTY_PRODUCER_GROUP = "PGROUP";
    public static final String PROPERTY_MIN_OFFSET = "MIN_OFFSET";
    public static final String PROPERTY_MAX_OFFSET = "MAX_OFFSET";
    public static final String PROPERTY_BUYER_ID = "BUYER_ID";
    public static final String PROPERTY_ORIGIN_MESSAGE_ID = "ORIGIN_MESSAGE_ID";
    public static final String PROPERTY_TRANSFER_FLAG = "TRANSFER_FLAG";
    public static final String PROPERTY_CORRECTION_FLAG = "CORRECTION_FLAG";
    public static final String PROPERTY_MQ2_FLAG = "MQ2_FLAG";
    public static final String PROPERTY_RECONSUME_TIME = "RECONSUME_TIME";
    public static final String PROPERTY_MSG_REGION = "MSG_REGION";
    public static final String PROPERTY_TRACE_SWITCH = "TRACE_ON";
    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = "UNIQ_KEY";
    public static final String PROPERTY_MAX_RECONSUME_TIMES = "MAX_RECONSUME_TIMES";
    public static final String PROPERTY_CONSUME_START_TIMESTAMP = "CONSUME_START_TIME";
    public static final String PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET = "TRAN_PREPARED_QUEUE_OFFSET";
    public static final String PROPERTY_TRANSACTION_CHECK_TIMES = "TRANSACTION_CHECK_TIMES";
    public static final String PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS = "CHECK_IMMUNITY_TIME_IN_SECONDS";

    private static final long serialVersionUID = 5720810328625748049L;
    @JSONField(serialize = false)
    private int queueId;
    @JSONField(serialize = false)
    private int storeSize;
    @JSONField(serialize = false)
    private long queueOffset;
    @JSONField(serialize = false)
    private int sysFlag;
    @JSONField(serialize = false)
    private long bornTimestamp;
    @JSONField(serialize = false)
    private SocketAddress bornHost;
    @JSONField(serialize = false)
    private long storeTimestamp;
    @JSONField(serialize = false)
    private SocketAddress storeHost;
    @JSONField(serialize = false)
    private String msgId;
    @JSONField(serialize = false)
    private long commitLogOffset;
    @JSONField(serialize = false)
    private int bodyCRC;
    @JSONField(serialize = false)
    private int reconsumeTimes;
    @JSONField(serialize = false)
    private long preparedTransactionOffset;


    @JSONField(serialize = false)
    private String topic;
    @JSONField(serialize = false)
    private int flag;
    @JSONField(serialize = false)
    private Map<String, String> properties;
    @JSONField(serialize = false)
    private byte[] body;
    @JSONField(serialize = false)
    private String transactionId;



    @JSONField(serialize = false)
    public String getTags() {
        return this.getProperty(PROPERTY_TAGS);
    }

    @JSONField(serialize = false)
    public String getKeys() {
        return this.getProperty(PROPERTY_KEYS);
    }

    @JSONField(serialize = false)
    public int getDelayTimeLevel() {
        String t = this.getProperty(PROPERTY_DELAY_TIME_LEVEL);
        if (t != null) {
            return Integer.parseInt(t);
        }

        return 0;
    }

    @JSONField(serialize = false)
    public boolean isWaitStoreMsgOK() {
        String result = this.getProperty(PROPERTY_WAIT_STORE_MSG_OK);
        if (null == result) {
            return true;
        }

        return Boolean.parseBoolean(result);
    }

    @JSONField(serialize = false)
    public String getBuyerId() {
        return getProperty(PROPERTY_BUYER_ID);
    }

    @JSONField(serialize = false)
    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<String, String>();
        }

        return this.properties.get(name);
    }
}
