package com.sp.infra.comp.rocketmq.listen;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.rocketmq.config.ClientConstants;
import com.sp.infra.comp.rocketmq.config.ConsumeStatus;
import com.sp.infra.comp.rocketmq.processor.MessageProcessor;
import com.sp.infra.comp.rocketmq.processor.RequestContextHolder;
import com.sp.infra.comp.rocketmq.properties.MqMetaInfo;
import com.sp.infra.comp.rocketmq.utils.LogMdcUtil;
import com.sp.infra.comp.rocketmq.utils.MqMetaInfoConverter;
import com.sp.infra.comp.rocketmq.utils.RocketMQUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class AbstractMessageListener {

    /**
     * MessageProcessor接口的实现类放进map集合 key：tag value：MessageProcessor实现类
     */
    protected Map<String, MessageProcessor> handleMap = new HashMap<>();

    public void registerHandler(String topicAndTag, MessageProcessor messageProcessor) {
        handleMap.put(topicAndTag, messageProcessor);
    }

    protected ConsumeStatus consumeMsgSingle(MessageExt ext) {
        // 消息透传的前台应用标识
        String clientId = ext.getProperty(ClientConstants.HTTP_HEADER_CLIENT_ID);
        RequestContextHolder.setClientId(clientId);

        log.debug("AbstractMessageListener-consumeMessage() msgId:{}, clientId={}, body:{}",
                ext.getMsgId(), clientId, new String(ext.getBody()));

        String message = new String(ext.getBody());
        //获取到key
        String key = RocketMQUtils.concatKey(ext.getTopic(), ext.getTags());
        //根据key从handleMap里获取到我们的处理类
        MessageProcessor messageProcessor = handleMap.get(key);
        if (Objects.isNull(messageProcessor)) {
            messageProcessor = handleMap.get(ext.getTopic());
        }
        Optional.ofNullable(messageProcessor).orElseThrow(() -> new SystemException(String.format("未找到消息处理类, topic:%s, tag:%s", ext.getTopic(), ext.getTags())));
        Object obj = null;
        try {
            //将String类型的message反序列化成对应的对象。
            obj = messageProcessor.transferMessage(message);
            if (obj instanceof MqMetaInfo) {
                MqMetaInfo meta = (MqMetaInfo) obj;
                MqMetaInfoConverter.fromExt(meta, ext);
            }
            generateMDC(ext);
        } catch (Exception e) {
            StringBuilder errMsg = new StringBuilder("对象反序列化失败, ")
                    .append("messageId:     ")
                    .append(ext.getMsgId()).append("\n")
                    .append("msgBody:       ")
                    .append(new String(ext.getBody())).append("\n")
                    .append("messageExt     ")
                    .append(ext).append("\n")
                    .append("stackTrace:    ")
                    .append(JSON.toJSONString(e.getStackTrace()));

            log.error("AbstractMessageListener-consumeMessage() error:{}, msgId:{},  message:{}, errMsg:{}"
                    , e, ext.getMsgId(), new String(ext.getBody()), errMsg.toString());
            throw new SystemException(errMsg.toString());
        }
        //处理消息
        boolean result = messageProcessor.handleMessage(obj);
        if (!result) {
            if (ext.getReconsumeTimes() > Integer.MAX_VALUE) {
                return ConsumeStatus.SUCCESS;
            }
            return ConsumeStatus.FAIL;
        }
        return ConsumeStatus.SUCCESS;
    }

    /**
     * 全局日志追踪
     * @param ext
     */
    private void generateMDC(MessageExt ext) {
        try {
            String traceId = ext.getUserProperty(LogMdcUtil.TRACE_ID);
            if (StringUtils.isNotBlank(traceId)) {
                LogMdcUtil.insertMDC(traceId);
            } else {
                LogMdcUtil.getOrDefaultMdc();
            }
        } catch (Exception e) {
            //ignore
        }
    }
}
