package com.sp.infra.comp.geography.model;

import org.apache.commons.lang3.EnumUtils;

import java.util.List;
import java.util.Objects;

/**
 * 坐标系类型
 */
public enum CoordinateType {
    /**
     * 百度坐标系
     */
    BAIDU("baidu", "百度坐标系"),
    /**
     * 火星坐标系，高德，阿里使用
     */
    MARS("mars", "火星坐标系，高德，阿里、腾讯使用");

    private static List<CoordinateType> VALUES = EnumUtils.getEnumList(CoordinateType.class);

    private String value;
    private String description;

    CoordinateType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CoordinateType getCoordinateType(String value) {
        return VALUES.stream().filter((n) ->
                Objects.equals(n.getValue(), value)
        ).findAny().orElse(null);
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
