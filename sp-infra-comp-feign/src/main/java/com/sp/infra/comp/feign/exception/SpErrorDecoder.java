package com.sp.infra.comp.feign.exception;

import com.alibaba.fastjson.JSONObject;
import com.sp.infra.comp.feign.hystrix.ExceptionWrapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;

/**
 * Feign接口的反序列化异常信息
 *
 * @author luchao
 * @create 2020/6/29.
 */
@Slf4j
public class SpErrorDecoder implements ErrorDecoder {

    @Autowired(required = false)
    private ExceptionWrapper exceptionWrapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 401) {
            UnauthorizedException r = new UnauthorizedException("无访问权限，对方接口配置了@OriginService限定访问来源");
            log.error("无访问权限", r);
            return r;
        }

        if (response.status() >= 400) {
            // 解析json
            JSONObject jsonObject = null;
            String body;
            try {
                body = Util.toString(response.body().asReader());
                log.debug("error body : " + body);
                jsonObject = JSONObject.parseObject(body);
            } catch (IOException e) {
                log.error("解析请求体报文错误", e);
                return new Exception(jsonObject.getString("exception") + " : " + jsonObject.getString("msg"));
            }

            // 创建异常
            try {
                // 反序列化异常信息
                Object expObj = jsonObject.get("exception");
                Exception exception;
                if (expObj == null) {
                    // 不是异常
                    String message = jsonObject.get("status") + " " + jsonObject.get("path")
                            + " : " + jsonObject.get("error");
                    exception = new RuntimeException(message);
                } else {
                    Class exceptionClass = Class.forName(jsonObject.getString("exception"));
                    String exceptionDetailStr = jsonObject.getString("exception_detail");
                    if (exceptionClass == HttpMessageNotReadableException.class) {
                        exception = new HttpMessageNotReadableException(
                                "From Server : " + jsonObject.getString("msg"));
                    } else {
                        exception = (Exception) JSONObject.parseObject(exceptionDetailStr, exceptionClass);
                    }
                }
                if (exceptionWrapper != null) {
                    exception = exceptionWrapper.wrap(exception);
                }
                return exception;
            } catch (Exception e) {
                // 其他异常
                Exception exception = new Exception("服务提供方返回:" + body);
                log.error("异常反序列化失败，服务提供方返回：{}", body, exception);
                return exception;
            }
        }
        return new Exception("methodKey : " + response.reason());
    }
}
