package com.sp.infra.comp.rest.registry.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * WebMvc条件判断器
 */
public class SpringWebMvcCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            //也可以根据容器是否存在该类型的bean返回结果
//           Class c1 = Class.forName("org.springframework.web.context.WebApplicationContext");
//           Class c2 = Class.forName("org.springframework.web.servlet.mvc.method.RequestMappingInfo");
//           if(c1 != null && c2 != null){
//               return true;
//           }

           return true;
        } catch (Throwable e) {
        }

        return false;
    }
}