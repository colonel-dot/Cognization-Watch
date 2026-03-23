package com.example.bridge.dashboard.item;

public interface DashboardItem {
    int TYPE_RTC = 0;
    int TYPE_RISK = 1;
    int TYPE_COLLECTION = 2;
    int TYPE_ALERT = 3;

    int getViewType();
}
