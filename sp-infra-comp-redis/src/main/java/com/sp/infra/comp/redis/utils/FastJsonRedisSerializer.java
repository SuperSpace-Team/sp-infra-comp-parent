package com.sp.infra.comp.redis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

public class FastJsonRedisSerializer implements RedisSerializer<Object> {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(Boolean.TRUE);//解决fastJson autoType is not support错误
    }


    public FastJsonRedisSerializer() {
        super();
    }

    @Override
    public byte[] serialize(Object t) throws SerializationException {
        if (t == null || t instanceof NullValue) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        // return JSON.parse(str, Feature.SupportAutoType);
        return JSON.parse(str);
    }

}