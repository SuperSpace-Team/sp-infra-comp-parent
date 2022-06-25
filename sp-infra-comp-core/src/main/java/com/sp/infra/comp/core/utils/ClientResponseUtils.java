package com.sp.infra.comp.core.utils;

import com.sp.framework.common.enums.SystemErrorCodeEnum;
import com.sp.framework.common.exception.SystemException;
import com.sp.framework.common.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Client调用统一返回码解析器
 *
 * @author alexlu
 * @date 2019-07-05 17:30:43
 */
@Slf4j
public class ClientResponseUtils {
    /**
     * 根据响应失败时并抛出系统异常
     * @param response
     * @param params
     * @param <T>
     */
    public static <T> void resolveFailure(ResponseVO<T> response, Object... params) {
        if (response.getCode() != SystemErrorCodeEnum.SUCCESS.getCode()) {
            log.error("Client调用失败,Response Code:{}， Message:{}, Params:{}",
                    response.getCode(), response.getMsg(), params);
            throw new SystemException(response.getCode(), response.getMsg());
        }
    }
}
