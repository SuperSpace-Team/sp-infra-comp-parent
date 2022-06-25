package com.sp.infra.comp.rocketmq.config;

/**
 * Created by 程序猿DD/翟永超 on 2019/11/14.
 * <p>
 */
public class ClientConstants {

    /**
     * 前台应用进入中台时候的唯一标识：X-BEANSTALK-CLIENTID
     */
    public final static String HTTP_HEADER_CLIENT_ID = "X-BEANSTALK-CLIENTID";

    /**
     * 通过Kong网关透传下来的 client_id
     */
    public final static String HTTP_HEADER_OAUTH2_CLIENT_ID = "X-Consumer-Username";

}
