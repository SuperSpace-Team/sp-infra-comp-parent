package com.sp.infra.comp.geography;

import com.google.common.base.Preconditions;
import com.sp.infra.comp.geography.model.CoordinateInfo;
import com.sp.infra.comp.geography.model.CoordinateType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成地理坐标多边形
 */
public class CoordinatePolygon {
    private CoordinateType coordinateType;
    private List<List<CoordinateInfo>> polygonPoints;
    private int index = 0;

    public CoordinatePolygon(CoordinateType coordinateType) {
        this.coordinateType = coordinateType;
        polygonPoints = new ArrayList<>();
    }

    /**
     * 检查地理位置点是否在指定的区域范围内
     *
     * @param coordinatePoint 地理点
     * @return 在范围内返回true
     */
    public boolean checkIfCoordinatePointInRange(@NotNull CoordinateInfo coordinatePoint) {
        Preconditions.checkNotNull(coordinatePoint, "需要检查的地理点为空");

        if (coordinatePoint.getCoordinateType() != this.coordinateType) {
            Coordinate coordinate = new Coordinate(coordinatePoint);
            coordinatePoint = coordinate.getCoordinate(coordinateType);
        }

        boolean inRange = false;

        for (List<CoordinateInfo> polygonPoint : this.polygonPoints) {
            List<Point> pointList = polygonPoint.stream().map(n -> new Point(n.getLongitude(),
                    n.getLatitude())).collect(Collectors.toList());

            inRange = GeoUtil.isPointInPolygon(coordinatePoint.getLongitude(),
                    coordinatePoint.getLatitude(),
                    pointList.toArray(new Point[pointList.size()]));

            if (inRange) {
                break;
            }
        }

        return inRange;
    }

    /**
     * 增加用来描绘多边形的点，顺序相关
     *
     * @param coordinateInfo 多边形点信息，坐标系可以不同，会强制转换为多边形对应的坐标系
     */
    public void addPoint(@NotNull CoordinateInfo coordinateInfo) {
        if (this.polygonPoints.size() < index + 1) {
            this.polygonPoints.add(new ArrayList<>());
        }

        List<CoordinateInfo> currentPoints = this.polygonPoints.get(index);

        if (coordinateInfo.getCoordinateType() == coordinateType) {
            currentPoints.add(coordinateInfo);
        } else {
            Coordinate coordinate = new Coordinate(coordinateInfo);
            currentPoints.add(coordinate.getCoordinate(coordinateType));
        }
    }

    /**
     * 关闭当前绘画多边形区域，每次完成一个多边形绘画后调用
     */
    public void closure() {
        index += 1;
    }
}
