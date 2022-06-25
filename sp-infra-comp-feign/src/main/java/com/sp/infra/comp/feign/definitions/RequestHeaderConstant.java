package com.sp.infra.comp.feign.definitions;

/**
 * 请求头常量定义
 * @author luchao
 * @create 2020/10/11.
 */
public interface RequestHeaderConstant {

    /**
     * 用户id
     */
    String USER_ID = "X-SP-UserId";

    /**
     * 用户权限字符串，逗号分隔
     */
    String USER_PERMISSIONS = "X-SP-UserPermissions";

    /**
     * Feign接口的调用来源，存放服务名：spring.application.name
     */
    String FEIGN_ORIGIN = "X-SP-FeignOrigin";

    /**
     * 请求特征，
     */
    String REQUEST_FEATURE = "X-SP-RequestFeature";

    /**
     * RPC上下文传递
     */
    String RPC_HOLDER = "X-SP-RpcHolder";
}
