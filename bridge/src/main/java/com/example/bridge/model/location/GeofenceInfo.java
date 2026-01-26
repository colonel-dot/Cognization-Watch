package com.example.bridge.model.location;

import com.baidu.trace.model.LatLng;

/**
 * 包含围栏的静态配置信息的围栏实体类
 */
public class GeofenceInfo {

    private long fenceId; // 百度地图SDK可能没有注明使用围栏ID字段的情况，该字段可以考虑选择性去除
    private String fenceName;

    private String monitoredPerson; // Entity-entityName

    // 圆形围栏
    private LatLng center;
    private double radius; // 0 <= x <= 5000 meters

    // 行政区围栏
    private String keyword;

    private GeofenceInfo() {}

    public static GeofenceInfo newCircularFence(long fenceId, String fenceName, String monitoredPerson, LatLng center, double radius) {
        GeofenceInfo info = new GeofenceInfo();
        info.fenceId = fenceId;
        info.fenceName = fenceName;
        info.monitoredPerson = monitoredPerson;
        info.center = center;
        info.radius = radius;
        return info;
    }

    public static GeofenceInfo newDistrictFence(long fenceId, String fenceName, String monitoredPerson, String keyword) {
        GeofenceInfo info = new GeofenceInfo();
        info.fenceId = fenceId;
        info.fenceName = fenceName;
        info.monitoredPerson = monitoredPerson;
        info.keyword = keyword;
        return info;
    }

    public long getFenceId() {
        return fenceId;
    }

    public String getFenceName() {
        return fenceName;
    }

    public String getMonitoredPerson() {
        return monitoredPerson;
    }

    public LatLng getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public String getKeyword() {
        return keyword;
    }
}
