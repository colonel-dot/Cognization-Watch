package com.example.bridge.domain.location;

import java.util.Date;

/**
 * 表示老年端设备定位某一时刻的围栏内外状态
 */
public record GeofenceStatus(boolean isInside, Date timestamp) {
}
