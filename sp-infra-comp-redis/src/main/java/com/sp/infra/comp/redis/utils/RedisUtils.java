package com.sp.infra.comp.redis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.framework.common.utils.JsonUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sp.infra.comp.redis.utils.ConstantsUtils.RedisSet.NOT_EXPIRE;

/**
 * Redis工具类
 *
 * @author zhanghai
 * @date 2018-09-05 17:30:43
 */
@Component
public class RedisUtils {

	@Autowired
	@Getter
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * 操作字符串
	 */
	@Autowired
	private ValueOperations<String, String> valueOperations;

	/**
	 * 操作hash
	 */
	@Autowired
	private HashOperations<String, String, Object> hashOperations;

	/**
	 * 操作list
	 */
	@Autowired
	private ListOperations<String, Object> listOperations;

	/**
	 * 操作set
	 */
	@Autowired
	private SetOperations<String, Object> setOperations;

	/**
	 * 操作有序set
	 */
	@Autowired
	private ZSetOperations<String, Object> zSetOperations;

	/**
	 * 检查key是否存在
	 *
	 * @param key
	 * @return
	 */
	public boolean exists(String key) {
		return redisTemplate.hasKey(key);
	}




	/**
	 * 写入缓存设置失效时间
	 *
	 * @param key
	 * @param value
	 * @param expire 过期时长
	 */
	public void set(String key, Object value, long expire) {
		valueOperations.set(key, JSON.toJSONString(value));
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
	}

	/**
	 * 写入缓存,不设置有效时间
	 *
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		set(key, value, NOT_EXPIRE);
	}

	/**
	 * 设置key的生命周期
	 *
	 * @param key
	 * @param time     有效时长
	 * @param timeUnit 单位
	 */
	public void expire(String key, long time, TimeUnit timeUnit) {
		redisTemplate.expire(key, time, timeUnit);
	}

	/**
	 * 指定key在指定的日期过期
	 *
	 * @param key
	 * @param date
	 */
	public void expireKeyAt(String key, Date date) {
		redisTemplate.expireAt(key, date);
	}

	/**
	 * 查询key的生命周期
	 *
	 * @param key
	 * @param timeUnit
	 * @return
	 */
	public long getKeyExpire(String key, TimeUnit timeUnit) {
		return redisTemplate.getExpire(key, timeUnit);
	}

	/**
	 * 将key设置为永久有效
	 *
	 * @param key
	 */
	public void persistKey(String key) {
		redisTemplate.persist(key);
	}

	/**
	 * 获取缓存，转成Object，并重新设置有效时长
	 *
	 * @param key
	 * @param clazz
	 * @param expire 过期时长
	 * @param <T>
	 * @return
	 */
	public <T> T get(String key, Class<T> clazz, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
		return value == null ? null : JSON.parseObject(value, clazz);
	}

	/**
	 * 获取缓存，转成JSONObject，并重新设置有效时长
	 *
	 * @param key
	 * @param expire 过期时长
	 * @return
	 */
	public JSONObject getJsonObject(String key, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
		return value == null ? null : JSON.parseObject(value);
	}

	/**
	 * 获取缓存，转成JSONArray，并重新设置有效时长
	 *
	 * @param key
	 * @param expire 过期时长
	 * @return
	 */
	public JSONArray getJsonArray(String key, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
		return value == null ? null : JSON.parseArray(value);
	}

	/**
	 * 获取缓存，转成Object
	 *
	 * @param key
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> T get(String key, Class<T> clazz) {
		return get(key, clazz, NOT_EXPIRE);
	}

	/**
	 * 获取缓存
	 *
	 * @param key
	 * @return
	 */
	public JSONObject getJsonObject(String key) {
		return getJsonObject(key, NOT_EXPIRE);
	}

	/**
	 * 获取缓存
	 *
	 * @param key
	 * @return
	 */
	public JSONArray getJsonArray(String key) {
		return getJsonArray(key, NOT_EXPIRE);
	}

	/**
	 * 获取缓存，并重新设置有效时长
	 *
	 * @param key
	 * @param expire
	 * @return
	 */
	public String get(String key, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
		return value;
	}

	/**
	 * 在原有的值基础上新增字符串到末尾
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Integer valueAppend(String key, String value) {
		return valueOperations.append(key, value);
	}

	/**
	 * 以增量的方式将double值存储在变量中
	 *
	 * @param key
	 * @param increment
	 * @return
	 */
	public Double incrByDouble(String key, double increment) {
		return valueOperations.increment(key, increment);
	}

	/**
	 * 通过increment(K key, long delta)方法以增量方式存储long值（正值则自增，负值则自减）
	 *
	 * @param key
	 * @param increment
	 * @return
	 */
	public Long incrBy(String key, long increment) {
		return valueOperations.increment(key, increment);
	}

	/**
	 * 如果对应的map集合名称不存在，则添加否则不做修改
	 *
	 * @param valueMap
	 */
	public void setByMap(Map<String, String> valueMap) {
		valueOperations.multiSetIfAbsent(valueMap);
	}

	/**
	 * 设置map集合到redis
	 *
	 * @param valueMap
	 */
	public void setMap(Map<String, String> valueMap) {
		valueOperations.multiSet(valueMap);
	}

	/**
	 * 获取字符串的长度
	 *
	 * @param key
	 * @return
	 */
	public Long getSize(String key) {
		return valueOperations.size(key);
	}

	/**
	 * 重新设置key对应的值，如果存在返回false，否则返回true
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setIfAbsent(String key, String value) {
		return valueOperations.setIfAbsent(key, value);
	}

	public boolean setIfAbsent(String key, String value, long l, TimeUnit timeUnit) {
		return valueOperations.setIfAbsent(key, value, l, timeUnit);
	}

	/**
	 * 设置key对应的值，如果存在返回true，否则返回false
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setIfPresent(String key, String value) {
		return valueOperations.setIfPresent(key, value);
	}

	public boolean setIfPresent(String key, String value, long l, TimeUnit timeUnit) {
		return valueOperations.setIfPresent(key, value, l, timeUnit);
	}

	/**
	 * 获取缓存
	 *
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return get(key, NOT_EXPIRE);
	}

	/**
	 * 删除key
	 *
	 * @param key
	 */
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 删除多个key
	 *
	 * @param keys
	 */
	public void delete(String... keys) {
		Set<String> kSet = Stream.of(keys).map(k -> k).collect(Collectors.toSet());
		redisTemplate.delete(kSet);
	}

	/**
	 * 删除Key的集合
	 *
	 * @param keys
	 */
	public void delete(Collection<String> keys) {
		Set<String> kSet = keys.stream().map(k -> k).collect(Collectors.toSet());
		redisTemplate.delete(kSet);
	}

	/**
	 * 重名名key，如果newKey已经存在，则newKey的原值被覆盖
	 *
	 * @param oldKey
	 * @param newKey
	 */
	public void renameKey(String oldKey, String newKey) {
		redisTemplate.rename(oldKey, newKey);
	}

	/**
	 * set 写入缓存设置失效时间
	 *
	 * @param key
	 * @param value
	 * @param expire 过期时长
	 */
	public void setOperationsAdd(String key, Object value, long expire) {
		setOperations.add(key, value);
		if (expire != NOT_EXPIRE) {
			expire(key, expire, TimeUnit.SECONDS);
		}
	}

	/**
	 * set 写入缓存,不设置有效时间
	 *
	 * @param key
	 * @param value
	 */
	public void setOperationsAdd(String key, Object value) {
		setOperationsAdd(key, value, NOT_EXPIRE);
	}

	/**
	 * set 获取缓存
	 *
	 * @param key
	 */
	public Set setOperationsMembers(String key) {
		return setOperations.members(key);
	}

	/**
	 * set 是否存在
	 *
	 * @param key
	 */
	public boolean setOperationsIsMembers(String key, Object value) {
		return setOperations.isMember(key, value);
	}

	/**
	 * set 删除缓存
	 *
	 * @param key
	 */
	public Long setOperationsRemove(String key, Object value) {
		return setOperations.remove(key, value);
	}

	// Hash类型

	/**
	 * 查看hash表中指定字段是否存在
	 *
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public boolean hashKey(String key, String hashKey) {
		return hashOperations.hasKey(key, hashKey);
	}

	/**
	 * hash 设置缓存 (key/value 采用String的序列化方式)
	 *
	 * @param key
	 * @param hashKey
	 * @param domain
	 */
	public void hashPut(String key, String hashKey, Object domain) {
		hashPutObject(key, hashKey, JSON.toJSONString(domain));
	}

	public void hashPutObject(String key, String hashKey, Object domain) {
		hashOperations.put(key, hashKey, domain);
	}

	/**
	 * 仅当hashKey不存在时才设置
	 *
	 * @param key
	 * @param hashKey
	 * @param value
	 * @return
	 */
	public Boolean hashPutIfAbsent(String key, String hashKey, String value) {
		return hashOperations.putIfAbsent(key, hashKey, value);
	}

	/**
	 * 以map集合的形式添加键值对
	 *
	 * @param key
	 * @param maps
	 */
	public void hPutAll(String key, Map<String, String> maps) {
		hashOperations.putAll(key, maps);
	}

	/**
	 * hash 查询key下所有数据
	 *
	 * @param key
	 * @return
	 */
	public Map<String, Object> hashTable(String key) {
		return hashOperations.entries(key);
	}

	/**
	 * hash 查询
	 *
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public Object hashGet(String key, Object hashKey) {
		return hashOperations.get(key, hashKey);
	}

	/**
	 * hash 查询
	 *
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public String hashGetString(String key, String hashKey) {
		Object object = hashGet(key, hashKey);
		return object == null ? null : object.toString();
	}

	/**
	 * hash multi 查询
	 *
	 * @param key
	 * @return
	 */
	public List<String> hmGetString(String key, Collection<String> hashKeys) {
		List<Object> values = hashOperations.multiGet(key, hashKeys);
		if (null != values) {
			return values.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * hash 查询
	 *
	 * @param key
	 * @param hashKey
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> T hashGetClass(String key, String hashKey, Class<T> clazz) {
		String value = hashGetString(key, hashKey);
		return value == null ? null : JSON.parseObject(value, clazz);
	}

	/**
	 * hash 查询
	 *
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public JSONObject hashGetJsonObject(String key, String hashKey) {
		String value = hashGetString(key, hashKey);
		return value == null ? null : JSON.parseObject(value);
	}

	/**
	 * hash 删除(多个key)
	 *
	 * @param key
	 * @param hashKey
	 * @return 删除个数
	 */
	public Long hashDelete(String key, String... hashKey) {
		return hashOperations.delete(key, hashKey);
	}

	/**
	 * 为哈希表 key 中的指定字段的整数值加上增量 increment (如果hashKey不存在时，会设置新的值，如果是原数据是float类型会异常)
	 *
	 * @param key
	 * @param hashKey
	 * @param increment 正负数、0、正整数
	 * @return
	 */
	public long hashIncr(String key, String hashKey, long increment) {
		return hashOperations.increment(key, hashKey, increment);
	}

	/**
	 * 为哈希表 key 中的指定字段的浮点数值加上增量 increment 。(注：如果hashKey不存在时，会设置新的值)
	 *
	 * @param key
	 * @param hashKey
	 * @param delta，可以为负数、正数、0
	 * @return
	 */
	public Double hashIncrByDouble(String key, String hashKey, double delta) {
		return hashOperations.increment(key, hashKey, delta);
	}

	/**
	 * 获取hash表中字段的数量
	 *
	 * @param key
	 * @return
	 */
	public Long hashGetSize(String key) {
		return hashOperations.size(key);
	}

}
