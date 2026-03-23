package com.example.bridge.dashboard.item;

public class DashboardRtcItem implements DashboardItem {

    private String name;
    private String status;

    public DashboardRtcItem(String name, String status) {
        this.name = name;
        this.status = status;
    }

    @Override
    public int getViewType() {
        return DashboardItem.TYPE_RTC;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
