# sp-infra-comp-geography

    1.提供第三方地图坐标的操作
    2.提供点位等工具类
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-geography</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 使用案例
### 自定义坐标信息
```java
package com.yonghui.web.frame.model.address;

import com.yonghui.common.geography.model.CoordinateInfo;
import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class UserCoordinate implements Serializable {
    private Integer cityId;
    private CoordinateInfo coordinateInfo;

    public UserCoordinate() {
    }

    public String toString() {
        return (new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)).append("cityId", this.cityId).append("coordinateInfo", this.coordinateInfo).toString();
    }

    public Integer getCityId() {
        return this.cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public CoordinateInfo getCoordinateInfo() {
        return this.coordinateInfo;
    }

    public void setCoordinateInfo(CoordinateInfo coordinateInfo) {
        this.coordinateInfo = coordinateInfo;
    }
}

```
### 使用方式
```java
   private LocationVO getUserLocation(String areaName, UserCoordinate userCoordinate) {
        Long cityid = null;
        String lng = null;
        String lat = null;
        if (userCoordinate != null) {
        if (userCoordinate.getCityId() != null) {
        cityid = Long.valueOf(userCoordinate.getCityId());
        }
        if (userCoordinate.getCoordinateInfo() != null) {
        lng = String.valueOf(userCoordinate.getCoordinateInfo().getLongitude());
        }
        if (userCoordinate.getCoordinateInfo() != null) {
        lat = String.valueOf(userCoordinate.getCoordinateInfo().getLatitude());
        }
        }
        String cityName = null;
        if(cityid != null){
        City city = localCacheHelper.getCityCache(cityid);
        cityName = city != null ? city.getCityName() : null;
        }
        LocationVO userLocation = LocationVO.builder()
        .cityId(cityid)
        .cityName(cityName)
        .latitude(lat)
        .longitude(lng)
        .areaName(areaName)
        .build();
        return userLocation;
        }
```

