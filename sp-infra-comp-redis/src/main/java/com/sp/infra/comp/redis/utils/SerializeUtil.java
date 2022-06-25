package com.sp.infra.comp.redis.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Description: 序列化反序列化工具
 */
@Slf4j
public class SerializeUtil {
    /**
     * 序列化
     */
    public static byte[] serialize(Object obj) {

        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;

        try {
            //序列化
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.writeObject(obj);
            byte[] byteArray = baos.toByteArray();
            return byteArray;

        } catch (IOException e) {
            log.error("SerializeUtil-serialize() error:{}", e);
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public static Object unSerialize(byte[] bytes) {

        ByteArrayInputStream bais = null;

        try {
            //反序列化为对象
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();

        } catch (Exception e) {
            log.error("SerializeUtil-unSerialize() error:{}", e);
        }
        return null;
    }
}