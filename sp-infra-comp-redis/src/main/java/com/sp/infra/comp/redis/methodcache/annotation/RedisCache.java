/**
 *
 */
package com.sp.infra.comp.redis.methodcache.annotation;

import java.lang.annotation.*;

/**
 * @author shuaizhihu
 * <p>
 * $LastChangedDate: 2016-09-05 18:26:32 +0800 (Mon, 05 Sep 2016) $
 * $LastChangedRevision: 896 $
 * $LastChangedBy: shuaizhihu $
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface RedisCache {
    //缓存时间，默认60秒  
    int expire() default 60;
}
