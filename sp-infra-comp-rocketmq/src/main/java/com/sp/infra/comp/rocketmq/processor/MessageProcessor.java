package com.sp.infra.comp.rocketmq.processor;
/*
 * 消息消费需要开发者自己实现
 * Created by chenfei on 2019/4/10 18:11
 */

import com.alibaba.fastjson.JSON;

public interface MessageProcessor<T> {

    boolean handleMessage(T message);

    Class<T> getClazz();

    default T transferMessage(String message) {
        return JSON.parseObject(message, getClazz());
    }
}
