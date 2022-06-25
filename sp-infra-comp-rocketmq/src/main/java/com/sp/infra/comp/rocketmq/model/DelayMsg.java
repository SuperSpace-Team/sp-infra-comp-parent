package com.sp.infra.comp.rocketmq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Wang Chong
 * @Date 2019/11/15
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayMsg implements Serializable {

    private static final long serialVersionUID = 3384847636726834918L;
    /**
     * 事件最终触发时间
     */
    Date eventDelayAt;

    /**
     * 事件发送开始时间
     */
    Date eventSendTime;

    String topic;

    String tags;

    String keys;

    String contentText;

    /**
     * 当前topic index
     */
    Integer index = 0;

}
