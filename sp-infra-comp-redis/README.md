# sp-infra-comp-redis

    封装了redis的常 用操作，可以方 便地操作各类 redis数据结构
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-redis</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 数据源配置

> 基础配置
```yaml
spring:
  redis:
    password: 123456    # 集群模式无需密码（如果集群设置了密码, 并不能通过lettuceCluster.auth()方式来输入密码）
    database: 0         # 连接工厂使用的数据库索引
    timeout: 10000      # 以毫秒为单位的连接超时
    lettuce:
      pool:
        min-idle: 0       # 目标为保持在池中的最小空闲连接数(这个设置只有在正面的情况下才有效果)
        max-idle: 10      # 池中“空闲”连接的最大数量(使用负值表示无限数量的空闲连接)
        max-active: 50    # 连接池最大连接数（使用负值表示没有限制）
        max-wait: 10000   # 连接池最大阻塞等待时间（使用负值表示没有限制）
```

> 单机模式
```yaml
spring:
  redis:
    host: 10.0.91.249
    port: 7001
    lettuce: ...
```

> 哨兵集群模式
```yaml
spring:
  redis:
    lettuce: ...
    sentinel:
      master: redis1  # Redis服务器的名称
      nodes:
        - 10.0.38.155:26379
        - 10.0.38.156:26380
        - 10.0.38.157:26381
```

> 普通集群模式

    如果同时配置集群模式，那么将覆盖单机模式的配置

```yaml
spring:
  redis:
    lettuce: ...
    cluster:
      nodes:
        - 10.0.71.50:7001
        - 10.0.71.50:7006
        - 10.0.71.51:7004
        - 10.0.71.51:7002
        - 10.0.71.52:7003
        - 10.0.71.52:7005
      max-redirects: 5  # 在群集中执行命令时要遵循的最大重定向数目
```
    
## 禁用命令

> keys

    禁止模糊查询：Keys模糊匹配会引发Redis锁，并且增加Redis的CPU占用。
    替换方式: 将匹配规则的数组序列化后存到一个key中，读取的时候就直接用get

## 操作方法列表

> exists(String key) 

    检查key是否存在
    
> set(String key, Object value, long expire) 

    写入缓存设置失效时间
    
> set(String key, Object value)

    写入缓存,不设置有效时间
    
> expire(String key, long time, TimeUnit timeUnit)

    设置key的生命周期
    
> expireKeyAt(String key, Date date)

    指定key在指定的日期过期
    
> getKeyExpire(String key, TimeUnit timeUnit)

    查询key的生命周期
    
> persistKey(String key)

    将key设置为永久有效
    
> T get(String key, Class<T> clazz, long expire)

    获取缓存，并重新设置有效时长
    
> JSONObject getJsonObject(String key, long expire)

    获取缓存，并重新设置有效时长
    
> JSONArray getJsonArray(String key, long expire)

    获取缓存，并重新设置有效时长
    
> T get(String key, Class<T> clazz)

    获取缓存，转成Object
    
> JSONObject getJsonObject(String key)

    获取缓存 
    
> JSONArray getJsonArray(String key)

    获取缓存 
    
> get(String key, long expire)

    获取缓存，并重新设置有效时长
    
> get(String key)

    获取缓存
    
> delete(String key)

    删除key
    
> delete(String... keys)

    删除多个key

> delete(Collection<String> keys)

    删除Key的集合
    
> renameKey(String oldKey, String newKey)

    重名名key，如果newKey已经存在，则newKey的原值被覆盖
    
> hashKey(String key, String hashKey)

    hash key 是否存在
    
> setOperationsAdd(String key, Object value, long expire) 

    set 写入缓存设置失效时间
    
> setOperationsAdd(String key, Object value)

    set 写入缓存,不设置有效时间
    
> Set setOperationsMembers(String key)

    set 获取缓存
    
> setOperationsIsMembers(String key, Object value)

    set 是否存在
    
> setOperationsRemove(String key, Object value)

    set 删除缓存
    
> hashPut(String key, String hashKey, Object domain)

    hash 设置缓存 (key/value 采用String的序列化方式)
    
> Object hashGet(String key, Object hashKey)

    hash 查询
    
> String hashGetString(String key, String hashKey)

    hash 查询 

> JSONObject hashGetJsonObject(String key, String hashKey)

    hash 查询 
    
> hashDelete(String key, String... hashKey) 

    hash 删除
    

## 代码实例

> 普通操作

```java

import com.yonghui.core.utils.R;
import RedisUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试API
 *
 * @author zhanghai 80730305@yonghui.cn
 */
@RestController
@RequestMapping(value = "/v1")
@Slf4j
public class DemoController {

    @Resource
    private RedisUtils redisUtils;

    @GetMapping("/redis")
    @ApiOperation(value = "Redis测试")
    public R redis(@RequestParam String key, @RequestParam String hashKey, @RequestParam String value) {
        redisUtils.set(hashKey, value, 3000);
        redisUtils.hashPutObject(key, hashKey, value);
        Map m = new HashMap<>();
        m.put("set", redisUtils.get(hashKey));
        m.put("hash", redisUtils.hashGet(key, hashKey));
        return R.success(m);
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ZSetOperations<String, Object> zSetOperations;

    @GetMapping("/redis/zset")
    @ApiOperation(value = "Redis测试")
    public R redisZset(@RequestParam String keya, @RequestParam String keyb) {

        zSetOperations.add(keya, "a", 1);
        zSetOperations.add(keya, "b", 2);
        zSetOperations.add(keya, "c", 3);
        zSetOperations.add(keya, "d", 4);

        redisTemplate.boundZSetOps(keyb).add("aa", 1);
        redisTemplate.boundZSetOps(keyb).add("bb", 2);
        redisTemplate.boundZSetOps(keyb).add("cc", 3);
        redisTemplate.boundZSetOps(keyb).add("dd", 4);

        Map m = new HashMap<>();
        m.put("keya", zSetOperations.rangeByScore(keya, 0, 2));
        m.put("keyb", redisTemplate.boundZSetOps(keyb).rangeByScore(0, 2));

        return R.success(m);
    }

}

```

> zSet 测试

```java

import com.yonghui.core.utils.R;
import RedisUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 测试API
 *
 * @author zhanghai 80730305@yonghui.cn
 */
@RestController
@RequestMapping(value = "/v1")
@Slf4j
public class DemoController {

    @Resource
    private RedisUtils redisUtils;

    @GetMapping("/redis")
    @ApiOperation(value = "Redis测试")
    public R redis(@RequestParam String key, @RequestParam String hashKey, @RequestParam String value) {
        redisUtils.set(hashKey, value, 3000);
        redisUtils.hashPutObject(key, hashKey, value);
        Map m = new HashMap<>();
        m.put("set", redisUtils.get(hashKey));
        m.put("hash", redisUtils.hashGet(key, hashKey));
        return R.success(m);
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ZSetOperations<String, Object> zSetOperations;

    @GetMapping("/redis/zset")
    @ApiOperation(value = "Redis测试")
    public R redisZset(@RequestParam String keya, @RequestParam String keyb) {

        // 构造测试数据
            /*
            for (int i = 1; i < 1000; i++) {
                zSetOperations.add(keya, keya + "_" + i, i);
            }
    
            for (int i = 1; i < 1000; i++) {
                redisTemplate.boundZSetOps(keyb).add(keyb + "_" + i, i);
            }
            */

        // 随机获取数据
        Map m = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 1001; i++) {
            list.add(new Integer(i));
        }
        for (int i = 0; i < 200; i++) {
            int random = new Random().nextInt(list.size());
            Set seta = zSetOperations.rangeByScore(keya, 0, list.get(random));
            Set setb = redisTemplate.boundZSetOps(keyb).rangeByScore(0, list.get(random));
            if (seta.size() != list.get(random)) {
                return R.error("seta.size: " + seta.size() + " random: " + list.get(random));
            }
            log.info("seta.size: " + seta.size() + " random: " + list.get(random));
            if (setb.size() != list.get(random)) {
                return R.error("setb.size: " + setb.size() + " random: " + list.get(random));
            }
            log.info("setb.size: " + setb.size() + " random: " + list.get(random));
            m.put(keya + "_" + i, "seta.size: " + seta.size() + " random: " + list.get(random));
            m.put(keya + "_" + i, "setb.size: " + setb.size() + " random: " + list.get(random));
            list.remove(random);
        }
        return R.success(m);
    }

}

```

