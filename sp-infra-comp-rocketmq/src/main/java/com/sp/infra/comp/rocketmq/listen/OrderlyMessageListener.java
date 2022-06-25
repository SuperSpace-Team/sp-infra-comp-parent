package com.sp.infra.comp.rocketmq.listen;

import com.sp.infra.comp.rocketmq.config.ConsumeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Objects;

@Slf4j
public class OrderlyMessageListener extends AbstractMessageListener implements MessageListenerOrderly {

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext context) {
        ConsumeStatus status = consumeMsgSingle(list.get(0));
        return Objects.equals(status, ConsumeStatus.SUCCESS) ? ConsumeOrderlyStatus.SUCCESS : ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
    }
}
