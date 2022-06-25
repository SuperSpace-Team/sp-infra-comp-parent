package com.sp.infra.comp.feign.exception;

import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 异常信息处理器
 * 增加异常详情,用来在Feign接口调用的时候模拟异常捕获
 *
 * @author luchao
 * @create 2020/12/15.
 */
public class SpErrorAttributes extends DefaultErrorAttributes {
    //传递的异常栈长度，可以再参数化
    private final int MAX_STACK_TRACE_ELEMENT = 5;

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest,
                                                  boolean includeStackTrace) {
        Map<String, Object> result = super.getErrorAttributes(webRequest, includeStackTrace);
        Throwable throwable = getError(webRequest);
        try {
            // 增加exception相关的信息，用来在通过feign调用的客户端进行反序列化，实现feign rpc的异常处理
            List<StackTraceElement> steList = new LinkedList<>();
            for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                steList.add(stackTraceElement);
                if(steList.size() >= MAX_STACK_TRACE_ELEMENT) {
                    break;
                }
            }
            throwable.setStackTrace(steList.toArray(new StackTraceElement[steList.size()]));
            result.put("exception_detail", JSONObject.toJSONString(throwable));
        } catch (Exception e) {
            e.setStackTrace(new StackTraceElement[0]);
            result.put("exception_detail", e);
        }

        return result;
    }
}