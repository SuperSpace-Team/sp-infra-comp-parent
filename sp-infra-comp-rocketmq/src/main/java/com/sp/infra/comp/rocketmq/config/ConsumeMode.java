package com.sp.infra.comp.rocketmq.config;

import com.sp.framework.common.base.BaseBizEnum;

/**
 * @author  Wang Chong
 */
public enum ConsumeMode implements BaseBizEnum {
    /**
     * 并发消费
     */
    CONCURRENTLY(1,"CONCURRENTLY"),

    /**
     * 顺序消费，一个队列，一个线程
     */
    ORDERLY(2,"ORDERLY"),

    /**
     * 广播消费
     */
    BROADCASTING(3,"BROADCASTING"),
    ;

    ConsumeMode(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
