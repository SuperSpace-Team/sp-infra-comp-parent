package com.sp.infra.comp.core.aop;

import com.alibaba.fastjson.JSON;
import com.sp.framework.common.enums.SystemErrorCodeEnum;
import com.sp.framework.common.exception.SystemException;
import com.sp.framework.common.vo.ResponseVO;
import com.sp.infra.comp.core.annotation.ApiResponseConversion;
import com.sp.infra.comp.core.model.Resp;
import com.sp.infra.comp.core.model.WebResult;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * API响应体统一封装转换Handler
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiResponseHandler implements ResponseBodyAdvice<Object> {
//
//    @Override
//    public boolean supportsReturnType(MethodParameter methodParameter){
//        return methodParameter.hasMethodAnnotation(ApiResponseConversion.class);
//    }
//
//    @Override
//    public void handleReturnValue(Object returnValue,MethodParameter returnType,
//                                  ModelAndViewContainer mavContainer,
//                                  NativeWebRequest webRequest) throws Exception{
//        mavContainer.setRequestHandled(true);
//        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//        response.setContentType("application/json;charset=UTF-8");
//
//        ApiResponseConversion annoResp = returnType.getMethodAnnotation(ApiResponseConversion.class);
//        if(annoResp == null || annoResp.adaptType() == null){
//            return;
//        }
//
//        String finalRespStr = null;
//        switch (annoResp.adaptType()){
//            //适配封装为原云创WebResult返回类型
//            case WebResult:
//                WebResult respWebResult = null;
//                if(BeanUtils.isSimpleValueType(returnType.getParameterType())){
//                    respWebResult = new WebResult(returnValue);
//                }else if(returnType.getParameterType().equals(ResponseVO.class)){
//                    ResponseVO respVO = (ResponseVO)returnValue;
//                    respWebResult = new WebResult(respVO.getCode(), respVO.getMsg(), respVO.getData());
//                }
//
//                finalRespStr = JSON.toJSONString(respWebResult);
//                break;
//            case R:
//                Resp respInfoObj = null;
//                if(BeanUtils.isSimpleValueType(returnType.getParameterType())){
//                    respInfoObj = Resp.success(returnValue);
//                }else if(returnType.getParameterType().equals(ResponseVO.class)){
//                    ResponseVO respVO = (ResponseVO)returnValue;
//                    respInfoObj = Resp.newInstance(respVO.getCode(), respVO.getData(), respVO.getMsg());
//                }
//                finalRespStr = JSON.toJSONString(respInfoObj);
//                break;
//        }
//
//        try (PrintWriter out = response.getWriter()){
//            out.write(finalRespStr);
//            out.flush();
//        }catch (Exception e){
//            throw new SystemException(SystemErrorCodeEnum.SYSTEM_ERROR.getCode(),
//                    SystemErrorCodeEnum.SYSTEM_ERROR.getMsg());
//        }
//    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(ApiResponseConversion.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
//        HttpServletResponse servletResponse = (HttpServletResponse) response;
//        servletResponse.setContentType("application/json;charset=UTF-8");

        ApiResponseConversion annoResp = returnType.getMethodAnnotation(ApiResponseConversion.class);
        if(annoResp == null || annoResp.adaptType() == null){
            return body;
        }

        String finalRespStr = null;
        switch (annoResp.adaptType()){
            //适配封装为原云创WebResult返回类型
            case WebResult:
                WebResult respWebResult = null;
                if(BeanUtils.isSimpleValueType(returnType.getParameterType())){
                    respWebResult = new WebResult(body);
                }else if(returnType.getParameterType().equals(ResponseVO.class)){
                    ResponseVO respVO = (ResponseVO)body;
                    respWebResult = new WebResult(respVO.getCode(), respVO.getMsg(), respVO.getData());
                }

//                finalRespStr = JSON.toJSONString(respWebResult);
                return respWebResult;
            case R:
                Resp respInfoObj = null;
                if(BeanUtils.isSimpleValueType(returnType.getParameterType())){
                    respInfoObj = Resp.success(body);
                }else if(returnType.getParameterType().equals(ResponseVO.class)){
                    ResponseVO respVO = (ResponseVO)body;
                    respInfoObj = Resp.newInstance(respVO.getCode(), respVO.getData(), respVO.getMsg());
                }
//                finalRespStr = JSON.toJSONString(respInfoObj);
                return respInfoObj;

        }

        return body;
//        try (PrintWriter out = servletResponse.getWriter()){
//            out.write(finalRespStr);
//            out.flush();
//        }catch (Exception e){
//            throw new SystemException(SystemErrorCodeEnum.SYSTEM_ERROR.getCode(),
//                    SystemErrorCodeEnum.SYSTEM_ERROR.getMsg());
//        }
    }
}
