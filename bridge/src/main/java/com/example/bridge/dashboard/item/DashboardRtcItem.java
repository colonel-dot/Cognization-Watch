package com.example.bridge.dashboard.item;

public class DashboardRtcItem implements DashboardItem {

    private final String name;

    public DashboardRtcItem(String name) {
        this.name = name;
    }

    @Override
    public int getViewType() {
        return DashboardItem.TYPE_RTC;
    }

    public String getName() {
        return name;
    }

}
