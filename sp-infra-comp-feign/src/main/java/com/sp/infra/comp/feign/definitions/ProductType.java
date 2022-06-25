package com.sp.infra.comp.feign.definitions;

/**
 * API所属产品权限类型
 */
public interface ProductType {
    /**
     * 中台聚合服务alliedweb
     **/
    String ALLIEDWEB = "ALLIEDWEB";

    /**
     * WMS_PDA
     **/
    String WMS_PDA = "WMS_PDA";

    /**
     * ALLIED_VENDOR
     **/
    String ALLIED_VENDOR = "ALLIED_VENDOR";

    /**
     * ALLIED_VENDOR
     **/
    String DEALER_API = "DEALER_API";

    /**
     * PARTNER_APP
     **/
    String PARTNER_APP = "PARTNER_APP";


    /**
     * ORDER_FULFILLMENT 履单APP使用
     **/
    String ORDER_FULFILLMENT = "ORDER_FULFILLMENT";

    /**
     * DELIVERY 骑手APP使用
     **/
    String DELIVERY = "DELIVERY";

    /**
     * CSS 清结算系统
     **/
    String CSS = "CSS";

}
