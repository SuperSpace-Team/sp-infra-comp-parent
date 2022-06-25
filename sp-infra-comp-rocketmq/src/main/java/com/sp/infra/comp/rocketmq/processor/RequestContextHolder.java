package com.sp.infra.comp.rocketmq.processor;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * Created by 程序猿DD/翟永超 on 2019/11/14.
 * <p>
 */
public class RequestContextHolder {

    /**
     * 前台应用进入中台时候的唯一标识：X-BEANSTALK-CLIENTID
     */
    private static final TransmittableThreadLocal<String> clientId = new TransmittableThreadLocal<>();


    public static void setClientId(String cId) {
        clientId.set(cId);
    }

    public static String getClientId() {
        return clientId.get();
    }

    public static void removeClientId() {
        clientId.remove();
    }

    public static void removeAll() {
        removeClientId();
    }

}
