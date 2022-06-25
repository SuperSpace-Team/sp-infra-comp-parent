package com.sp.infra.comp.geography;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Preconditions;
import com.sp.infra.comp.geography.model.CoordinateInfo;
import com.sp.infra.comp.geography.model.CoordinateType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class CoordinateUtils {
    /**
     * 根据坐标系类型和坐标点范围二维列表，获取坐标区域范围
     *
     * @param coordinateType   坐标系类型
     * @param coordinateRanges 区域范围
     * @return 坐标区域
     */
    @Contract(pure = true)
    @NotNull
    public static CoordinatePolygon makeCoordinatePolygon(@NotNull CoordinateType coordinateType,
                                                                                       @NotNull List<List<CoordinateInfo>> coordinateRanges) {
        Preconditions.checkArgument(!coordinateRanges.isEmpty(), "地理点数据为空");

        CoordinatePolygon coordinatePolygon = new CoordinatePolygon(coordinateType);

        for (List<CoordinateInfo> coordinates : coordinateRanges) {
            if (coordinates == null || coordinates.isEmpty()) {
                continue;
            }

            coordinates.forEach(coordinatePolygon::addPoint);

            coordinatePolygon.closure();
        }

        return coordinatePolygon;
    }

    /**
     * 计算两个地理点之间的距离，单位：米
     *
     * @param coordinateInfo1 地理点1
     * @param coordinateInfo2 地理点2
     * @return 米
     */
    @Contract(pure = true)
    public static long calculateDistance(@NotNull CoordinateInfo coordinateInfo1,
                                         @NotNull CoordinateInfo coordinateInfo2) {
        Preconditions.checkNotNull(coordinateInfo1, "地理点1为空");
        Preconditions.checkNotNull(coordinateInfo2, "地理点2为空");

        if (coordinateInfo1.getCoordinateType() != coordinateInfo2.getCoordinateType()) {
            Coordinate coordinate = new Coordinate(coordinateInfo2);
            coordinateInfo2 = coordinate.getCoordinate(coordinateInfo1.getCoordinateType());
        }

        double a, b, R;
        R = 6378137; // 地球半径
        double lat1 = coordinateInfo1.getLatitude() * Math.PI / 180.0;
        double lat2 = coordinateInfo2.getLatitude() * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (coordinateInfo1.getLongitude() - coordinateInfo2.getLongitude()) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return (long) d;
    }

    public static CoordinatePolygon convertCoordinatePolygon(String geoJson) {
        if (StringUtils.isNotBlank(geoJson)) {
            //多个围栏数据
            List<List<CoordinateInfo>> coordinateRanges = JSON.parseObject(geoJson, new TypeReference<List<List<CoordinateInfo>>>() {
            });
            return makeCoordinatePolygon(CoordinateType.BAIDU, coordinateRanges);
        }
        return null;
    }
}