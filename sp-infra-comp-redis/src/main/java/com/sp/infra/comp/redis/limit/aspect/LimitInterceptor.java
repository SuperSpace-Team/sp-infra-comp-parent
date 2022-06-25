package com.sp.infra.comp.redis.limit.aspect;

import com.google.common.collect.ImmutableList;
import com.sp.framework.common.exception.SystemException;
import com.sp.infra.comp.redis.methodcache.annotation.LimitAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "limit.redis", name = "enabled", havingValue = "true")
public class LimitInterceptor {

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    private static final String LIMIT_LUA_SCRIPT = buildLuaScript();


    public LimitInterceptor() {
        log.info("------->启用redis限流切面控制");
    }

    @Around(value = "@annotation(com.sp.infra.comp.redis.methodcache.annotation.LimitAnnotation)")

    public Object interceptor(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        LimitAnnotation limitAnnotation = method.getAnnotation(LimitAnnotation.class);

        String prefix = limitAnnotation.prefix();
        LimitType limitType = limitAnnotation.limitType();
        String name = limitAnnotation.name();
        int limitPeriod = limitAnnotation.period();
        int limitCount = limitAnnotation.count();

        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            if (arg instanceof ILimitSourceSystem) {
                String sourceSystem = ((ILimitSourceSystem) arg).getSourceSystem();
                if (StringUtils.isNotBlank(sourceSystem)) {
                    prefix = prefix + sourceSystem + ":";
                }
                break;
            }
        }

        String key;
        switch (limitType) {
            case CUSTOMER:
                key = limitAnnotation.key();
                break;
            default:
                key = StringUtils.upperCase(method.getName());
        }
        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(prefix, key));
        try {
            RedisScript<Number> redisScript = new DefaultRedisScript<>(LIMIT_LUA_SCRIPT, Number.class);
            // 由于 RedisConfig 中设置的 redisTemplate.setValueSerializer(new StringRedisSerializer()); 所以序列化时需要转换成String
            Number count = redisTemplate.execute(redisScript, keys, String.valueOf(limitCount), String.valueOf(limitPeriod));
            if (count != null && count.intValue() <= limitCount) {
                return pjp.proceed();
            } else {
                throw new RuntimeException(String.format("%s访问超频, %s秒内限流%s次", name, limitPeriod, limitCount));
            }
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new SystemException(500008, e.getLocalizedMessage());
            }
            throw new SystemException("server exception");
        }
    }

    /**
     * 限流 脚本
     *
     * @return lua脚本
     */
    private static String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }
}
