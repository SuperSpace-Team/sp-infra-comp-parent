package com.sp.infra.comp.feign.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sp.infra.comp.feign.exception.SpErrorDecoder;
import com.sp.infra.comp.feign.definitions.OpenAPI;
import com.sp.infra.comp.feign.definitions.OriginService;
import com.sp.infra.comp.feign.exception.SpErrorAttributes;
import com.sp.infra.comp.feign.exception.SpExceptionHandler;
import com.sp.infra.comp.feign.exception.SpWebResultErrorAttributes;
import com.sp.infra.comp.feign.filter.RequestOriginFilter;
import com.sp.infra.comp.feign.filter.SpFilterConfiguration;
import com.sp.infra.comp.feign.gateway.OpenApiConfiguration;
import com.sp.infra.comp.feign.gateway.OpenApiLoadData;
import com.sp.infra.comp.feign.hystrix.HystrixSupportConfiguration;
import feign.Feign;
import feign.codec.ErrorDecoder;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;
import java.util.TimeZone;

 /**
 * Feign????????????????????????
 *
 * @author luchao
 * @create 2020/12/15.
 */
@Configuration
@Import({
        SpFilterConfiguration.class,
        OpenApiConfiguration.class,
        HystrixSupportConfiguration.class,
})
@ConditionalOnClass({Feign.class})
public class FeignConfiguration implements ServletContextListener {
    private static ServletContextEvent sce;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.sce = sce;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    @Bean
    public WebMvcRegistrations feignWebRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new FeignRequestMappingHandlerMapping();
            }
        };
    }

    private static class FeignRequestMappingHandlerMapping extends RequestMappingHandlerMapping  {
        @Override
        protected boolean isHandler(Class<?> beanType) {
            return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class);
        }

        @Override
        protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
            RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            OpenAPI openAPI = AnnotatedElementUtils.findMergedAnnotation(method, OpenAPI.class);
            if (info != null && openAPI != null) {
                // ?????????????????????api??????
                for (String path : info.getPatternsCondition().getPatterns()) {
                    path = resolveUrlPath(path);

                    if (info.getMethodsCondition().getMethods().size() == 0) {
                        // @RequestMap??????????????????????????????????????????8????????????????????????GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
                        OpenApiLoadData.getInstance().add(path, RequestMethod.GET, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.POST, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.PUT, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.DELETE, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.HEAD, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.OPTIONS, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.PATCH, openAPI.products(), openAPI.authorities());
                        OpenApiLoadData.getInstance().add(path, RequestMethod.TRACE, openAPI.products(), openAPI.authorities());
                    } else {
                        for (RequestMethod requestMethod : info.getMethodsCondition().getMethods()) {
                            OpenApiLoadData.getInstance().add(path, requestMethod, openAPI.products(), openAPI.authorities());
                        }
                    }
                }
            }

            // ????????????????????????????????????????????????????????????????????????????????????????????????
            OriginService originService = AnnotatedElementUtils.findMergedAnnotation(method, OriginService.class);
            if (info != null && originService != null) {
                // ?????????????????????api??????
                for (String path : info.getPatternsCondition().getPatterns()) {
                    RequestOriginFilter.REQUEST_INFO_MAP.put(resolveUrlPath(path), originService.name());
                }
            }

            return info;
        }

    }

    /**
     * ????????????Context-Path???URL
     * @param path
     * @return
     */
    private static String resolveUrlPath(String path) {
        if(StringUtils.isNotBlank(sce.getServletContext().getContextPath())) {
            String contextPath = sce.getServletContext().getContextPath();
            if(contextPath.endsWith("/")){
                contextPath = contextPath.substring(0, contextPath.lastIndexOf("/"));
            }

            path = contextPath + path;
        }
        return path;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new SpErrorDecoder();
    }

    /**
     * feign?????????????????????
     *
     * @return
     */
    @Bean("errorAttributes")
    @ConditionalOnProperty(name = "http.sp-error-attributes", matchIfMissing = true)
    public ErrorAttributes spErrorAttributes() {
        return new SpErrorAttributes();
    }

    /**
     * ??????api-rest???????????????WebResult???????????????
     *
     * @return
     */
    @Bean("errorAttributes")
    @ConditionalOnProperty(name = "http.web-result-error-attributes", matchIfMissing = false)
    public ErrorAttributes spWebResultErrorAttributes() {
        return new SpWebResultErrorAttributes();
    }

    @Bean
    public HandlerExceptionResolver exceptionResolver() {
        return new SpExceptionHandler();
    }

    /**
     * json???key????????????????????????
     **/
    @Bean
    @ConditionalOnProperty(name = "http.key-lower-case.enabled", matchIfMissing = true)
    public HttpMessageConverters customConverters() {
        //1??????????????????convert?????????????????????
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setPropertyNamingStrategy(new LowerCasePropertyNamingStrategy());
        objectMapper.registerModule(new JavaTimeModule());
        jacksonConverter.setObjectMapper(objectMapper);

        HttpMessageConverter<?> converter = jacksonConverter;
        return new HttpMessageConverters(converter);
    }

    public static class LowerCasePropertyNamingStrategy extends PropertyNamingStrategy {
        private static final long serialVersionUID = -8071273694301065421L;

        @Override
        public String nameForGetterMethod(MapperConfig<?> arg0, AnnotatedMethod arg1, String arg2) {
            String v = super.nameForGetterMethod(arg0, arg1, arg2);
            return v.toLowerCase();
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> arg0, AnnotatedMethod arg1, String arg2) {
            String v = super.nameForSetterMethod(arg0, arg1, arg2);
            return v.toLowerCase();
        }
    }
}