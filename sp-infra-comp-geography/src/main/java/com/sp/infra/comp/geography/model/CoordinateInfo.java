package com.sp.infra.comp.geography.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class CoordinateInfo implements Serializable {
    @JSONField(name = "lat")
    private double latitude;
    @JSONField(name = "lng")
    private double longitude;
    @JSONField(serialize = false)
    private CoordinateType coordinateType;

    public CoordinateInfo() {
        this.coordinateType = CoordinateType.BAIDU;
    }

    /**
     * 初始化一个地理点
     *
     * @param coordinateType 坐标系类型
     * @param longitude      经度
     * @param latitude       纬度
     */
    public CoordinateInfo(CoordinateType coordinateType, double longitude, double latitude) {
        this.coordinateType = coordinateType;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CoordinateInfo that = (CoordinateInfo) o;

        return new EqualsBuilder()
                .append(latitude, that.latitude)
                .append(longitude, that.longitude)
                .append(coordinateType, that.coordinateType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(latitude)
                .append(longitude)
                .append(coordinateType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("坐标系", coordinateType == null ? "无" : coordinateType.getDescription())
                .append("经度", longitude)
                .append("纬度", latitude)
                .toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public CoordinateType getCoordinateType() {
        return coordinateType == null ? CoordinateType.BAIDU : coordinateType;
    }

    public void setCoordinateType(CoordinateType coordinateType) {
        this.coordinateType = coordinateType;
    }
}
