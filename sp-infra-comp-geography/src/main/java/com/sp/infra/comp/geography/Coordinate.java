package com.sp.infra.comp.geography;

import com.google.common.base.Preconditions;
import com.sp.infra.comp.geography.model.CoordinateInfo;
import com.sp.infra.comp.geography.model.CoordinateType;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 坐标系信息
 */
public class Coordinate {
    private final double xPi = 3.14159265358979324 * 3000.0 / 180.0;
    private Map<CoordinateType, CoordinateInfo> coordinates = new HashMap<>(2);

    /**
     * 通过经纬度初始化地理点
     *
     * @param coordinateType 坐标系类型
     * @param longitude      经度
     * @param latitude       纬度
     */
    public Coordinate(@NotNull CoordinateType coordinateType, double longitude, double latitude) {
        initial(coordinateType, longitude, latitude);
    }

    /**
     * 通过地理点信息初始化
     *
     * @param coordinateInfo 坐标信息
     */
    public Coordinate(@NotNull CoordinateInfo coordinateInfo) {
        Preconditions.checkNotNull(coordinateInfo, "地理点信息为空");
        Preconditions.checkNotNull(coordinateInfo.getCoordinateType(), "坐标点没有坐标系类型");

        initial(coordinateInfo.getCoordinateType(), coordinateInfo.getLongitude(), coordinateInfo.getLatitude());
    }

    /**
     * 根据坐标系类型获取相应的坐标信息
     *
     * @param coordinateType 坐标系类型
     * @return 坐标信息
     */
    public CoordinateInfo getCoordinate(@NotNull CoordinateType coordinateType) {
        return this.coordinates.get(coordinateType);
    }

    private void initial(@NotNull CoordinateType coordinateType, double longitude, double latitude) {
        CoordinateInfo currentCoordinate = new CoordinateInfo();
        currentCoordinate.setCoordinateType(coordinateType);
        currentCoordinate.setLatitude(latitude);
        currentCoordinate.setLongitude(longitude);
        coordinates.put(coordinateType, currentCoordinate);

        List<CoordinateType> types = EnumUtils.getEnumList(CoordinateType.class);

        for (CoordinateType type : types) {
            if (this.coordinates.containsKey(type)) {
                continue;
            }

            CoordinateInfo coordinateInfo = convert(type, currentCoordinate);

            coordinates.put(coordinateInfo.getCoordinateType(), coordinateInfo);
        }
    }

    private CoordinateInfo convert(@NotNull CoordinateType convertToType, @NotNull CoordinateInfo coordinateInfo) {
        if (convertToType == coordinateInfo.getCoordinateType()) {
            return coordinateInfo;
        }

        CoordinateInfo coordinateOut = new CoordinateInfo();
        coordinateOut.setCoordinateType(convertToType);

        double longitude = coordinateInfo.getLongitude();
        double latitude = coordinateInfo.getLatitude();
        double z, theta, x2, y2;
        switch (convertToType) {
            case BAIDU:
                z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * xPi);
                theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * xPi);
                x2 = z * Math.cos(theta) + 0.0065;
                y2 = z * Math.sin(theta) + 0.006;
                break;
            default:
                double x = longitude - 0.0065;
                double y = latitude - 0.006;
                z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * xPi);
                theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * xPi);
                x2 = z * Math.cos(theta);
                y2 = z * Math.sin(theta);
                break;
        }

        coordinateOut.setLongitude(x2);
        coordinateOut.setLatitude(y2);

        return coordinateOut;
    }
}
