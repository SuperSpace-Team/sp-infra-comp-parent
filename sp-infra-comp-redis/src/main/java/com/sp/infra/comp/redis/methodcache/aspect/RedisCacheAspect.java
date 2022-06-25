/**
 *
 */
package com.sp.infra.comp.redis.methodcache.aspect;

import com.sp.infra.comp.redis.methodcache.annotation.RedisCache;
import com.sp.infra.comp.redis.utils.FastJsonRedisSerializer;
import com.sp.infra.comp.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * @author shuaizhihu
 * <p>
 * $LastChangedDate: 2016-09-05 18:26:32
 * $LastChangedRevision: 896 $
 * $LastChangedBy: shuaizhihu $
 */
@Component
@Aspect
@Slf4j
public class RedisCacheAspect {

    @Autowired
    private RedisUtils redisUtils;

    private static Logger logger = LoggerFactory.getLogger(RedisCacheAspect.class);
    private static final FastJsonRedisSerializer FAST_JSON_REDIS_SERIALIZER = new FastJsonRedisSerializer();


    @Pointcut(value = "@annotation(com.sp.infra.comp.redis.methodcache.annotation.RedisCache)")
    private void cachePointCut() {

    }

    @Around(value = "cachePointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Class<?> classTarget = pjp.getTarget().getClass();

        //获取类名、方法名、参数名
        String className = classTarget.getName();
        String methodName = ms.getName();
        Object[] args = pjp.getArgs();

        Class<?>[] par = ms.getParameterTypes();
        Method method = classTarget.getMethod(methodName, par);
        RedisCache annotation = method.getAnnotation(RedisCache.class);

        //获取注解信息
        int expire = annotation.expire();

        StringBuffer sb = new StringBuffer();
        for (Object arg : args) {
            if (arg != null) {
                sb.append("_").append(arg.toString());
            }
        }
        //用类名、方法名、参数名作为缓存的key
        String cacheKey = className.concat("_").concat(methodName).concat(sb.toString());
        Object obj = this.getCache(cacheKey);
        //命中缓存，直接返回信息
        if (obj != null) {
            return obj;
        } else {
            //未命中缓存，查询结果，并放到缓存中
            Object result = pjp.proceed();
            try {
                save(cacheKey, result, expire);
            } catch (Exception e) {
                logger.error("put cache exception:" + ExceptionUtils.getFullStackTrace(e));

            }
            return result;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object getCache(String key) {
        try {
            return get(key);
        } catch (Exception e) {
            logger.error("getFromCache exception" + ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }


    private void save(final String key, Object value, Integer expire) {
        try {
            this.redisUtils.getRedisTemplate().execute((RedisCallback<Object>) connection -> {
                connection.set(
                        redisUtils.getRedisTemplate().getStringSerializer().serialize(key),
                        FAST_JSON_REDIS_SERIALIZER.serialize(value),
                        Expiration.seconds(expire),
                        RedisStringCommands.SetOption.UPSERT);
                return null;
            });
        } catch (Exception e) {
            log.error("RedisCacheAspect-save() error:{}", e);
        }
    }

    private Object get(final String key) {
        try {
            return this.redisUtils.getRedisTemplate().execute((RedisCallback<Object>) connection -> {
                byte[] keybytes = redisUtils.getRedisTemplate().getStringSerializer().serialize(key);
                if (connection.exists(keybytes)) {
                    byte[] valuebytes = connection.get(keybytes);
                    return FAST_JSON_REDIS_SERIALIZER.deserialize(valuebytes);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("RedisCacheAspect-get() error:{}", e);
        }
        return null;
    }
}
